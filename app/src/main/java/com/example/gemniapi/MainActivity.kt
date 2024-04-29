package com.example.gemniapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.gemniapi.database.ChatMessage
import com.example.gemniapi.database.ChatMessagesDatabase
import com.example.gemniapi.databinding.ActivityMainBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: ChatMessagesDatabase
    private lateinit var adapter: ChatAdapter
    private var chats = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        adapter = ChatAdapter(binding.rvChat, mutableListOf())
        binding.rvChat.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        binding.rvChat.adapter = adapter


        val apiKey = BuildConfig.apiKey
        val generativeModel = GenerativeModel(
            // Use a model that's applicable for your use case (see "Implement basic use cases" below)
            modelName = "gemini-pro",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = apiKey
        )

        thread {
            database = Room.databaseBuilder(
                this@MainActivity,
                ChatMessagesDatabase::class.java,
                "chatMessagesDB"
            ).build()

            chats = database.chatMessagesDao().getAllChatMessages()
            adapter.updateData(chats)
        }

        binding.btnSend.setOnClickListener {
            val prompt = binding.tietPrompt.text
            val promptText: String = prompt.toString()
            Log.d("MYAPP", "Prompt details : $promptText")

            if (promptText.isNullOrBlank()) {
                Toast.makeText(this@MainActivity, "Please enter a prompt!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    var chat = ChatMessage(text = prompt.toString(), sender = "Me", timeStamp = System.currentTimeMillis())
                    database.chatMessagesDao().insert(chat)
                    adapter.addData(chat)
                    binding.tietPrompt.text!!.clear()

                    val response = generativeModel.generateContent(promptText)

                    chat = ChatMessage(text = response.text.toString(), sender = "Gemini", timeStamp = System.currentTimeMillis())
                    database.chatMessagesDao().insert(chat)
                    adapter.addData(chat)

                } catch (e : Exception) {
                    Toast.makeText(this@MainActivity, "Cannot generate text with this prompt", Toast.LENGTH_LONG).show()
                }
            }
        }





    }
}