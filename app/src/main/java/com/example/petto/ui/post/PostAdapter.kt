package com.example.petto.ui.post

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.LikeUser
import com.example.petto.data.model.Post
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

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
        val commentCountText: TextView = view.findViewById(R.id.commentCountText)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.itemView.findViewById<ImageView>(R.id.menuButton).visibility = View.GONE

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
        val menuButton = holder.itemView.findViewById<ImageView>(R.id.menuButton)


        if (postId.isNotEmpty() && userId != null) {
            val postRef = firestore.collection("posts").document(postId)
            val likeDocRef = postRef.collection("likes").document(userId)

            likeDocRef.get().addOnSuccessListener { documentSnapshot ->
                var isLiked = documentSnapshot.exists()
                holder.likeButton.setImageResource(if (isLiked) R.drawable.heart_filled else R.drawable.heart)

                holder.likeButton.setOnClickListener {
                    if (!isLiked) {
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
                                firestore.collection("Users").document(userId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val fullName = "${userDoc.getString("firstName") ?: ""} ${userDoc.getString("lastName") ?: ""}".trim()
                                        val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""

                                        if (userId != post.userId) {
                                            sendLikeNotification(
                                                postOwnerId = post.userId,
                                                currentUserId = userId,
                                                postId = post.id,
                                                currentUserName = fullName,
                                                profileImageUrl = profileImageUrl
                                            )
                                        }
                                    }

                            }
                        }
                    } else {
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

            holder.likeCountText.setOnClickListener {
                val bottomSheetView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.layout_bottom_sheet_likes, null)

                val dialog = BottomSheetDialog(holder.itemView.context)
                dialog.setContentView(bottomSheetView)

                val likesRecyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.likesRecyclerView)
                likesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

                postRef.collection("likes").get().addOnSuccessListener { likesSnapshot ->
                    val userIds = likesSnapshot.documents.map { it.id }
                    val users = mutableListOf<LikeUser>()
                    val userCollection = firestore.collection("Users")

                    for (userIdItem in userIds) {
                        userCollection.document(userIdItem).get().addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("firstName").orEmpty() + " " + userDoc.getString("lastName").orEmpty()
                            val imageUrl = userDoc.getString("profileImageUrl") ?: ""
                            users.add(LikeUser(userIdItem, name, imageUrl))

                            if (users.size == userIds.size) {
                                likesRecyclerView.adapter = LikeUserAdapter(users)
                            }
                        }
                    }
                }
                dialog.show()
            }

            //menu
            if (userId == post.userId) {
                menuButton.visibility = View.VISIBLE
                menuButton.setOnClickListener {
                    val popup = android.widget.PopupMenu(holder.itemView.context, menuButton)
                    popup.inflate(R.menu.post_item_menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> {
                                val context = holder.itemView.context
                                val intent = Intent(context, EditPostActivity::class.java)
                                intent.putExtra("postId", post.id)
                                context.startActivity(intent)
                                true
                            }
                            R.id.action_delete -> {
                                AlertDialog.Builder(holder.itemView.context)
                                    .setTitle("Delete Post")
                                    .setMessage("Are you sure you want to delete this post?")
                                    .setPositiveButton("Delete") { _, _ ->
                                        firestore.collection("posts").document(post.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(holder.itemView.context, "Post deleted", Toast.LENGTH_SHORT).show()
                                                // Optional: Remove from list and refresh UI
                                                val mutableList = posts.toMutableList()
                                                mutableList.removeAt(holder.adapterPosition)
                                                updatePosts(mutableList)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(holder.itemView.context, "Failed to delete post", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            } else {
                menuButton.visibility = View.GONE
            }

        }




        holder.commentCountText.text = post.commentsCount.toString()


        holder.commentButton.setOnClickListener {
            if (post.id.isNotEmpty()) {
                val context = holder.itemView.context
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId", post.id)
                holder.itemView.context.startActivity(intent)            } else {
                Toast.makeText(holder.itemView.context, "Invalid Post ID", Toast.LENGTH_SHORT).show()
            }
        }



    }
    private fun sendLikeNotification(
        postOwnerId: String,
        currentUserId: String,
        postId: String,
        currentUserName: String,
        profileImageUrl: String?
    ) {
        val db = FirebaseFirestore.getInstance()
        val notificationData = hashMapOf(
            "type" to "like",
            "text" to "$currentUserName liked your post",
            "timestamp" to com.google.firebase.Timestamp.now(),
            "senderId" to currentUserId,
            "postId" to postId,
            "profileImage" to profileImageUrl
        )

        db.collection("notifications")
            .document(postOwnerId)
            .collection("items")
            .add(notificationData)
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

    companion object {
        fun bindPostToView(view: View, post: Post) {
            val username = view.findViewById<TextView>(R.id.username)
            val profileImage = view.findViewById<ImageView>(R.id.profileImage)
            val timePosted = view.findViewById<TextView>(R.id.timePosted)
            val postText = view.findViewById<TextView>(R.id.postText)
            val postMedia = view.findViewById<ImageView>(R.id.postMedia)
            val likeCountText = view.findViewById<TextView>(R.id.likeCountText)
            val commentCountText = view.findViewById<TextView>(R.id.commentCountText)
            val likeButton = view.findViewById<ImageView>(R.id.likeButton)
            val commentButton = view.findViewById<ImageView>(R.id.commentButton)

            username.text = post.username
            timePosted.text = getFormattedTime(post.timestamp)
            postText.text = post.content ?: ""
            likeCountText.text = post.likes.toString()
            commentCountText.text = post.commentsCount.toString()

            if (!post.userProfileImage.isNullOrEmpty()) {
                Glide.with(view.context).load(post.userProfileImage).into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.profile)
            }

            if (!post.mediaUrl.isNullOrEmpty()) {
                postMedia.visibility = ImageView.VISIBLE
                Glide.with(view.context).load(post.mediaUrl).into(postMedia)
            } else {
                postMedia.visibility = ImageView.GONE
            }

            likeButton.setImageResource(R.drawable.heart)
            commentButton.setImageResource(R.drawable.speech_bubble_filled)
        }

        private fun getFormattedTime(timestamp: Timestamp?): String {
            if (timestamp == null) return ""
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(timestamp.toDate())
        }
    }

}
