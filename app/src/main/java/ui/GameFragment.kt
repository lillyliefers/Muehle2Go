package com.example.offlinemuehle.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.gridlayout.widget.GridLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.offlinemuehle.R
import model.Board
import model.Player
import controller.GameEngine
import model.Phase
import androidx.core.content.ContextCompat



class GameFragment : Fragment() {

    var gameEngine: GameEngine? = null
        private set

    private lateinit var currentPlayerText: TextView
    private var removeMode: Boolean = false
    private var selectedIndex: Int? = null
    private var selectedButtonIndex: Int? = null
    private lateinit var buttons: Array<Button?>
    private var selectedPlayer: Player? = null

    //private lateinit var gameStatusText: TextView
    private lateinit var restartButton: Button
    private lateinit var winnerMessageText: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    val millPositions = mapOf(
        0 to Pair(0, 0), 1 to Pair(0, 3), 2 to Pair(0, 6),
        3 to Pair(1, 1), 4 to Pair(1, 3), 5 to Pair(1, 5),
        6 to Pair(2, 2), 7 to Pair(2, 3), 8 to Pair(2, 4),
        9 to Pair(3, 0), 10 to Pair(3, 1), 11 to Pair(3, 2),
        12 to Pair(3, 4), 13 to Pair(3, 5), 14 to Pair(3, 6),
        15 to Pair(4, 2), 16 to Pair(4, 3), 17 to Pair(4, 4),
        18 to Pair(5, 1), 19 to Pair(5, 3), 20 to Pair(5, 5),
        21 to Pair(6, 0), 22 to Pair(6, 3), 23 to Pair(6, 6)
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (gameEngine == null) {
            gameEngine = GameEngine(Board())
        }

        currentPlayerText = view.findViewById(R.id.text_current_player)

        winnerMessageText = view.findViewById(R.id.text_winner_message)
        restartButton = view.findViewById(R.id.button_restart)

        restartButton.setOnClickListener {
            resetGame()
        }


        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)
        gridLayout.removeAllViews()
        gridLayout.columnCount = 7
        gridLayout.rowCount = 7

        buttons = arrayOfNulls(24) // Jetzt initialisiert!

        for (i in 0 until 7) {
            for (j in 0 until 7) {
                val isValid = millPositions.containsValue(Pair(i, j))
                if (isValid) {
                    val index = millPositions.filterValues { it == Pair(i, j) }.keys.first()
                    val button = Button(requireContext()).apply {
                        text = ""  // keine Zahlen mehr anzeigen
                        contentDescription = "grid_cell_$index"
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 100
                            height = 100
                            rowSpec = GridLayout.spec(i)
                            columnSpec = GridLayout.spec(j)
                            setMargins(8, 8, 8, 8)
                        }
                        background = ContextCompat.getDrawable(context, R.drawable.stone_empty)
                        backgroundTintList = null
                        setOnClickListener {
                            try {
                                handleMove(index)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(requireContext(), "Ein Fehler ist aufgetreten: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    buttons[index] = button
                    gridLayout.addView(button)
                } else {
                    val empty = Space(requireContext()).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 100
                            height = 100
                            rowSpec = GridLayout.spec(i)
                            columnSpec = GridLayout.spec(j)
                        }
                    }
                    gridLayout.addView(empty)
                }
            }
        }
    }

    fun updateUI() {
        val player = gameEngine?.getCurrentPlayer() ?: return
        currentPlayerText.text = "Aktueller Spieler: ${player.name}"
    }

    fun handleMove(index: Int) {
        if (!::buttons.isInitialized || buttons.isEmpty()) {
            showError("Spielfeld nicht bereit!")
            return
        }

        val player = gameEngine?.getCurrentPlayer() ?: return
        val playerPhase = gameEngine?.getPhaseForPlayer(player) ?: Phase.MOVING

        if (removeMode || playerPhase == Phase.REMOVING) {
            handleRemovingPhase(index)
            return
        }

        when (playerPhase) {
            Phase.PLACING -> handlePlacingPhase(index)
            Phase.MOVING -> handleMovingPhase(index)
            Phase.FLYING -> handleFlyingPhase(index)
            else -> showError("Unbekannte Spielphase!")
        }
    }

