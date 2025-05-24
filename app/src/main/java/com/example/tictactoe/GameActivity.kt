package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import android.content.Intent
import com.example.tictactoe.game.GameProgress
import com.example.tictactoe.game.GameProgressListener
import com.example.tictactoe.game.Player

class GameActivity : ComponentActivity(), GameProgressListener {

    private lateinit var turnTextView: TextView
    private lateinit var gameProgress: GameProgress
    private lateinit var gameMode: String
    private var isPvCMode: Boolean = false
    private var playerXName: String = ""
    private var playerOName: String = ""


    private val handler = Handler(Looper.getMainLooper()) // handler for delay before PvC makes turn


    override fun onCreate(savedInstanceState: Bundle?) {  // initializing the game
        super.onCreate(savedInstanceState)  // call the superclass constructor to set up the activity
        setContentView(R.layout.activity_game)


        turnTextView = findViewById(R.id.turnTextView) // label to display who's turn it is


        gameMode = intent.getStringExtra("GAME_MODE") ?: "PvP"
        isPvCMode = (gameMode == "PvC") // check if game mode is PvC

        if (isPvCMode) { // if PvC mode => needs only 1 name for players, second player is computer
            playerXName = intent.getStringExtra("PLAYER_NAME") ?: "Player X"
            playerOName = "Computer"
        } else {   // if not PvC => user can input both names or go with default player X and player O
            playerXName = intent.getStringExtra("PLAYER_X_NAME") ?: "Player X"
            playerOName = intent.getStringExtra("PLAYER_O_NAME") ?: "Player O"
        }

        gameProgress = GameProgress(this) // initializing the game progress object

        if (savedInstanceState != null) {  // if savedInstanceState is not null => means that we have saved state
            restoreGameState(savedInstanceState) // restoring the game state after rotation
        }

        updateTurnText()  // update label to display who's turn it is
    }

    override fun onSaveInstanceState(outState: Bundle) {  // to save the game state before rotation
        super.onSaveInstanceState(outState)  // call the superclass method to save the instance state

        val boardState = Array(3) { row ->
            Array(3) { col ->
                gameProgress.gameBoard.board[row][col].name
            }
        }  // initialazing 2d dimension board for saving player's moves(X and O) on the board

        outState.putSerializable("BOARD_STATE", boardState)   // 2d dimension board with players(X and O)
        outState.putString("CURRENT_PLAYER", gameProgress.getCurrentTurn().name) // to save current turn to restore it after rotation
    }

    private fun restoreGameState(savedInstanceState: Bundle) {  // function to restore the game state(board, which player makes turn at this moment)
        val boardState = savedInstanceState.getSerializable("BOARD_STATE") as? Array<Array<String>>  // initializing 2d dimension board with players(X and O)
        val currentPlayer = savedInstanceState.getString("CURRENT_PLAYER")  // which player makes turn at this moment

        if (boardState != null && currentPlayer != null) {  // if we have saved state (we have some info in boardState and we have Player that made last move)
            for (row in 0..2) { // loops through the board's rows and columns
                for (col in 0..2) {
                    val player = Player.valueOf(boardState[row][col])
                    gameProgress.gameBoard.board[row][col] = player   // writing in players(X and O)

                    val buttonId = resources.getIdentifier("button$row$col", "id", packageName)  // getting every button id by row and col
                    val button = findViewById<Button>(buttonId) // and getting this button by id
                    button.text = when (player) {       // place X or O symbol in button depending on player
                        Player.X -> "X"
                        Player.O -> "O"
                        else -> ""
                    }
                    button.isEnabled = (player == Player.NONE)  // if player is NONE => button is enabled for clicking, otherwise => disabled
                }
            }
            gameProgress.setCurrentTurn(Player.valueOf(currentPlayer))  // setting current player to save it and restore after rotation
        }
    }

    private fun updateTurnText() {  // method to update label to display who's turn it is
        val currentTurn = gameProgress.getCurrentTurn()
        val playerText = when (currentTurn) {
            Player.X -> "$playerXName's turn" // 1st player's name (X)
            Player.O -> if (isPvCMode) "Computer's turn" else "$playerOName's turn" // 2nd player's name (O)
            else -> "Waiting for a turn" // if there is no player or maybe some error?
        }
        turnTextView.text = playerText
    }

