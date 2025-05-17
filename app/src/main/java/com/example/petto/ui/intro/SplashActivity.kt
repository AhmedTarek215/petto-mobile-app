package com.example.petto.ui.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("PettoPrefs", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null) {
                startActivity(Intent(this, com.example.petto.ui.HomeActivity::class.java))
            } else {
                startActivity(Intent(this, com.example.petto.ui.intro.IntroActivity::class.java))
            }
            finish()
        }, 3000) // 3-second splash delay
    }

    /*private fun navigateToLastScreen(screen: String?) {
        val intent = when (screen) {
            "Home" -> Intent(this, com.example.petto.ui.HomeActivity::class.java)
            "UserProfile" -> Intent(this, com.example.petto.ui.profiles.UserProfile::class.java)
            "PetProfile" -> Intent(this, com.example.petto.ui.profiles.PetProfile::class.java)
            "Posts" -> Intent(this, com.example.petto.ui.post.PostListActivity::class.java)
            "Comments" -> Intent(this, com.example.petto.ui.post.CommentsActivity::class.java)
            "CreatePost" -> Intent(this, com.example.petto.ui.post.CreatePostActivity::class.java)
            "MyPosts" -> Intent(this, com.example.petto.ui.post.MyPostsActivity::class.java)
            "EditPost" -> Intent(this, com.example.petto.ui.post.EditPostActivity::class.java)
            "Calender" -> Intent(this, com.example.petto.ui.calender.calender::class.java)
            "AddReminder" -> Intent(this, com.example.petto.ui.calender.AddReminder::class.java)
            "Notifications" -> Intent(this, com.example.petto.ui.notification.NotificationActivity::class.java)
            "Tips" -> Intent(this, com.example.petto.ui.tips.TipsActivity::class.java)
            "ServiceProfile" -> Intent(this, com.example.petto.ui.Services.ServiceProfile::class.java)
            "ServicesPage" -> Intent(this, com.example.petto.ui.Services.ServicesPage::class.java)
            else -> Intent(this, com.example.petto.ui.HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }*/
}
