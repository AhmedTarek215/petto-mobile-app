package com.example.petto.ui.notification

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.ui.notification.CustomTypefaceSpan
import android.text.SpannableString
import android.text.Spanned
import androidx.core.content.res.ResourcesCompat
import com.example.petto.data.model.NotificationItem
import com.example.petto.ui.post.CommentsActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class NotificationsAdapter(
    private val notifications: List<NotificationItem>
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val fullText = notification.text
        val username = fullText.substringBefore(" ")
        val spannable = SpannableString(fullText)

        val context = holder.itemView.context
        val customTypeface = ResourcesCompat.getFont(context, R.font.alexandriabold)

        if (customTypeface != null) {
            spannable.setSpan(
                CustomTypefaceSpan(customTypeface),
                0,
                username.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        holder.notificationText.text = spannable


        // Format time like "3h ago"
        val timeAgo = notification.timestamp?.toDate()?.let { getTimeAgo(it) } ?: ""
        holder.notificationTime.text = timeAgo

        if (!notification.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(notification.profileImage)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.profile)
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("postId", notification.postId)
            intent.putExtra("highlightCommentInput", true)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = notifications.size

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.profileImage)
        val notificationText: TextView = view.findViewById(R.id.notificationText)
        val notificationTime: TextView = view.findViewById(R.id.notificationTime)
    }

    private fun getTimeAgo(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
        }
    }
}
