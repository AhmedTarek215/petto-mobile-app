package com.example.petto.ui.post

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.profileImage)
        val username: TextView = view.findViewById(R.id.username)
        val timePosted: TextView = view.findViewById(R.id.timePosted)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val postText: TextView = view.findViewById(R.id.postText)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val likeCountText: TextView = view.findViewById(R.id.likeCountText)
        val commentButton: ImageView = view.findViewById(R.id.commentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.username.text = post.username.ifEmpty { "Unknown" }

        if (!post.userProfileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.userProfileImage)
                .placeholder(R.drawable.profile)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.profile)
        }

        if (!post.content.isNullOrEmpty()) {
            holder.postText.visibility = View.VISIBLE
            holder.postText.text = post.content
        } else {
            holder.postText.visibility = View.GONE
        }

        if (!post.mediaUrl.isNullOrEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.mediaUrl)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.timePosted.text = getFormattedTime(post.timestamp)
        holder.likeCountText.text = post.likes.toString()

        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val postId = post.id
        val userId = currentUser?.uid

        if (postId.isNotEmpty() && userId != null) {
            val postRef = firestore.collection("posts").document(postId)
            val likeDocRef = postRef.collection("likes").document(userId)

            likeDocRef.get().addOnSuccessListener { documentSnapshot ->
                var isLiked = documentSnapshot.exists()
                holder.likeButton.setImageResource(if (isLiked) R.drawable.heart_filled else R.drawable.heart)

                holder.likeButton.setOnClickListener {
                    if (!isLiked) {
                        // Add like
                        val likeData = mapOf("timestamp" to Timestamp.now())
                        likeDocRef.set(likeData).addOnSuccessListener {
                            firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(postRef)
                                val currentLikes = snapshot.getLong("likes") ?: 0
                                val newLikes = currentLikes + 1
                                transaction.update(postRef, "likes", newLikes)
                                newLikes
                            }.addOnSuccessListener { newLikes ->
                                isLiked = true
                                holder.likeButton.setImageResource(R.drawable.heart_filled)
                                holder.likeCountText.text = newLikes.toString()
                                Toast.makeText(holder.itemView.context, "Liked!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Remove like
                        likeDocRef.delete().addOnSuccessListener {
                            firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(postRef)
                                val currentLikes = snapshot.getLong("likes") ?: 1
                                val newLikes = (currentLikes - 1).coerceAtLeast(0)
                                transaction.update(postRef, "likes", newLikes)
                                newLikes
                            }.addOnSuccessListener { newLikes ->
                                isLiked = false
                                holder.likeButton.setImageResource(R.drawable.heart)
                                holder.likeCountText.text = newLikes.toString()
                                Toast.makeText(holder.itemView.context, "Unliked!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        holder.commentButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Comment clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    fun getFormattedTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}
