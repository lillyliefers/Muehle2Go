package controller

import model.Board
import model.Player
import model.Move
import model.GameState
import model.Phase

/**
 * Verantwortlich für Spiellogik
 * (platzieren, Ziehen, Mühlenbildung, Siegprügung)
 *
 * test ideen: millenerkennung bei platzierung, verbotene züge (doppelte position), spielerwechsel nach zug
 */

data class RemoveResult(val success: Boolean, val message: String? = null)

open class GameEngine(private val board: Board) {

    //enum class Phase {PLACING, MOVING, FLYING }
    val gameState = GameState()
    private var currentPlayer = Player.PLAYER_ONE

    val remainingStones = mutableMapOf(
        Player.PLAYER_ONE to 9,
        Player.PLAYER_TWO to 9
    )

    /** Gibt das aktuelle Spielfeld zurück */
    fun getBoard(): Array<`Player`?> = board.getSnapshot()

    /** Gibt den aktuellen Spieler zurück */
    open fun getCurrentPlayer(): `Player` = `currentPlayer`

    fun setPhase(newPhase: Phase) {
        gameState.phase = newPhase
    }

    open fun placePiece(index: Int): Move? {
        if (playerPhases[currentPlayer] != Phase.PLACING) return null
        if (remainingStones[currentPlayer] == 0) return null
        if (board.isOccupied(index)) return null

        val success = board.placeStone(index, currentPlayer)
        if (!success) return null

        gameState.stonesPlaced[currentPlayer] = gameState.stonesPlaced[currentPlayer]!! + 1
        gameState.stonesInPlay[currentPlayer] = gameState.stonesInPlay[currentPlayer]!! + 1
        remainingStones[currentPlayer] = remainingStones[currentPlayer]!! - 1

        val millFormed = board.formsMill(index, currentPlayer)
        val move = Move(index, currentPlayer, millFormed)

        if (millFormed) {
            playerPhases[currentPlayer] = Phase.REMOVING
            return move
        }

        // Check if this player is done placing
        var placedLast = false
        if (remainingStones[currentPlayer] == 0) {
            placedLast = true
        }
        updateAllPlayerPhases()
        switchPlayer()

        return move.copy(lastPlacement = placedLast)

    }

    /** Versucht, eine Figur zu bewegen (nur in MOVING/FLYING erlaubt) */
    fun movePiece(from: Int, to: Int, player: Player?): Boolean {
        if (player == null) return false
        val phase = playerPhases[player] ?: return false

        if (board.isOccupied(to)) return false
        if (board.getOccupant(from) != player) return false

        val success = when (phase) {
            Phase.MOVING -> board.moveStone(from, to, player)
            Phase.FLYING -> board.moveStoneFlying(from, to, player)
            else -> return false
        }

        if (!success) return false

        val millFormed = board.formsMill(to, player)
        if (millFormed) {
            playerPhases[currentPlayer] = Phase.REMOVING
        } else {
            updateAllPlayerPhases()
            switchPlayer()
        }

        return true
    }

    fun movePieceFlying(from: Int, to: Int, player: Player?): Boolean {
        if (player == null) return false
        val phase = playerPhases[player] ?: return false
        if (phase != Phase.FLYING) return false

        if (board.isOccupied(to)) return false
        if (board.getOccupant(from) != player) return false

        val success = board.moveStoneFlying(from, to, player)
        if (!success) return false

        val millFormed = board.formsMill(to, player)

        if (millFormed) {
            playerPhases[currentPlayer] = Phase.REMOVING
        } else {
            updateAllPlayerPhases()
            switchPlayer()
        }

        return true
    }

    fun removeOpponentPiece(index: Int): RemoveResult {
        val opponent = if (currentPlayer == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
        val stoneOwner = board.getOccupant(index)
        println("REMOVE DEBUG: Position $index occupied by $stoneOwner. Remover is $currentPlayer, expects opponent $opponent.")

        if (stoneOwner != opponent) {
            return RemoveResult(false, "Kein gültiger Gegnerstein.")
        }

        if (board.formsMill(index, stoneOwner)) {
            val hasNonMill = opponentHasNonMillStones(opponent)
            if (hasNonMill) {
                return RemoveResult(false, "Kann keinen Stein aus einer Mühle entfernen.")
            }
        }

        if (board.removeStone(index, currentPlayer)) {
            gameState.stonesInPlay[opponent] = gameState.stonesInPlay[opponent]!! - 1

            // Gegner prüfen
            if ((remainingStones[opponent] ?: 0) > 0) {
                playerPhases[opponent] = Phase.PLACING
            } else {
                updatePhaseForPlayer(opponent)
            }

            // WICHTIG: Jetzt auch den Entferner (currentPlayer) prüfen!
            updatePhaseForPlayer(currentPlayer)
            updateAllPlayerPhases()
            switchPlayer()
            return RemoveResult(true)
        }
        return RemoveResult(false, "Fehler beim Entfernen des Steins.")
    }

    /** Prüft, ob an Position eine Mühle entstanden ist */
    fun isMillFormed(position: Int): Boolean {
        return board.formsMill(position, currentPlayer)
    }

    /** Spielerwechsel */
    fun switchPlayer() {
        currentPlayer = if (currentPlayer == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
    }

    fun remainingStonesForCurrentPlayer(): Int {
        return remainingStones[currentPlayer] ?: 0
    }

    private val playerPhases = mutableMapOf(
        Player.PLAYER_ONE to Phase.PLACING,
        Player.PLAYER_TWO to Phase.PLACING
    )

    fun getPhaseForPlayer(player: Player): Phase {
        return playerPhases[player] ?: Phase.PLACING
    }

    /** Prüft ob Gegner noch Steine hat, die NICHT in Mühlen sind */
    private fun opponentHasNonMillStones(opponent: Player): Boolean {
        for (pos in 0 until 24) {
            if (board.getOccupant(pos) == opponent && !board.formsMill(pos, opponent)) {
                return true
            }
        }
        return false
    }

    fun updatePhaseForPlayer(player: Player) {
        val stones = gameState.stonesInPlay[player] ?: 0
        val remaining = remainingStones[player] ?: 0

        playerPhases[player] = when {
            remaining > 0 -> Phase.PLACING
            stones > 3 -> Phase.MOVING
            stones == 3 -> Phase.FLYING
            else -> Phase.MOVING // oder GameOver
        }
    }

    fun updateAllPlayerPhases() {
        for (player in Player.values()) {
            updatePhaseForPlayer(player)
        }
    }

    fun checkWinCondition(): Player? {
        for (player in Player.values()) {
            if ((remainingStones[player] ?: 0) == 0 && (gameState.stonesInPlay[player] ?: 0) < 3) {
                // Spieler hat weniger als 3 Steine und keine mehr zum Platzieren
                return if (player == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
            }
        }
        return null
    }

}