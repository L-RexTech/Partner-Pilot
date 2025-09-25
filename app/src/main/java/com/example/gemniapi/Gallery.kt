package com.example.gemniapi

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Gallery : AppCompatActivity() {

    data class TechItem(
        val imageResId: Int,
        val name: String,
        val url: String
    )

    private val techStack = listOf(
        TechItem(
            R.drawable.image1,
            "Angular",
            "https://angular.io/"
        ),
        TechItem(
            R.drawable.image2,
            "MetaMask",
            "https://metamask.io/"
        ),
        TechItem(
            R.drawable.image3,
            "Jira",
            "https://www.atlassian.com/software/jira"
        ),
        TechItem(
            R.drawable.image4,
            "Java",
            "https://www.java.com/"
        ),
        TechItem(
            R.drawable.image5,
            "Android Studio",
            "https://developer.android.com/studio"
        ),
        TechItem(
            R.drawable.image6,
            "Python",
            "https://www.python.org/"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)


        val backButton = findViewById<MaterialButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, SessionsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val gridLayout: GridLayout = findViewById(R.id.galleryGridLayout)

        for ((index, tech) in techStack.withIndex()) {
            val frameLayout = FrameLayout(this)
            frameLayout.layoutParams = GridLayout.LayoutParams().apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                setMargins(16, 16, 16, 16)
                rowSpec = GridLayout.spec(index / 3)
                columnSpec = GridLayout.spec(index % 3)
            }

            val drawable = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 20f
                setStroke(4, Color.LTGRAY)
            }

            val imageView = ImageView(this)
            imageView.setImageResource(tech.imageResId)
            imageView.layoutParams = FrameLayout.LayoutParams(
                300, 300
            ).apply {
                gravity = Gravity.CENTER
            }
            imageView.background = drawable
            imageView.elevation = 8f
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(16, 16, 16, 16)

            imageView.setOnClickListener {
                openTechWebsite(tech)
            }

            frameLayout.addView(imageView)
            gridLayout.addView(frameLayout)
        }
    }

    private fun openTechWebsite(tech: TechItem) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tech.url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open ${tech.name} website",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}