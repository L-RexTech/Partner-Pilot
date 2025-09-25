package com.example.gemniapi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import android.widget.CheckBox

class AboutUsActivity : AppCompatActivity() {
    private lateinit var emailTextView: TextView
    private lateinit var gratitudeEditText: TextInputEditText
    private lateinit var anonymousCheckBox: CheckBox
    private lateinit var submitButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_us)
        setupToolbar()
        initializeViews()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "About Us"
        }
    }

    private fun initializeViews() {
        emailTextView = findViewById(R.id.emailTextView)
        gratitudeEditText = findViewById(R.id.gratitudeEditText)
        anonymousCheckBox = findViewById(R.id.anonymousCheckBox)
        submitButton = findViewById(R.id.submitButton)
    }

    private fun setupClickListeners() {
        // Email click handler
        emailTextView.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("hellopk924@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Inquiry from App")
                putExtra(Intent.EXTRA_TEXT, "Please write your message here...")
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email using:"))
            } catch (e: Exception) {
                Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
            }
        }

        // Phone number click handler
        findViewById<TextView>(R.id.phoneTextView).setOnClickListener {
            val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:+911824404404")
            }
            startActivity(phoneIntent)
        }

        // Submit button handler
        submitButton.setOnClickListener {
            val message = gratitudeEditText.text.toString()
            if (message.isBlank()) {
                gratitudeEditText.error = "Please enter your message"
                return@setOnClickListener
            }

            val isAnonymous = anonymousCheckBox.isChecked
            // Here you can handle the submission of feedback
            // For now, just showing a toast
            Toast.makeText(
                this,
                "Thank you for your feedback!" + if (isAnonymous) " (Anonymous)" else "",
                Toast.LENGTH_SHORT
            ).show()

            // Clear the input after successful submission
            gratitudeEditText.text?.clear()
            anonymousCheckBox.isChecked = false
        }

        // Address click handler for opening in maps
        findViewById<TextView>(R.id.addressTextView).setOnClickListener {
            val locationUri = android.net.Uri.encode("Jalandhar - Delhi, Grand Trunk Rd, Phagwara, Punjab 144411")
            val mapIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=$locationUri"))
            mapIntent.setPackage("com.google.android.apps.maps")

            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Maps application not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}