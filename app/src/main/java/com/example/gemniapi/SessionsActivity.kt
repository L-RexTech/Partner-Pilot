package com.example.gemniapi

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gemniapi.database.AppDatabase
import com.example.gemniapi.database.ChatSession
import com.example.gemniapi.databinding.ActivitySessionsBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date

class SessionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionsBinding
    private lateinit var database: AppDatabase
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySessionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar) // Make sure you have a toolbar in your layout
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userEmail = intent.getStringExtra("USER_EMAIL") ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }


        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        observeSessions()
        setupClickListeners()

    }


    private fun setupRecyclerView() {
        sessionAdapter = SessionAdapter(
            onClick = { session ->
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra("SESSION_ID", session.id)
                    putExtra("SESSION_TITLE", session.title)
                })
            },
            onExport = { session -> exportSession(session) }
        )

        binding.rvSessions.apply {
            layoutManager = LinearLayoutManager(this@SessionsActivity)
            adapter = sessionAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabNewSession.setOnClickListener {
            showNewSessionDialog()
        }
    }

    private fun observeSessions() {
        lifecycleScope.launch {
            database.chatSessionDao().getUserSessions(userEmail).collect { sessions ->
                sessionAdapter.submitList(sessions)
                binding.tvNoSessions.isVisible = sessions.isEmpty()
            }
        }
    }

    private fun showNewSessionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_session, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.etSessionTitle)

        AlertDialog.Builder(this)
            .setTitle("New Chat Session")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = titleEditText.text.toString()
                if (title.isNotBlank()) {
                    lifecycleScope.launch {
                        val session = ChatSession(
                            userEmail = userEmail,
                            title = title
                        )
                        database.chatSessionDao().createSession(session)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportSession(session: ChatSession) {
        lifecycleScope.launch {
            val messages = database.chatMessagesDao().getSessionMessagesForExport(session.id)
            val exportText = buildString {
                appendLine("Chat Session: ${session.title}")
                appendLine("Date: ${Date(session.createdAt)}")
                appendLine("-------------------")
                messages.forEach { message ->
                    appendLine("${message.sender}: ${message.text}")
                    appendLine()
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Chat Session - ${session.title}")
                putExtra(Intent.EXTRA_TEXT, exportText)
            }
            startActivity(Intent.createChooser(intent, "Export Session"))
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

