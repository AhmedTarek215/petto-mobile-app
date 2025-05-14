package com.example.petto.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.Post
import com.example.petto.ui.profiles.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MyPostsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var createPostButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        recyclerView = findViewById(R.id.recyclerViewMyPosts)
        progressBar = findViewById(R.id.progressBarMyPosts)
        emptyStateText = findViewById(R.id.emptyStateText)
        createPostButton = findViewById(R.id.createFirstPostBtn)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.myPostsToolbar)
        toolbar.title = "My Posts"
        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(emptyList())
        recyclerView.adapter = postAdapter

        createPostButton.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        loadMyPosts()
    }

    private fun loadMyPosts() {
        progressBar.visibility = View.VISIBLE
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.mapNotNull { doc ->
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    post
                }.sortedByDescending { it.timestamp }

                if (posts.isEmpty()) {
                    emptyStateText.visibility = View.VISIBLE
                    createPostButton.visibility = View.VISIBLE
                } else {
                    postAdapter.updatePosts(posts)
                    emptyStateText.visibility = View.GONE
                    createPostButton.visibility = View.GONE
                }

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load your posts", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
    override fun onResume() {
        super.onResume()
        loadMyPosts() // ✅ Refresh the post list when returning from EditPostActivity
    }
}
