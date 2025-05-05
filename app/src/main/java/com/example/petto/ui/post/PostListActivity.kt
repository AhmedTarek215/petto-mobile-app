package com.example.petto.ui.post

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var progressBar: ProgressBar
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        recyclerView = findViewById(R.id.PostsRecyclerView)
        progressBar = findViewById(R.id.loadingProgressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(emptyList())
        recyclerView.adapter = postAdapter

        loadPosts()
    }

    private fun loadPosts() {
        progressBar.visibility = android.view.View.VISIBLE

        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.mapNotNull { doc ->
                    val post = doc.toObject(Post::class.java)
                    post.copy(id = doc.id) // ✅ Attach document ID to each post
                }
                postAdapter.updatePosts(posts)
                progressBar.visibility = android.view.View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
                progressBar.visibility = android.view.View.GONE
            }
    }
}
