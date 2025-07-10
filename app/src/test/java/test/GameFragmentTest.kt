package test

import org.junit.Assert.*
import org.junit.Test
import model.Phase
import model.Player

class FakeGameEngineTest {

    @Test
    fun testPlacePieceRecordsIndex() {
        val engine = FakeGameEngine()
        engine.placePiece(5)

        assertEquals(5, engine.lastPlacedIndex)
    }

    @Test
    fun testGetPhaseIsPlacing() {
        val engine = FakeGameEngine()
        assertEquals(Phase.PLACING, engine.getPhaseForPlayer(Player.PLAYER_ONE))
    }

    @Test
    fun testGetCurrentPlayer() {
        val engine = FakeGameEngine()
        assertEquals(Player.PLAYER_ONE, engine.getCurrentPlayer())
    }
}