    private fun handlePlacingPhase(index: Int) {
        val move = gameEngine?.placePiece(index)
        if (move != null) {
            buttons[move.index]?.apply {
                text = ""
                background = ContextCompat.getDrawable(context,
                    if (move.player == Player.PLAYER_ONE) R.drawable.stone_black_normal
                    else R.drawable.stone_white_normal
                )
                backgroundTintList = null
            }
            updateBoardUI()

            if (move.millFormed) {
                removeMode = true
                Toast.makeText(requireContext(), "Mühle gebildet! Wähle einen gegnerischen Stein zum Entfernen", Toast.LENGTH_SHORT).show()
            } else {
                if (move.lastPlacement) {
                    Toast.makeText(requireContext(), "Alle Steine platziert - bewege nun deinen Stein!", Toast.LENGTH_SHORT).show()
                }
                updateUI()
            }
        } else {
            showError("Ungültiger Zug!")
        }
    }

    private fun handleRemovingPhase(index: Int) {
        val result = gameEngine?.removeOpponentPiece(index) ?: return

        if (result.success) {
            updateBoardUI()
            removeMode = false

            // Phasenwechsel korrekt prüfen: Spieler-spezifisch
            val currentPlayer = gameEngine?.getCurrentPlayer() ?: return
            val playerPhase = gameEngine?.getPhaseForPlayer(currentPlayer) ?: Phase.MOVING

            when (playerPhase) {
                Phase.PLACING, Phase.MOVING, Phase.FLYING -> {
                    updateUI()
                }
                Phase.REMOVING -> {
                    removeMode = true
                    Toast.makeText(requireContext(), "Noch ein gegnerischer Stein entfernen!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    showError("Unbekannte Spielphase nach Entfernen!")
                }
            }
        } else {
            showError(result.message ?: "Fehler beim Entfernen")
        }
    }

    private fun handleMovingPhase(index: Int) {
        val player = gameEngine?.getCurrentPlayer() ?: return
        val board = gameEngine?.getBoard() ?: return

        if (selectedIndex == null) {
            if (board[index] == player) {
                selectedButtonIndex?.let { prevIndex ->
                    val prevPlayer = board[prevIndex]
                    val drawable = if (prevPlayer == Player.PLAYER_ONE) R.drawable.stone_black_normal else R.drawable.stone_white_normal
                    buttons[prevIndex]?.background = ContextCompat.getDrawable(requireContext(), drawable)
                }

                val selectedDrawable = if (player == Player.PLAYER_ONE) R.drawable.stone_black_selected else R.drawable.stone_white_selected
                buttons[index]?.background = ContextCompat.getDrawable(context, selectedDrawable)

                selectedButtonIndex = index
                selectedIndex = index
                selectedPlayer = player

                Toast.makeText(requireContext(), "Feld $index ausgewählt - wähle ein benachbartes freies Feld", Toast.LENGTH_SHORT).show()
            } else if (board[index] == null) {
                showError("Nicht dein Stein!")
            }
        } else {
            // Attempt the move
            val from = selectedIndex!!
            val to = index
            val success = gameEngine?.movePiece(from, to, selectedPlayer) ?: false

            if (success) {
                buttons[from]?.background = ContextCompat.getDrawable(context, R.drawable.stone_empty)
                val playerAtDest = gameEngine?.getBoard()?.get(to)
                val drawable = if (playerAtDest == Player.PLAYER_TWO) R.drawable.stone_white_normal else R.drawable.stone_black_normal
                buttons[to]?.background = ContextCompat.getDrawable(context, drawable)

                if (gameEngine?.isMillFormed(to) == true) {
                    removeMode = true
                    Toast.makeText(requireContext(), "Mühle gebildet! Wähle einen gegnerischen Stein zum Entfernen", Toast.LENGTH_SHORT).show()
                } else {
                    updateBoardUI()
                    updateUI()
                }
            } else {
                showError("Ungültiger Zug!")
                if (gameEngine?.getBoard()?.get(to) != null) {
                    showError("Stein darf nicht auf ein besetztes Feld bewegt werden!")
                }
            }

            selectedIndex = null
            selectedButtonIndex = null
            selectedPlayer = null
        }
    }

    private fun handleFlyingPhase(index: Int) {
        val player = gameEngine?.getCurrentPlayer() ?: return
        val board = gameEngine?.getBoard() ?: return

        if (selectedIndex == null) {
            if (board[index] == player) {
                // Vorherige Auswahl zurücksetzen
                selectedButtonIndex?.let { prevIndex ->
                    val prevPlayer = board[prevIndex]
                    val drawable = if (prevPlayer == Player.PLAYER_ONE) R.drawable.stone_black_normal else R.drawable.stone_white_normal
                    buttons[prevIndex]?.background = ContextCompat.getDrawable(requireContext(), drawable)
                }

                val selectedDrawable = if (player == Player.PLAYER_ONE) R.drawable.stone_black_selected else R.drawable.stone_white_selected
                buttons[index]?.background = ContextCompat.getDrawable(context, selectedDrawable)

                selectedButtonIndex = index
                selectedIndex = index
                selectedPlayer = player

                Toast.makeText(requireContext(), "Feld $index ausgewählt - wähle ein beliebiges freies Feld", Toast.LENGTH_SHORT).show()
            } else if (board[index] != null){
                showError("Nicht dein Stein!")
            }
        } else {
            val from = selectedIndex!!
            val to = index
            val success = gameEngine?.movePieceFlying(from, to, selectedPlayer) ?: false

            if (success) {
                buttons[from]?.background = ContextCompat.getDrawable(context, R.drawable.stone_empty)
                val playerAtDest = gameEngine?.getBoard()?.get(to)
                val drawable = if (playerAtDest == Player.PLAYER_TWO) R.drawable.stone_white_normal else R.drawable.stone_black_normal
                buttons[to]?.background = ContextCompat.getDrawable(context, drawable)

                if (gameEngine?.isMillFormed(to) == true) {
                    removeMode = true
                    Toast.makeText(requireContext(), "Mühle gebildet! Wähle einen gegnerischen Stein zum Entfernen", Toast.LENGTH_SHORT).show()
                } else {
                    updateBoardUI()
                    updateUI()
                }
            } else {
                showError("Ungültiger Zug!")
                if (gameEngine?.getBoard()?.get(to) != null) {
                    showError("Stein darf nicht auf ein besetztes Feld bewegt werden!")
                }
            }

            selectedIndex = null
            selectedButtonIndex = null
            selectedPlayer = null
        }
    }


    // Helper: update the visual board to reflect the model state
    private fun updateBoardUI() {
        val board = gameEngine?.getBoard() ?: return
        for (i in buttons.indices) {
            val player = board[i]
            buttons[i]?.apply {
                when (player) {
                    Player.PLAYER_ONE -> {
                        background = ContextCompat.getDrawable(context, R.drawable.stone_black_normal)
                        backgroundTintList = null
                        text = ""
                    }
                    Player.PLAYER_TWO -> {
                        background = ContextCompat.getDrawable(context, R.drawable.stone_white_normal)
                        backgroundTintList = null
                        text = ""
                    }
                    else -> {
                        background = ContextCompat.getDrawable(context, R.drawable.stone_empty)
                        backgroundTintList = null
                        text = ""
                    }
                }
            }
        }
        // Prüfe ob Spiel vorbei ist
        val winner = gameEngine?.checkWinCondition()
        if (winner != null) {
            winnerMessageText.text = "${winner.name} hat gewonnen!"
            winnerMessageText.visibility = View.VISIBLE
            restartButton.visibility = View.VISIBLE
        } else {
            winnerMessageText.text = ""
            winnerMessageText.visibility = View.GONE
            restartButton.visibility = View.GONE
        }

        // Aktuellen Spieler anzeigen
        currentPlayerText.text = "Aktueller Spieler: ${gameEngine?.getCurrentPlayer()?.name}"
    }

    //helper for testing toast messages
    var lastErrorMessageForTest: String? = null;

    // Helper: show an error message to the user
    private fun showError(message: String) {
        lastErrorMessageForTest = message  // TEST HOOK
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun resetGame() {
        // Engine neu aufsetzen
        gameEngine = GameEngine(Board())

        // Board visuell leeren
        updateBoardUI()

        // Auswahl zurücksetzen
        selectedIndex = null
        selectedButtonIndex = null
        selectedPlayer = null
        removeMode = false

        // Anzeigen aktualisieren
        currentPlayerText.text = "Aktueller Spieler: ${gameEngine?.getCurrentPlayer()?.name}"
        winnerMessageText.text = ""
        winnerMessageText.visibility = View.GONE
        restartButton.visibility = View.GONE
    }


}
