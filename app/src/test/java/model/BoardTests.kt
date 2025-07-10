package model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BoardTests {

    private lateinit var board: Board

    @Before
    fun setup() {
        board = Board()
    }

    @Test
    fun testPlaceStone() {
        val result1 = board.placeStone(0, Player.PLAYER_ONE)
        val result2 = board.placeStone(0, Player.PLAYER_TWO)

        assertTrue(result1)
        assertFalse(result2)
        assertEquals(Player.PLAYER_ONE, board.positions[0].occupant)
    }

    @Test
    fun testMoveStone() {
        board.placeStone(0, Player.PLAYER_ONE)
        val moveValid = board.moveStone(0, 1, Player.PLAYER_ONE)
        val moveInvalid = board.moveStone(1, 14, Player.PLAYER_ONE) // not adjacent

        assertTrue(moveValid)
        assertFalse(moveInvalid)
        assertEquals(Player.PLAYER_ONE, board.positions[1].occupant)
        assertNull(board.positions[0].occupant)
    }

    @Test
    fun testRemoveStone() {
        board.placeStone(0, Player.PLAYER_ONE)
        board.placeStone(1, Player.PLAYER_TWO)

        val removeValid = board.removeStone(1, Player.PLAYER_ONE)
        val removeInvalid = board.removeStone(1, Player.PLAYER_TWO) // versucht eigenen Stein zu entfernen

        assertTrue(removeValid)
        assertFalse(removeInvalid)
        assertNull(board.positions[1].occupant)
        assertEquals(Player.PLAYER_ONE, board.positions[0].occupant)
    }

    @Test
    fun testFormsMill() {
        board.placeStone(0, Player.PLAYER_ONE)
        board.placeStone(1, Player.PLAYER_ONE)
        board.placeStone(2, Player.PLAYER_ONE)

        assertTrue(board.formsMill(0, Player.PLAYER_ONE))
        assertTrue(board.formsMill(1, Player.PLAYER_ONE))
        assertTrue(board.formsMill(2, Player.PLAYER_ONE))

        assertFalse(board.formsMill(0, Player.PLAYER_TWO))
    }

    @Test
    fun testResetBoard() {
        board.placeStone(0, Player.PLAYER_ONE)
        board.placeStone(1, Player.PLAYER_TWO)

        board.resetBoard()

        board.positions.forEach {
            assertNull(it.occupant)
        }
    }
}