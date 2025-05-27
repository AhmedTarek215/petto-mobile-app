package com.example.petto.ui.post

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
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
import java.util.concurrent.TimeUnit

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.profileImage)
        val username: TextView = view.findViewById(R.id.username)
        val timePosted: TextView = view.findViewById(R.id.timePosted)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val postText: TextView = view.findViewById(R.id.postText)
        val readMore: TextView = view.findViewById(R.id.readMoreText)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val likeCountText: TextView = view.findViewById(R.id.likeCountText)
        val commentButton: ImageView = view.findViewById(R.id.commentButton)
        val commentCountText: TextView = view.findViewById(R.id.commentCountText)
        val menuButton: ImageView = view.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = firestore.collection("posts").document(post.id)

        holder.username.text = post.username.ifEmpty { "Unknown" }
        holder.timePosted.text = getRelativeTime(post.timestamp)

        // Profile Image
        if (!post.userProfileImage.isNullOrEmpty()) {
            Glide.with(context).load(post.userProfileImage).into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.profile)
        }

        // Post Text + Read More
        if (!post.content.isNullOrEmpty()) {
            holder.postText.text = post.content
            holder.postText.maxLines = 2
            holder.postText.ellipsize = TextUtils.TruncateAt.END
            holder.readMore.visibility = View.GONE
            holder.readMore.text = "Read more"

            holder.postText.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    holder.postText.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val layout = holder.postText.layout
                    if (layout != null && layout.lineCount >= 2) {
                        val isEllipsized = layout.getEllipsisCount(layout.lineCount - 1) > 0
                        holder.readMore.visibility = if (isEllipsized) View.VISIBLE else View.GONE
                    } else {
                        holder.readMore.visibility = View.GONE
                    }
                }
            })


            holder.readMore.setOnClickListener {
                val expanded = holder.postText.maxLines == Int.MAX_VALUE
                holder.postText.maxLines = if (expanded) 2 else Int.MAX_VALUE
                holder.postText.ellipsize = if (expanded) TextUtils.TruncateAt.END else null
                holder.readMore.text = if (expanded) "Read more" else "Show less"
            }
        } else {
            holder.postText.visibility = View.GONE
            holder.readMore.visibility = View.GONE
        }

        // Post Image
        if (!post.mediaUrl.isNullOrEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(context).load(post.mediaUrl).into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        // Likes
        postRef.collection("likes").document(userId).get().addOnSuccessListener {
            var isLiked = it.exists()
            holder.likeButton.setImageResource(if (isLiked) R.drawable.heart_filled else R.drawable.heart)

            holder.likeButton.setOnClickListener {
                if (!isLiked) {
                    postRef.collection("likes").document(userId).set(mapOf("timestamp" to Timestamp.now()))
                        .addOnSuccessListener {
                            firestore.runTransaction { tx ->
                                val newLikes = (tx.get(postRef).getLong("likes") ?: 0) + 1
                                tx.update(postRef, "likes", newLikes)
                                newLikes
                            }.addOnSuccessListener { newLikes ->
                                isLiked = true
                                holder.likeButton.setImageResource(R.drawable.heart_filled)
                                holder.likeCountText.text = newLikes.toString()
                            }
                        }
                } else {
                    postRef.collection("likes").document(userId).delete()
                        .addOnSuccessListener {
                            firestore.runTransaction { tx ->
                                val newLikes = (tx.get(postRef).getLong("likes") ?: 1) - 1
                                tx.update(postRef, "likes", newLikes)
                                newLikes
                            }.addOnSuccessListener { newLikes ->
                                isLiked = false
                                holder.likeButton.setImageResource(R.drawable.heart)
                                holder.likeCountText.text = newLikes.toString()
                            }
                        }
                }
            }
        }

        holder.likeCountText.text = post.likes.toString()
        holder.commentCountText.text = post.commentsCount.toString()

        holder.commentButton.setOnClickListener {
            context.startActivity(Intent(context, CommentsActivity::class.java).putExtra("postId", post.id))
        }

        // Show likes list
        holder.likeCountText.setOnClickListener {
            val sheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_likes, null)
            val dialog = BottomSheetDialog(context)
            val recycler = sheetView.findViewById<RecyclerView>(R.id.likesRecyclerView)
            recycler.layoutManager = LinearLayoutManager(context)
            dialog.setContentView(sheetView)

            postRef.collection("likes").get().addOnSuccessListener { snapshot ->
                val users = mutableListOf<LikeUser>()
                val userIds = snapshot.documents.map { it.id }
                userIds.forEach { id ->
                    firestore.collection("Users").document(id).get().addOnSuccessListener { doc ->
                        val name = "${doc.getString("firstName") ?: ""} ${doc.getString("lastName") ?: ""}".trim()
                        val image = doc.getString("profileImageUrl") ?: ""
                        users.add(LikeUser(id, name, image))
                        if (users.size == userIds.size) {
                            recycler.adapter = LikeUserAdapter(users)
                        }
                    }
                }
            }

            dialog.show()
        }

        // Menu for Post Owner
        if (userId == post.userId) {
            holder.menuButton.visibility = View.VISIBLE
            holder.menuButton.setOnClickListener {
                val popup = PopupMenu(context, holder.menuButton)
                popup.inflate(R.menu.post_item_menu)
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> {
                            context.startActivity(Intent(context, EditPostActivity::class.java).putExtra("postId", post.id))
                            true
                        }
                        R.id.action_delete -> {
                            AlertDialog.Builder(context)
                                .setTitle("Delete Post")
                                .setMessage("Are you sure you want to delete this post?")
                                .setPositiveButton("Delete") { _, _ ->
                                    postRef.delete().addOnSuccessListener {
                                        updatePosts(posts.toMutableList().also { list ->
                                            val pos = holder.adapterPosition
                                            if (pos != RecyclerView.NO_POSITION) {
                                                list.removeAt(pos)
                                            }
                                        })
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
            holder.menuButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun getRelativeTime(timestamp: Timestamp?): String {
        timestamp ?: return ""
        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            diff < TimeUnit.DAYS.toMillis(365) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 30} months ago"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 365} years ago"
        }
    }
}
