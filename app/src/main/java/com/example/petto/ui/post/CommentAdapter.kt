package com.example.petto.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.Comment

class CommentAdapter(private var commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        holder.username.text = comment.username
        holder.commentText.text = comment.content
        holder.commentText.visibility = if (comment.content.isNotEmpty()) View.VISIBLE else View.GONE
        holder.timestamp.text = android.text.format.DateFormat.format("dd MMM, HH:mm", comment.timestamp.toDate())

        if (!comment.userProfileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(comment.userProfileImage)
                .placeholder(R.drawable.profile)
                .into(holder.userImage)
        } else {
            holder.userImage.setImageResource(R.drawable.profile)
        }
    }

    override fun getItemCount(): Int = commentList.size

    fun updateComments(newComments: List<Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }
}
