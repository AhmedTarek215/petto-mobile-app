package com.example.petto.ui.post

import android.os.Bundle
import android.text.format.DateUtils
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
    private lateinit var profileImage: ImageView
    private lateinit var timePosted: TextView
    private lateinit var likeButton: ImageView
    private lateinit var likeCountText: TextView
    private lateinit var commentButton: ImageView
    private lateinit var commentCountText: TextView

    private lateinit var postId: String
    private var postOwnerId: String = ""
    private var isLiked = false

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
            finish()
        }

        postUsername = findViewById(R.id.username)
        postContent = findViewById(R.id.postText)
        postMedia = findViewById(R.id.postMedia)
        profileImage = findViewById(R.id.profileImage)
        timePosted = findViewById(R.id.timePosted)
        likeButton = findViewById(R.id.likeButton)
        likeCountText = findViewById(R.id.likeCountText)
        commentButton = findViewById(R.id.commentButton)
        commentCountText = findViewById(R.id.commentCountText)

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentInput = findViewById(R.id.commentInput)
        sendButton = findViewById(R.id.sendButton)

        commentAdapter = CommentAdapter(emptyList())
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        loadPostContent()
        loadComments()

        val shouldHighlight = intent.getBooleanExtra("highlightCommentInput", false)
        if (shouldHighlight) {
            commentInput.requestFocus()
            commentInput.setBackgroundResource(R.drawable.highlight_comment_input)
            commentInput.postDelayed({
                commentInput.setBackgroundResource(android.R.color.transparent)
            }, 3000)
        }

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

                        firestore.collection("posts")
                            .document(postId)
                            .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))

                        val newCount = (commentCountText.text.toString().toIntOrNull() ?: 0) + 1
                        commentCountText.text = newCount.toString()

                        if (currentUser.uid != postOwnerId) {
                            sendCommentNotification(
                                postOwnerId,
                                currentUser.uid,
                                postId,
                                username,
                                profileImage
                            )
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun sendCommentNotification(
        postOwnerId: String,
        commenterId: String,
        postId: String,
        commenterName: String,
        profileImageUrl: String?
    ) {
        val notifData = hashMapOf(
            "type" to "comment",
            "text" to "$commenterName commented on your post",
            "timestamp" to Timestamp.now(),
            "senderId" to commenterId,
            "postId" to postId,
            "profileImage" to profileImageUrl
        )

        firestore.collection("notifications")
            .document(postOwnerId)
            .collection("items")
            .add(notifData)
    }

    private fun loadPostContent() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val postRef = firestore.collection("posts").document(postId)

        postRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                postOwnerId = document.getString("userId") ?: ""
                postUsername.text = document.getString("username") ?: "User"
                postContent.text = document.getString("content") ?: ""

                // ✅ FIX: Check both profileImage fields
                val profileUrl = document.getString("userProfileImage")
                    ?: document.getString("profileImageUrl")

                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(this).load(profileUrl).placeholder(R.drawable.profile).into(profileImage)
                } else {
                    profileImage.setImageResource(R.drawable.profile)
                }

                // ✅ Show formatted relative time
                val timestamp = document.getTimestamp("timestamp")
                if (timestamp != null) {
                    val relativeTime = DateUtils.getRelativeTimeSpanString(
                        timestamp.toDate().time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                    timePosted.text = relativeTime
                }

                val mediaUrl = document.getString("mediaUrl")
                if (!mediaUrl.isNullOrEmpty()) {
                    postMedia.visibility = ImageView.VISIBLE
                    Glide.with(this).load(mediaUrl).into(postMedia)
                } else {
                    postMedia.visibility = ImageView.GONE
                }

                val likes = document.getLong("likes")?.toInt() ?: 0
                val commentsCount = document.getLong("commentsCount")?.toInt() ?: 0
                likeCountText.text = likes.toString()
                commentCountText.text = commentsCount.toString()

                postRef.collection("likes").document(userId).get()
                    .addOnSuccessListener { likeDoc ->
                        isLiked = likeDoc.exists()
                        likeButton.setImageResource(if (isLiked) R.drawable.heart_filled else R.drawable.heart)
                    }

                likeButton.setOnClickListener {
                    handleLikeToggle(userId, postRef)
                }
            }
        }
    }


    private fun handleLikeToggle(userId: String, postRef: com.google.firebase.firestore.DocumentReference) {
        val likeDocRef = postRef.collection("likes").document(userId)

        if (isLiked) {
            likeDocRef.delete().addOnSuccessListener {
                postRef.update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                val newLikes = (likeCountText.text.toString().toIntOrNull() ?: 1) - 1
                likeCountText.text = newLikes.toString()
                likeButton.setImageResource(R.drawable.heart)
                isLiked = false
            }
        } else {
            val likeData = mapOf("timestamp" to Timestamp.now())
            likeDocRef.set(likeData).addOnSuccessListener {
                postRef.update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                val newLikes = (likeCountText.text.toString().toIntOrNull() ?: 0) + 1
                likeCountText.text = newLikes.toString()
                likeButton.setImageResource(R.drawable.heart_filled)
                isLiked = true
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
