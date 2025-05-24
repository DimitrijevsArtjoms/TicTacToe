package com.example.tictactoe.game



interface GameProgressListener {
    fun onTurnChanged(player: Player)  // called when the turn switches to another player
    fun onGameFinished(winner: Player) // called when the game ends with a winner or a draw
}

// delegation of the interface implementation to the Game Board object
// so that there is no need to duplicate IGameBoard methods in GameProgress
class GameProgress(private val listener: GameProgressListener) : IGameBoard by GameBoard() {
    val gameBoard = GameBoard()  // game board state
    val firstTurn = Player.X
    private var currentTurn = firstTurn
        private set  // so that the value cannot be set from outside
    fun getCurrentTurn(): Player = currentTurn // return current player(maybe turn isn't very good name for ths variable and method)
    fun setCurrentTurn(player : Player) { // set current player
        currentTurn = player
    }

    fun makeMove(row: Int, col: Int): Boolean {
        if (!gameBoard.setCell(currentTurn, row, col)) return false  // if you can't make a move
                                                                     // return false
        val winner = checkWinner() // check if there is a winner

        if (winner != Player.NONE) {         // if there is a winner
            listener.onGameFinished(winner)  // notify the listener about end of the game
        } else if (gameBoard.isFull()) {     // if there is a draw on the board (boards is full and no winners)
            listener.onGameFinished(Player.NONE)  // and notify about draw
        }
        else if (!gameBoard.isFull()) {        // if no winners and not draw
            currentTurn = if (currentTurn == Player.X) Player.O else Player.X
            listener.onTurnChanged(currentTurn)  // notify about turn change
        }

        return true  // if move was successful
    }

    fun checkWinner(): Player {         // function that checks if there is a winner
        val b = Array(3) { row -> Array(3) { col -> gameBoard.getCell(row, col) } } // making "board" array

        for (i in 0..2) {
            if (b[i][0] != Player.NONE && b[i][0] == b[i][1] && b[i][1] == b[i][2]) return b[i][0]  // check all horizontal lines
            if (b[0][i] != Player.NONE && b[0][i] == b[1][i] && b[1][i] == b[2][i]) return b[0][i]  // check all vertical lines
        }

        // check both diagonal lines
        if (b[0][0] != Player.NONE && b[0][0] == b[1][1] && b[1][1] == b[2][2]) return b[0][0]
        if (b[0][2] != Player.NONE && b[0][2] == b[1][1] && b[1][1] == b[2][0]) return b[0][2]

        return Player.NONE  // if winner is not found in all lines
    }

}