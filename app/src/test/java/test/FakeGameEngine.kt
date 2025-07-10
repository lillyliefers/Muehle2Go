package test

import controller.GameEngine
import model.Board
import model.Move
import model.Phase
import model.Player

class FakeGameEngine : GameEngine(Board()) {
    var lastPlacedIndex: Int? = null

    override fun placePiece(index: Int): Move? {
        lastPlacedIndex = index
        return Move(index, Player.PLAYER_ONE, false)
    }

    //override fun getPhase(): Phase {
     //   return Phase.PLACING
   // }

    override fun getCurrentPlayer(): Player {
        return Player.PLAYER_ONE
    }
}