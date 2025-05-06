package com.example.petto.ui.post

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.Comment
import com.example.petto.ui.adapters.CommentAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentsActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var postUsername: TextView
    private lateinit var postContent: TextView
    private lateinit var postMedia: ImageView

    private lateinit var postId: String
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        postId = intent.getStringExtra("postId") ?: ""

        if (postId.isEmpty()) {
            Toast.makeText(this, "Missing postId!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            val intent = Intent(this, PostListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish() // or navigate to PostListActivity explicitly if needed
        }





        postUsername = findViewById(R.id.postUsername)
        postContent = findViewById(R.id.postContent)
        postMedia = findViewById(R.id.postMedia)

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentInput = findViewById(R.id.commentInput)
        sendButton = findViewById(R.id.sendButton)

        commentAdapter = CommentAdapter(emptyList())
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        loadPostContent()
        loadComments()

        sendButton.setOnClickListener {
            val content = commentInput.text.toString().trim()
            val currentUser = auth.currentUser ?: return@setOnClickListener

            if (content.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection("Users").document(currentUser.uid).get().addOnSuccessListener { doc ->
                val username = doc.getString("firstName").orEmpty() + " " + doc.getString("lastName").orEmpty()
                val profileImage = doc.getString("profileImageUrl") ?: ""

                val comment = Comment(
                    userId = currentUser.uid,
                    username = username,
                    userProfileImage = profileImage,
                    content = content,
                    timestamp = Timestamp.now()
                )

                firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .add(comment)
                    .addOnSuccessListener {
                        commentInput.text.clear()
                        Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()

                        // ✅ Increment comments count
                        firestore.collection("posts")
                            .document(postId)
                            .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadPostContent() {
        firestore.collection("posts").document(postId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                postUsername.text = document.getString("username") ?: "User"
                postContent.text = document.getString("content") ?: ""

                val mediaUrl = document.getString("mediaUrl")
                if (!mediaUrl.isNullOrEmpty()) {
                    postMedia.visibility = ImageView.VISIBLE
                    Glide.with(this).load(mediaUrl).into(postMedia)
                } else {
                    postMedia.visibility = ImageView.GONE
                }
            }
        }
    }

    private fun loadComments() {
        firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val comments = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
                commentAdapter.updateComments(comments)
            }
    }

}
