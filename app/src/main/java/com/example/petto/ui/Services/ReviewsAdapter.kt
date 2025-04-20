package com.example.petto.ui.Services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.Review
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReviewsAdapter(private val reviewList: List<Review>) :
    RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val ratingBarReview: RatingBar = itemView.findViewById(R.id.ratingBarReview)
        val reviewTime: TextView = itemView.findViewById(R.id.reviewTime)
        val reviewText: TextView = itemView.findViewById(R.id.reviewText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.userName.text = "${review.user.fname} ${review.user.lname}".trim()

        holder.reviewText.text = review.r_comment ?: "No comment provided"

        holder.ratingBarReview.rating = review.rating

        holder.reviewTime.text = getTimeAgo("${review.date} ${review.time}".trim())

        val imageUrl = review.user.user_img
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.profile)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.profile)
        }
    }

    override fun getItemCount(): Int = reviewList.size

    fun getTimeAgo(timestamp: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC") // Make sure it's in UTC if your backend is
            val past = format.parse(timestamp) ?: return "Unknown time"
            val now = Date()

            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
            val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)

            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
                hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
                days == 1L -> "Yesterday"
                days < 7 -> "$days day${if (days != 1L) "s" else ""} ago"
                else -> "${days / 7} week${if (days / 7 != 1L) "s" else ""} ago"
            }
        } catch (e: Exception) {
            "Unknown time"
        }
    }
}
