package com.example.petto.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.petto.ui.Login.Login
import com.example.petto.R
import com.example.petto.ui.SignUp.SignUp1

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        viewPager.adapter = IntroAdabter(getIntroItems(),
            onSignUpClick = {
                startActivity(Intent(this, SignUp1::class.java))
            },
            onLoginClick = {
                startActivity(Intent(this, Login::class.java)) // Make sure Login activity exists
            }
        )
    }

    private fun getIntroItems(): List<IntroItem> {
        return listOf(
            IntroItem("Pet Profile", "Description...", R.drawable.pet, R.drawable.base1),
            IntroItem("Reminder Notifications", "Description...", R.drawable.notification, R.drawable.base1),
            IntroItem("Posting", "Description...", R.drawable.post, R.drawable.base1),
            IntroItem("Nearby Pet Services", "Description...", R.drawable.sitting, R.drawable.base1)
        )
    }
}
