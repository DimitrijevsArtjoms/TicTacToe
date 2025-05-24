package com.example.tictactoe.game

// implementation of interface allows different implementations of
// a game board (e.g., larger board, online) without changing the rest of the game logic in the future
class GameBoard : IGameBoard {
    val board = Array(3) { Array(3) { Player.NONE} }

    override fun getCell(row: Int, col: Int): Player = board[row][col]     //get info about cell
    override fun setCell(player: Player, row: Int, col: Int) : Boolean {   //set info in cell if there isn't mark(X or O)
        if(board[row][col] == Player.NONE){  // allow place mark if the cell is empty
            board[row][col] = player
            return true     // return true if successful
        }
        return false        // return false if not successful
    }

    override fun isFull(): Boolean {
        // check if the board is full
        // allow to detect draw and make move if there is place for it
        return board.all { row -> row.all { it != Player.NONE }}
    }

    override fun reset() {     //clear the board
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                board[i][j] = Player.NONE // all cells become empty
            }
        }
    }

}
