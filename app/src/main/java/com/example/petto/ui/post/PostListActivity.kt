package com.example.petto.ui.post

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.google.firebase.firestore.ListenerRegistration
import com.example.petto.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView

    private val firestore = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        recyclerView = findViewById(R.id.PostsRecyclerView)
        progressBar = findViewById(R.id.loadingProgressBar)
        backButton = findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(emptyList())
        recyclerView.adapter = postAdapter

        setupRealTimePosts()

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRealTimePosts() {
        progressBar.visibility = View.VISIBLE

        snapshotListener = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, "Failed to listen to posts", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    return@addSnapshotListener
                }

                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.apply { id = doc.id }
                }

                postAdapter.updatePosts(posts)
                progressBar.visibility = View.GONE
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }
}