    override fun onTurnChanged(player: Player) { // called when the turn switches to another player
        updateTurnText() // calling the change of label to display who's turn it is
        if (isPvCMode && player == Player.O) { // if PvC mode and it's O player's turn => Computer makes turn
            handler.postDelayed({
                makeComputerMove()
            }, 1000) // delay 1 second before PvC makes turn for thinking feel :)
        // in future maybe realise random time, e.g. 0.5-1.5 seconds,
        // but I don't know will it be a good idea or no
        }
    }

    override fun onGameFinished(winner: Player) { // to handle end of game, show winner and ask to restart the game or return to the main menu
        val winnerText = when (winner) {
            Player.X -> "$playerXName wins!"
            Player.O -> "$playerOName wins!"
            else -> "It's a draw!"
        }

        AlertDialog.Builder(this) // window to show info about end of the game and possibilities to restart/main menu
            .setTitle("Game Over!")
            .setMessage(winnerText)
            .setPositiveButton("Restart game") { dialog, _ ->   // restart the game
                gameProgress = GameProgress(this)
                resetGame() // reset game state for new game
                updateTurnText()
                dialog.dismiss()
            }
            .setNeutralButton("Go back to main screen") { dialog, _ ->  // go to the main menu and choose different mode or name
                navigateToMainMenu()
                dialog.dismiss()
            }
            .show()
    }
    fun resetGame() { // reset game state - board, label
        gameProgress = GameProgress(this)   // initializing the game progress object
        updateTurnText()

        val buttons = listOf(           // list of all buttons on the board's grid
            R.id.button00, R.id.button01, R.id.button02,
            R.id.button10, R.id.button11, R.id.button12,
            R.id.button20, R.id.button21, R.id.button22
        )

        for (buttonId in buttons) {         // reset all buttons to empty
            val button = findViewById<Button>(buttonId)
            button.text = ""
            button.isEnabled = true
        }
    }


    private fun navigateToMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("RESET_GAME", true) // like a flag that shows that we want to reset the main menu state
                                                        // from end of the game
        startActivity(intent)
        finish()
    }


    fun onCellClicked(view: android.view.View) {        // we need to handle when button(cell) on the board is clicked to process action
        val button = view as Button
        val id = button.id

        // map button's ids to row and column
        val (row, col) = when (id) {
            R.id.button00 -> 0 to 0
            R.id.button01 -> 0 to 1
            R.id.button02 -> 0 to 2
            R.id.button10 -> 1 to 0
            R.id.button11 -> 1 to 1
            R.id.button12 -> 1 to 2
            R.id.button20 -> 2 to 0
            R.id.button21 -> 2 to 1
            R.id.button22 -> 2 to 2
            else -> return
        }

        val currentPlayer = gameProgress.getCurrentTurn()  // get current player

        val moveSuccessful = gameProgress.makeMove(row, col)  // make move if it's possible and get the true or false result
        if (moveSuccessful) {
            button.text = if (currentPlayer == Player.X) "X" else "O" // if move is successful => set text to button X or O
            button.isEnabled = false // disable button after move, make it unclickable, inactive
        }
    }


    private fun makeComputerMove() {  // built-in gemini in Android studio suggested this implementation of randoming computer's move
        val emptyCells = gameProgress.gameBoard.board.flatMapIndexed { rowIndex, row ->        // Computer places O in random empty cell
            row.mapIndexedNotNull { colIndex, player ->                                        // future idea: use some algorithms, like AlphaBeta or MiniMax
                if (player == Player.NONE) rowIndex * 3 + colIndex else null                   // for implementation of AI
            }
        }
        val randomIndex = (0 until emptyCells.size).random()
        val (row, col) = emptyCells[randomIndex] / 3 to emptyCells[randomIndex] % 3        // tuple with random row and col for computer's move

        gameProgress.makeMove(row, col)

        // update the button after the computer's move
        val buttonId = resources.getIdentifier(     // getting button id by row and col
            "button$row$col", "id", packageName
        )
        val button = findViewById<Button>(buttonId)  // getting button by id
        button.text = "O"  // assuming 'O' is the computer's symbol.
        button.isEnabled = false // disable the button after computer's move
        // Also future idea: player can choose his own symbol X or O, to make game more interesting
        // I tried to do that, but something went wrong and app didn't open, giving out a lot of errors that needed to be corrected
    }
}
