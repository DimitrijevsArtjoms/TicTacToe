package com.example.tictactoe.game
// LLM chatGPT-4o suggested to implement this through interface for future possibilities
// and more clear structure of the project
// to get that answer I asked how to make code cleaner,
// because I wanted to try to make the code structure cleaner, more universal and structured
interface IGameBoard {
    fun getCell(row: Int, col: Int): Player
    fun setCell(player: Player, row: Int, col: Int): Boolean
    fun isFull(): Boolean   // returns true or false depending on whether the board is full or not
    fun reset()
}