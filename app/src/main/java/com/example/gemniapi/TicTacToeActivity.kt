package com.example.gemniapi

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.gemniapi.databinding.ActivityTicTacToeBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TicTacToeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicTacToeBinding
    private lateinit var buttons: Array<Array<Button>>
    private lateinit var gameBoard: Array<Array<String>>
    private var gameActive = true
    private var moveCount = 0
    private var winningLine: List<Pair<Int, Int>>? = null

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityTicTacToeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGame()
        setupClickListeners()
    }

    private fun setupGame() {
        gameBoard = Array(3) { Array(3) { "" } }
        buttons = Array(3) { row ->
            Array(3) { col ->
                Button(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(8, 8, 8, 8)
                    }
                    minimumHeight = resources.getDimensionPixelSize(R.dimen.button_min_height)
                    textSize = 24f
                    gravity = Gravity.CENTER
                    setBackgroundResource(R.drawable.game_cell_background)
                    setOnClickListener { handleCellClick(row, col) }
                }
            }
        }

        buttons.forEach { row ->
            LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                weightSum = 3f
                row.forEach { button -> addView(button) }
                binding.boardLayout.addView(this)
            }
        }

        updateStatus("Your turn! (Move 1)")
    }

    private fun setupClickListeners() {
        binding.btnNewGame.setOnClickListener {
            resetGame()
        }
    }

    private fun handleCellClick(row: Int, col: Int) {
        if (!gameActive || gameBoard[row][col].isNotEmpty()) return

        lifecycleScope.launch {
            // Player move
            makeMove(row, col, "X")
            moveCount++

            // Check for player win
            if (checkGameEnd("X")) {
                highlightWinningLine()
                showGameOverDialog("You win! 🎉")
                return@launch
            }

            if (isBoardFull()) {
                showGameOverDialog("It's a draw! 🤝")
                return@launch
            }

            // AI move
            updateStatus("AI is thinking... 🤔")
            delay(500) // Add a small delay to make AI feel more natural

            val aiMove = getAIMove()
            try {
                val (aiRow, aiCol) = aiMove.split(",").map { it.trim().toInt() }
                if (isValidMove(aiRow, aiCol)) {
                    makeMove(aiRow, aiCol, "O")
                    moveCount++

                    // Check for AI win
                    if (checkGameEnd("O")) {
                        highlightWinningLine()
                        showGameOverDialog("AI wins! 🤖")
                        return@launch
                    }

                    if (isBoardFull()) {
                        showGameOverDialog("It's a draw! 🤝")
                        return@launch
                    }

                    updateStatus("Your turn! (Move ${moveCount/2 + 1})")
                } else {
                    makeStrategicMove()
                }
            } catch (e: Exception) {
                makeStrategicMove()
            }
        }
    }

    private fun makeMove(row: Int, col: Int, symbol: String) {
        gameBoard[row][col] = symbol
        buttons[row][col].text = symbol
    }

    private suspend fun getAIMove(): String {
        val prompt = """
            You are an expert Tic Tac Toe AI player (O) facing a human opponent (X).
            Current board state (move ${moveCount/2 + 1}):
            ${getBoardString()}
            
            Analyze the board carefully and make the optimal move following these priorities:
            1. Win immediately if possible
            2. Block opponent's winning move
            3. Create a fork (multiple winning possibilities)
            4. Block opponent's potential fork
            5. Take center if available (position 1,1)
            6. Take opposite corner if opponent has a corner
            7. Take empty corner
            8. Take empty side
            
            Additional strategic considerations:
            - If it's early game (moves 1-2), prioritize corner control
            - In mid-game (moves 3-5), look for fork opportunities
            - In late game (moves 6+), focus on forcing moves
            - Consider setting up multi-line threats
            - Watch for opponent's trap setups
            
            Game state analysis:
            - Winning lines available: ${analyzeWinningLines()}
            - Open corners: ${getOpenCorners()}
            - Center status: ${if(gameBoard[1][1].isEmpty()) "open" else "taken"}
            
            Respond ONLY with move coordinates (row,col). Example: "1,1" for center.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.toString()?.trim() ?: getStrategicMove()
        } catch (e: Exception) {
            getStrategicMove()
        }
    }

    private fun analyzeWinningLines(): String {
        val lines = mutableListOf<String>()

        // Check rows
        for (i in 0..2) {
            val row = gameBoard[i].joinToString("")
            if (row.count { it == 'O' } == 2 && row.contains("")) lines.add("row $i")
            if (row.count { it == 'X' } == 2 && row.contains("")) lines.add("opponent row $i")
        }

        // Check columns
        for (i in 0..2) {
            val col = (0..2).map { gameBoard[it][i] }.joinToString("")
            if (col.count { it == 'O' } == 2 && col.contains("")) lines.add("column $i")
            if (col.count { it == 'X' } == 2 && col.contains("")) lines.add("opponent column $i")
        }

        return if (lines.isEmpty()) "none" else lines.joinToString(", ")
    }

    private fun getOpenCorners(): String {
        val corners = listOf(Pair(0,0), Pair(0,2), Pair(2,0), Pair(2,2))
        return corners.filter { (row, col) -> gameBoard[row][col].isEmpty() }
            .map { (row, col) -> "($row,$col)" }
            .joinToString(", ")
    }

    private fun makeStrategicMove() {
        if (!gameActive) return

        // Priority 1: Win if possible
        if (findWinningMove("O")) return

        // Priority 2: Block opponent's win
        if (findWinningMove("X")) return

        // Priority 3: Take center
        if (gameBoard[1][1].isEmpty()) {
            makeMove(1, 1, "O")
            return
        }

        // Priority 4: Take corner
        val corners = listOf(Pair(0,0), Pair(0,2), Pair(2,0), Pair(2,2))
        for ((row, col) in corners.shuffled()) {
            if (gameBoard[row][col].isEmpty()) {
                makeMove(row, col, "O")
                return
            }
        }

        // Priority 5: Take any available move
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameBoard[i][j].isEmpty()) {
                    makeMove(i, j, "O")
                    return
                }
            }
        }
    }
    private fun findWinningMove(symbol: String): Boolean {
        // Check for winning move in rows and columns
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameBoard[i][j].isEmpty()) {
                    gameBoard[i][j] = symbol
                    if (checkWinner(symbol)) {
                        gameBoard[i][j] = ""
                        makeMove(i, j, "O")
                        return true
                    }
                    gameBoard[i][j] = ""
                }
            }
        }
        return false
    }

    private fun getStrategicMove(): String {
        // Priority 1: Win if possible
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameBoard[i][j].isEmpty()) {
                    gameBoard[i][j] = "O"
                    if (checkWinner("O")) {
                        gameBoard[i][j] = ""
                        return "$i,$j"
                    }
                    gameBoard[i][j] = ""
                }
            }
        }

        // Priority 2: Block opponent
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameBoard[i][j].isEmpty()) {
                    gameBoard[i][j] = "X"
                    if (checkWinner("X")) {
                        gameBoard[i][j] = ""
                        return "$i,$j"
                    }
                    gameBoard[i][j] = ""
                }
            }
        }

        // Priority 3: Center
        if (gameBoard[1][1].isEmpty()) return "1,1"

        // Priority 4: Corners
        val corners = listOf(Pair(0,0), Pair(0,2), Pair(2,0), Pair(2,2))
        for ((row, col) in corners.shuffled()) {
            if (gameBoard[row][col].isEmpty()) return "$row,$col"
        }

        // Priority 5: Any available move
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameBoard[i][j].isEmpty()) return "$i,$j"
            }
        }

        return "1,1" // Fallback
    }

    private fun getBoardString(): String {
        return buildString {
            for (i in 0..2) {
                for (j in 0..2) {
                    append(if (gameBoard[i][j].isEmpty()) "." else gameBoard[i][j])
                    if (j < 2) append("|")
                }
                if (i < 2) append("\n-+-+-\n")
            }
        }
    }

    private fun isValidMove(row: Int, col: Int): Boolean {
        return row in 0..2 && col in 0..2 && gameBoard[row][col].isEmpty()
    }

    private fun checkWinner(symbol: String): Boolean {
        // Check rows
        for (i in 0..2) {
            if ((0..2).all { gameBoard[i][it] == symbol }) {
                winningLine = (0..2).map { Pair(i, it) }
                return true
            }
        }

        // Check columns
        for (i in 0..2) {
            if ((0..2).all { gameBoard[it][i] == symbol }) {
                winningLine = (0..2).map { Pair(it, i) }
                return true
            }
        }

        // Check diagonal
        if ((0..2).all { gameBoard[it][it] == symbol }) {
            winningLine = (0..2).map { Pair(it, it) }
            return true
        }

        // Check anti-diagonal
        if ((0..2).all { gameBoard[it][2-it] == symbol }) {
            winningLine = (0..2).map { Pair(it, 2-it) }
            return true
        }

        return false
    }

    private fun highlightWinningLine() {
        winningLine?.forEach { (row, col) ->
            buttons[row][col].setBackgroundColor(Color.parseColor("#4CAF50")) // Green highlight
        }
    }

    private fun isBoardFull(): Boolean {
        return gameBoard.all { row -> row.all { it.isNotEmpty() } }
    }

    private fun checkGameEnd(symbol: String): Boolean {
        if (checkWinner(symbol)) {
            gameActive = false
            return true
        }
        if (isBoardFull()) {
            gameActive = false
            return true
        }
        return false
    }

    private fun showGameOverDialog(message: String) {
        gameActive = false
        updateStatus("Game Over - $message")

        lifecycleScope.launch {
            delay(500) // Small delay to show the winning line
            AlertDialog.Builder(this@TicTacToeActivity)
                .setTitle("Game Over")
                .setMessage(message)
                .setIcon(if (message.contains("AI wins")) R.drawable.ic_robot else R.drawable.ic_trophy)
                .setPositiveButton("Play Again") { _, _ ->
                    resetGame()
                }
                .setNegativeButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    private fun updateStatus(message: String) {
        binding.tvStatus.text = message
    }

    private fun resetGame() {
        gameBoard = Array(3) { Array(3) { "" } }
        buttons.forEach { row ->
            row.forEach { button ->
                button.text = ""
                button.setBackgroundResource(R.drawable.game_cell_background)
            }
        }
        gameActive = true
        moveCount = 0
        winningLine = null
        updateStatus("Your turn! (Move 1)")
    }

    override fun onBackPressed() {
        if (!gameActive) {
            super.onBackPressed()
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            // Show confirmation dialog if game is in progress
            AlertDialog.Builder(this)
                .setTitle("Exit Game?")
                .setMessage("Are you sure you want to exit? Current game progress will be lost.")
                .setPositiveButton("Exit") { _, _ ->
                    super.onBackPressed()
                    finish()
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                .setNegativeButton("Continue Playing") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
