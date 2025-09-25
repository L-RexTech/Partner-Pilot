package com.example.gemniapi

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.URL
import kotlin.random.Random

class DailyTipActivity : AppCompatActivity() {
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var backgroundLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_tip)

        quoteTextView = findViewById(R.id.quoteTextView)
        authorTextView = findViewById(R.id.authorTextView)
        backgroundLayout = findViewById(R.id.backgroundLayout)

        fetchRandomQuote()
        setRandomBackground()
    }

    private fun fetchRandomQuote() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL("https://zenquotes.io/api/random").readText()
                val jsonArray = JSONArray(response)
                val jsonObject = jsonArray.getJSONObject(0)
                val quote = jsonObject.getString("q")
                val author = jsonObject.getString("a")

                withContext(Dispatchers.Main) {
                    quoteTextView.text = quote
                    authorTextView.text = "- $author"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    quoteTextView.text = "Failed to fetch quote"
                    authorTextView.text = ""
                }
            }
        }
    }

    private fun setRandomBackground() {
        val backgrounds = arrayOf(
            R.drawable.background1,
            R.drawable.background2,
            R.drawable.background3
        )
        backgroundLayout.setBackgroundResource(backgrounds[Random.nextInt(backgrounds.size)])
    }
}