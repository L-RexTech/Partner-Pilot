package com.example.gemniapi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gemniapi.database.AppDatabase
import com.example.gemniapi.database.ChatMessage
import com.example.gemniapi.databinding.ActivityChatBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var database: AppDatabase
    private lateinit var chatAdapter: ChatAdapter
    private var sessionId: Int = -1
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey
    )

    // Easter egg tracking
    private var lastButtonClickTime = 0L
    private var consecutiveClicks = 0
    private val CLICK_THRESHOLD = 500
    private val REQUIRED_CLICKS = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionId = intent.getIntExtra("SESSION_ID", -1)
        val sessionTitle = intent.getStringExtra("SESSION_TITLE") ?: "Chat"

        if (sessionId == -1) {
            finish()
            return
        }

        supportActionBar?.title = sessionTitle
        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        observeMessages()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener { view ->
            val currentTime = System.currentTimeMillis()
            val prompt = binding.tietPrompt.text.toString()

            // Check for artist command
            if (prompt == "artist🎨") {
                startActivity(Intent(this, ArtistActivity::class.java))
                binding.tietPrompt.text?.clear()
                return@setOnClickListener
            }

            // Easter egg check
            if (currentTime - lastButtonClickTime < CLICK_THRESHOLD) {
                consecutiveClicks++
                if (consecutiveClicks >= REQUIRED_CLICKS) {
                    val intent = Intent(this, TicTacToeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    consecutiveClicks = 0
                    return@setOnClickListener
                }
            } else {
                consecutiveClicks = 1
            }
            lastButtonClickTime = currentTime

            // Original chat functionality
            if (prompt.isBlank()) {
                Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val userMessage = ChatMessage(
                        sessionId = sessionId,
                        text = prompt,
                        sender = "Me"
                    )
                    database.chatMessagesDao().insert(userMessage)
                    binding.tietPrompt.text?.clear()

                    val response = generativeModel.generateContent(prompt)
                    val aiMessage = ChatMessage(
                        sessionId = sessionId,
                        text = response.text ?: "Error generating response",
                        sender = "Gemini"
                    )
                    database.chatMessagesDao().insert(aiMessage)
                } catch (e: Exception) {
                    Toast.makeText(this@ChatActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { message ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Chat Message", message.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show()
            true
        }

        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            database.chatMessagesDao().getSessionMessages(sessionId).collect { messages ->
                chatAdapter.submitList(messages)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_daily_tip -> {
                startActivity(Intent(this, DailyTipActivity::class.java))
                true
            }
            R.id.action_gallery -> {
                startActivity(Intent(this, Gallery::class.java))
                true
            }
            R.id.action_about_us -> {
                startActivity(Intent(this, AboutUsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}