package model

data class Position(val index: Int, var occupant: Player? = null)

class Board {

    val positions = Array(24) { Position(it) }

    private val adjacencyMap = mapOf(
        0 to listOf(1, 9), 1 to listOf(0, 2, 4), 2 to listOf(1, 14),
        3 to listOf(4, 10), 4 to listOf(1, 3, 5, 7), 5 to listOf(4, 13),
        6 to listOf(7, 11), 7 to listOf(4, 6, 8), 8 to listOf(7, 12),
        9 to listOf(0, 10, 21), 10 to listOf(3, 9, 11, 18), 11 to listOf(6, 10, 15),
        12 to listOf(8, 13, 17), 13 to listOf(5, 12, 14, 20), 14 to listOf(2, 13, 23),
        15 to listOf(11, 16), 16 to listOf(15, 17, 19), 17 to listOf(12, 16),
        18 to listOf(10, 19), 19 to listOf(16, 18, 20, 22), 20 to listOf(13, 19),
        21 to listOf(9, 22), 22 to listOf(19, 21, 23), 23 to listOf(14, 22)
    )

    fun getSnapshot(): Array<Player?> {
        return positions.map { it.occupant }.toTypedArray()
    }

    fun isAdjacent(from: Int, to: Int): Boolean {
        return adjacencyMap[from]?.contains(to) ?: false
    }

    fun isOccupied(index: Int): Boolean = positions[index].occupant != null

    fun getOccupant(index: Int): Player? = positions[index].occupant

    fun placeStone(index: Int, player: Player): Boolean {
        if (isOccupied(index)) return false
        positions[index].occupant = player
        return true
    }

    fun moveStone(from: Int, to: Int, player: Player): Boolean {
        if (!isOccupied(from) || isOccupied(to)) return false
        if (getOccupant(from) != player) return false
        if (!isAdjacent(from, to)) return false
        positions[from].occupant = null
        positions[to].occupant = player
        return true
    }

    fun moveStoneFlying(from: Int, to: Int, player: Player): Boolean {
        if (!isOccupied(from) || isOccupied(to)) return false
        if (getOccupant(from) != player) return false
        // KEINE Adjazenzprüfung hier
        positions[from].occupant = null
        positions[to].occupant = player
        return true
    }

    fun removeStone(index: Int, byPlayer: Player): Boolean {
        val stoneOwner = getOccupant(index)
        val opponent = if (byPlayer == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
        println("REMOVE DEBUG: Position $index occupied by $stoneOwner. Remover is $byPlayer, expects opponent $opponent.")
        if (stoneOwner == opponent) {
            positions[index].occupant = null
            return true
        }
        return false
    }

    fun resetBoard() {
        positions.forEach { it.occupant = null }
    }

    val millTriplets = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(9, 10, 11), listOf(12, 13, 14), listOf(15, 16, 17),
        listOf(18, 19, 20), listOf(21, 22, 23),
        listOf(0, 9, 21), listOf(3, 10, 18), listOf(6, 11, 15),
        listOf(1, 4, 7), listOf(16, 19, 22), listOf(8, 12, 17),
        listOf(5, 13, 20), listOf(2, 14, 23)
    )

    fun formsMill(index: Int, player: Player): Boolean {
        val result = millTriplets.any { triplet ->
            index in triplet && triplet.all { getOccupant(it) == player }
        }
        println("DEBUG formsMill check for $index/$player: $result")
        return result
    }
}