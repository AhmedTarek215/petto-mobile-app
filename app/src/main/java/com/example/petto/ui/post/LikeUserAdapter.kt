package com.example.petto.ui.post

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
import com.example.petto.data.model.LikeUser
import com.example.petto.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class LikeUserAdapter(private val users: List<LikeUser>)  :

    RecyclerView.Adapter<LikeUserAdapter.LikeUserViewHolder>() {

        class LikeUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val userImage: CircleImageView = view.findViewById(R.id.userImage)
            val userName: TextView = view.findViewById(R.id.userName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeUserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_like_user, parent, false)
            return LikeUserViewHolder(view)
        }

        override fun onBindViewHolder(holder: LikeUserViewHolder, position: Int) {
            val user = users[position]
            holder.userName.text = user.username
            Glide.with(holder.itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.profile)
                .into(holder.userImage)
        }

        override fun getItemCount(): Int = users.size
    }

