package com.example.petto.ui.profiles

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // go back
        }
    }
}
