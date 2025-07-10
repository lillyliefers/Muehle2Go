package model

data class Move(
    val index: Int,
    val player: Player,
    val millFormed: Boolean,
    val lastPlacement: Boolean = false
)