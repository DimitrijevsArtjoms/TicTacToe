package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    private var isPvC: Boolean? = null // nullable to enforce game mode selection before starting the game
    private var resetMainScreenState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        resetMainScreenState = intent.getBooleanExtra("RESET_GAME", false)

        // get all necessary elements from xml layout
        val pvpButton = findViewById<Button>(R.id.buttonPvP)
        val pvcButton = findViewById<Button>(R.id.buttonPvC)
        val playButton = findViewById<Button>(R.id.playButton)
        val player1NameInput = findViewById<EditText>(R.id.player1NameInput)
        val player2NameInput = findViewById<EditText>(R.id.player2NameInput)

        // restore state of the screen after rotation
        // LLM ChatGPT-4o helped to implement state saving after rotation
        // prompt: "how can i make the app to keep state when user rotate the screen,
        // but reset when user go back to the main screen from the game screen?"
        if (savedInstanceState != null) {
            player1NameInput.setText(savedInstanceState.getString("PLAYER_X_NAME", ""))
            player2NameInput.setText(savedInstanceState.getString("PLAYER_O_NAME", ""))
            val isPvcSelected = savedInstanceState.getBoolean("IS_PVC_SELECTED", false)

            when {
                isPvcSelected -> {
                    isPvC = true
                    highlightSelectedButton(pvcButton)
                    unhighlightButton(pvpButton)
                    player1NameInput.visibility = View.VISIBLE
                    player2NameInput.visibility = View.GONE
                }
                !isPvcSelected -> {
                    isPvC = false
                    highlightSelectedButton(pvpButton)
                    unhighlightButton(pvcButton)
                    player1NameInput.visibility = View.VISIBLE
                    player2NameInput.visibility = View.VISIBLE
                }
            }

        }



        pvpButton.setOnClickListener {
            isPvC = false  // to control what game mode selected and intent data later
            // we know that selected mode is not PvC => is PvP
            highlightSelectedButton(pvpButton)
            unhighlightButton(pvcButton)
            // to display the input for names of both players for PvP mode
            player1NameInput.visibility = View.VISIBLE
            player2NameInput.visibility = View.VISIBLE
        }
        pvcButton.setOnClickListener {
            isPvC = true // we know that selected mode is PvC
            highlightSelectedButton(pvcButton)
            unhighlightButton(pvpButton)
            player1NameInput.visibility = View.VISIBLE
            player2NameInput.visibility = View.GONE // we need only 1 player name for PvC


        }
        playButton.setOnClickListener {
            if (isPvC == null) {
                Toast.makeText(this, "Please select a game mode.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // blocking the start of the game
                                          // by returning from the click listener
            }

            var playerXName = player1NameInput.text.toString()
                // if O player's name input is empty => "O" by default
                // but for X player checking will be in the PvC mode,
                // because in PvP mode you can have empty names (will be default X and O)
                // but for PvC name is necessary for starting the game
            val playerOName = if (player2NameInput.text.toString().isEmpty()) "Player O" else player2NameInput.text.toString()

            if (isPvC == true) {
                if (playerXName.isEmpty()) { // necessary name for player in PvC
                    Toast.makeText(this, "Please enter your name to play.", Toast.LENGTH_SHORT).show()
                } else {
                    startGame(playerXName)
                }
            }
            else {
                if (playerXName.isEmpty()) {
                    playerXName = "Player X"
                    startGame(playerXName, playerOName)
                }
                else
                    startGame(playerXName, playerOName)
            }



        }
    }

    override fun onResume() { // to reset all fields after returning to main(start) screen
        super.onResume()
        if (resetMainScreenState) { // only if we go back from the game screen
                                    // otherwise state will be saved (after rotation)
            resetStartScreen() // reset the game mode selection
            resetMainScreenState = false // reset is not needed anymore, so false
        }
    }

    private fun resetStartScreen() {
        val pvpButton = findViewById<Button>(R.id.buttonPvP)
        val pvcButton = findViewById<Button>(R.id.buttonPvC)
        val player1NameInput = findViewById<EditText>(R.id.player1NameInput)
        val player2NameInput = findViewById<EditText>(R.id.player2NameInput)

        player1NameInput.text.clear()
        player2NameInput.text.clear()
        isPvC = null
        unhighlightButton(pvpButton)
        unhighlightButton(pvcButton)
        player1NameInput.visibility = View.GONE
        player2NameInput.visibility = View.GONE
    }

    // LLM ChatGPT-4o helped to implement onSaveInstanceState for rotation
    // prompt: "how can i make the app state persist when i rotate the screen?"
    override fun onSaveInstanceState(outState: Bundle) {
        // save the game mode state for screen rotation
        super.onSaveInstanceState(outState)

        val player1NameInput = findViewById<EditText>(R.id.player1NameInput)
        val player2NameInput = findViewById<EditText>(R.id.player2NameInput)

        outState.putString("PLAYER_X_NAME", player1NameInput.text.toString())  // save player's names
        outState.putString("PLAYER_O_NAME", player2NameInput.text.toString())
        outState.putBoolean("IS_PVC_SELECTED", isPvC == true) // save game mode
    }



    private fun highlightSelectedButton(button: Button) {
        button.isSelected = true  // to highlight selected button(selected game mode) with blue color
    }
    private fun unhighlightButton(button: Button) {
        button.isSelected = false // opposite - unhighlight
    }


    private fun startGame(playerName: String) {     // function that starts the game with PvC (1 player) mode
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_MODE", "PvC")
            putExtra("PLAYER_NAME", playerName)
        }
        startActivity(intent)
    }

    private fun startGame(player1Name: String, player2Name: String) {   // function that starts the game with PvP (2 players) mode
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_MODE", "PvP")    // intent data to GameActivity, game mode
            putExtra("PLAYER_X_NAME", player1Name) // and player's names
            putExtra("PLAYER_O_NAME", player2Name)
        }
        startActivity(intent)   // start the game(activity) with intent
    }

}