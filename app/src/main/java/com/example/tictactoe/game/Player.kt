package com.example.tictactoe.game

enum class Player(symbol: Char) {           // representing players - X and O
    X('X'),
    O('O'),
    NONE(' ');

    fun nextPlayer(): Player = if (this == X) O else X // to get next player
}