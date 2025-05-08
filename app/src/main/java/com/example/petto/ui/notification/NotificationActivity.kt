package com.example.petto.ui.notification

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
import com.example.petto.ui.notification.NotificationsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView

    private val notifications = mutableListOf<NotificationItem>()
    private val adapter = NotificationsAdapter(notifications)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification) // Make sure this is your correct XML layout name

        recyclerView = findViewById(R.id.PostsRecyclerView)
        progressBar = findViewById(R.id.loadingProgressBar)
        backButton = findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadNotifications()
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
