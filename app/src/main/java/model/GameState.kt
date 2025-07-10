package model

/**
 * Verantwortung: UI interaktion mit spieler
 * ruft methden von GameEngine und BluetoothService auf
 */

enum class Phase {
    PLACING,
    MOVING,
    FLYING,
    REMOVING
}

data class GameState(

    var currentPlayer: Player = Player.PLAYER_ONE,
    var phase: Phase = Phase.PLACING,
    var stonesPlaced: MutableMap<Player, Int> = mutableMapOf(
        Player.PLAYER_ONE to 0,
        Player.PLAYER_TWO to 0
    ),
    var stonesInPlay: MutableMap<Player, Int> = mutableMapOf(
        Player.PLAYER_ONE to 0,
        Player.PLAYER_TWO to 0
    )

)