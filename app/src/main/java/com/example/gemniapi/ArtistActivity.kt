package com.example.gemniapi
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.gemniapi.databinding.ActivityArtistBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ArtistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtistBinding
    private var score = 0
    private var isGenerating = false
    private lateinit var usageTracker: ApiUsageTracker

    // Direct API key
    private val API_KEY = "YOUR_HUGGING_FACE_TOKEN_HERE"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityArtistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usageTracker = ApiUsageTracker(this)
        setupClickListeners()
        setupGameView()
        updateRequestsLeftText()
    }

    private fun updateRequestsLeftText() {
        val (monthlyLeft, hourlyLeft) = usageTracker.getRemainingRequests()
        binding.requestsLeftText.text = "Requests left: $hourlyLeft/hr, $monthlyLeft/month"
    }

    private fun setupClickListeners() {
        binding.generateButton.setOnClickListener {
            if (!isGenerating) {
                val prompt = binding.promptInput.text.toString()
                if (prompt.isNotEmpty()) {
                    if (usageTracker.canMakeRequest()) {
                        startGeneration(prompt)
                    } else {
                        showError("Rate limit reached. Please try again later.")
                    }
                } else {
                    showError("Please enter a prompt")
                }
            }
        }

        binding.target.setOnClickListener {
            if (isGenerating) {
                score++
                binding.scoreText.text = "Score: $score"
                moveTarget()
            }
        }
    }

    private fun setupGameView() {
        binding.apply {
            gameLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            errorText.visibility = View.GONE
        }
    }

    private fun startGeneration(prompt: String) {
        isGenerating = true
        score = 0
        binding.apply {
            gameLayout.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            generateButton.isEnabled = false
            errorText.visibility = View.GONE
            resultImage.setImageDrawable(null)
        }
        moveTarget()

        try {
            val jsonBody = JSONObject().apply {
                put("inputs", prompt)
                put("wait_for_model", true)
                put("height", 512)
                put("width", 512)
            }.toString()

            val request = Request.Builder()
                .url("https://api-inference.huggingface.co/models/runwayml/stable-diffusion-v1-5")
                .addHeader("Authorization", "Bearer $API_KEY")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            usageTracker.incrementRequestCount()
            updateRequestsLeftText()
            makeRequestWithRetry(request, maxRetries = 3)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error creating request: ${e.message}", e)
            showError("Error creating request: ${e.message}")
            endGeneration()
        }
    }

    private fun makeRequestWithRetry(request: Request, maxRetries: Int, currentRetry: Int = 0) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Attempt ${currentRetry + 1} failed: ${e.message}", e)

                if (currentRetry < maxRetries) {
                    val delay = (Math.pow(2.0, currentRetry.toDouble()) * 1000).toLong()
                    android.os.Handler(mainLooper).postDelayed({
                        makeRequestWithRetry(request, maxRetries, currentRetry + 1)
                    }, delay)
                } else {
                    runOnUiThread {
                        showError("Failed after $maxRetries attempts: ${e.message}")
                        endGeneration()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"

                        if (errorBody.contains("loading") && currentRetry < maxRetries) {
                            android.os.Handler(mainLooper).postDelayed({
                                makeRequestWithRetry(request, maxRetries, currentRetry + 1)
                            }, 3000)
                            return
                        }

                        runOnUiThread {
                            showError("API Error: ${response.code} - $errorBody")
                            endGeneration()
                        }
                        return
                    }

                    val imageBytes = response.body?.bytes()
                    if (imageBytes != null) {
                        runOnUiThread {
                            Glide.with(this@ArtistActivity)
                                .load(imageBytes)
                                .into(binding.resultImage)
                            endGeneration()
                        }
                    } else {
                        runOnUiThread {
                            showError("No image data received")
                            endGeneration()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Error processing response: ${e.message}", e)
                    runOnUiThread {
                        showError("Error processing response: ${e.message}")
                        endGeneration()
                    }
                } finally {
                    response.close()
                }
            }
        })
    }

    private fun showError(message: String) {
        binding.errorText.apply {
            text = message
            visibility = View.VISIBLE
        }
        Log.e("APP_ERROR", message)
    }

    private fun endGeneration() {
        isGenerating = false
        binding.apply {
            gameLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            generateButton.isEnabled = true
        }
        showScore()
    }

    private fun showScore() {
        if (score > 0) {
            Toast.makeText(this, "Final Score: $score", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveTarget() {
        val parentWidth = binding.gameLayout.width
        val parentHeight = binding.gameLayout.height
        if (parentWidth <= 0 || parentHeight <= 0) return

        val targetSize = binding.target.width
        val newX = Random.nextInt(parentWidth - targetSize)
        val newY = Random.nextInt(parentHeight - targetSize)

        binding.target.animate()
            .x(newX.toFloat())
            .y(newY.toFloat())
            .setDuration(0)
            .start()

        val colorAnimator = ValueAnimator.ofArgb(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW
        )
        colorAnimator.duration = 1000
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.interpolator = LinearInterpolator()
        colorAnimator.addUpdateListener { animator ->
            binding.target.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimator.start()
    }
}

class ApiUsageTracker(context: Context) {
    private val prefs = context.getSharedPreferences("api_usage", Context.MODE_PRIVATE)

    fun canMakeRequest(): Boolean {
        checkAndResetCounters()
        return getHourlyRequests() < 150 && getMonthlyRequests() < 30000
    }

    fun incrementRequestCount() {
        prefs.edit().apply {
            putInt("hourly_requests", getHourlyRequests() + 1)
            putInt("monthly_requests", getMonthlyRequests() + 1)
            putLong("last_request_time", System.currentTimeMillis())
            apply()
        }
    }

    fun getRemainingRequests(): Pair<Int, Int> {
        checkAndResetCounters()
        val monthlyLeft = 30000 - getMonthlyRequests()
        val hourlyLeft = 150 - getHourlyRequests()
        return Pair(monthlyLeft, hourlyLeft)
    }

    private fun getHourlyRequests(): Int = prefs.getInt("hourly_requests", 0)
    private fun getMonthlyRequests(): Int = prefs.getInt("monthly_requests", 0)

    private fun checkAndResetCounters() {
        val currentTime = System.currentTimeMillis()
        val lastRequestTime = prefs.getLong("last_request_time", 0)

        if (currentTime - lastRequestTime > 3600000) {
            prefs.edit().putInt("hourly_requests", 0).apply()
        }

        if (currentTime - lastRequestTime > 2592000000) {
            prefs.edit().putInt("monthly_requests", 0).apply()
        }
    }
}
