package controller

import model.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.*

class GameEngineTests {

    private lateinit var board: Board
    private lateinit var engine: GameEngine

    @Before
    fun setup() {
        board = mock()
        engine = GameEngine(board)
    }

    @Test
    fun testHandleMovePlacing() {
        whenever(board.isOccupied(0)).thenReturn(false)
        whenever(board.placeStone(0, Player.PLAYER_ONE)).thenReturn(true)
        whenever(board.formsMill(0, Player.PLAYER_ONE)).thenReturn(false)

        val result = engine.placePiece(0)
        assertNotNull(result)
        assertEquals(Player.PLAYER_ONE, result.player)
        assertFalse(result.millFormed)
        assertEquals(Player.PLAYER_TWO, engine.getCurrentPlayer())
    }

    @Test
    fun testHandleMoveInvalidMove() {
        whenever(board.isOccupied(0)).thenReturn(true)
        val result = engine.placePiece(0)
        assertNull(result)
        assertEquals(Player.PLAYER_ONE, engine.getCurrentPlayer())
    }

    @Test
    fun testHandleMoveMovingPhase() {
        // Set up for MOVING phase
        engine.remainingStones[Player.PLAYER_ONE] = 0
        engine.gameState.stonesInPlay[Player.PLAYER_ONE] = 4
        engine.updatePhaseForPlayer(Player.PLAYER_ONE)

        whenever(board.isOccupied(5)).thenReturn(false)
        whenever(board.isAdjacent(1, 5)).thenReturn(true)
        whenever(board.getOccupant(1)).thenReturn(Player.PLAYER_ONE)
        whenever(board.moveStone(1, 5, Player.PLAYER_ONE)).thenReturn(true)
        whenever(board.formsMill(5, Player.PLAYER_ONE)).thenReturn(false)

        val success = engine.movePiece(1, 5, Player.PLAYER_ONE)
        assertTrue(success)
        assertEquals(Player.PLAYER_TWO, engine.getCurrentPlayer())
    }

    @Test
    fun testHandleMoveRemovingPhase() {
        // Set up a stone to remove
        whenever(board.getOccupant(10)).thenReturn(Player.PLAYER_TWO)
        whenever(board.formsMill(10, Player.PLAYER_TWO)).thenReturn(false)
        whenever(board.removeStone(10, Player.PLAYER_ONE)).thenReturn(true)

        val result = engine.removeOpponentPiece(10)
        assertTrue(result.success)
    }

    @Test
    fun testPhaseTransitions() {
        // PLACING to MOVING
        engine.setPhase(Phase.PLACING)
        engine.remainingStones[Player.PLAYER_ONE] = 0
        engine.remainingStones[Player.PLAYER_TWO] = 0
        engine.remainingStones[Player.PLAYER_ONE] = 0
        engine.gameState.stonesInPlay[Player.PLAYER_ONE] = 3

        engine.updatePhaseForPlayer(Player.PLAYER_ONE)
        assertEquals(Phase.FLYING, engine.getPhaseForPlayer(Player.PLAYER_ONE))
    }

    @Test
    fun testMillDetection() {
        whenever(board.formsMill(3, Player.PLAYER_ONE)).thenReturn(true)
        assertTrue(engine.isMillFormed(3))
    }

    @Test
    fun testSwitchPlayer() {
        val initial = engine.getCurrentPlayer()
        engine.switchPlayer()
        assertNotEquals(initial, engine.getCurrentPlayer())
    }

    @Test
    fun testRemainingStonesOnBoard() {
        engine.remainingStones[Player.PLAYER_ONE] = 5
        assertEquals(5, engine.remainingStonesForCurrentPlayer())
    }

    @Test
    fun testRemoveOpponentPiece_invalid() {
        whenever(board.getOccupant(10)).thenReturn(Player.PLAYER_ONE)

        val result = engine.removeOpponentPiece(10)
        assertFalse(result.success)
        assertEquals("Kein gültiger Gegnerstein.", result.message)
    }

}