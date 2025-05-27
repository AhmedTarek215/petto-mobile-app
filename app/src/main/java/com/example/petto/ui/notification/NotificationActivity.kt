package com.example.petto.ui.notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView

    // Bottom navigation icons
    private lateinit var navHome: ImageView
    private lateinit var navCalendar: ImageView
    private lateinit var navProfile: ImageView
    private lateinit var navNotifications: ImageView
    private lateinit var fab: ImageView

    private val notifications = mutableListOf<NotificationItem>()
    private val adapter = NotificationsAdapter(notifications)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        recyclerView = findViewById(R.id.PostsRecyclerView)
        progressBar = findViewById(R.id.loadingProgressBar)
        backButton = findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupNavigation()
        loadNotifications()
    }

    private fun setupNavigation() {
        navHome = findViewById(R.id.nav_home)
        navCalendar = findViewById(R.id.nav_calendar)
        navNotifications = findViewById(R.id.nav_notifications)
        navProfile = findViewById(R.id.nav_profile)
        fab = findViewById(R.id.fab)

        navHome.setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.HomeActivity::class.java))
            finish()
        }

        navCalendar.setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.calender.calender::class.java))
            finish()
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.profiles.UserProfile::class.java))
            finish()
        }

        navNotifications.setOnClickListener {
            // You're already here, optionally show a toast
            Toast.makeText(this, "You're on Notifications", Toast.LENGTH_SHORT).show()
        }

        fab.setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.post.CreatePostActivity::class.java))
        }
    }

    private fun loadNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE
        Log.d("NotifDebug", "Current UID: $userId")

        FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(userId)
            .collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                notifications.clear()
                for (doc in result) {
                    val item = doc.toObject(NotificationItem::class.java)
                    notifications.add(item)
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
}
