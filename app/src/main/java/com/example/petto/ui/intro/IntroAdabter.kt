package com.example.petto.ui.intro

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.ui.Login.Login
import com.example.petto.R

class IntroAdabter(
    private val introItems: List<IntroItem>,
    private val onSignUpClick: () -> Unit,
    private val onLoginClick: () -> Unit
) : RecyclerView.Adapter<IntroAdabter.IntroViewHolder>() {

    inner class IntroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView? = view.findViewById(R.id.intro_image)
        private val titleTextView: TextView? = view.findViewById(R.id.intro_title)
        private val descriptionTextView: TextView? = view.findViewById(R.id.intro_description)
        private val base: ImageView? = view.findViewById(R.id.base)
        private val btnSignUp: Button? = view.findViewById(R.id.btn_signup)
        private val btnLogin: Button? = view.findViewById(R.id.btn_login)

        fun bind(introItem: IntroItem) {
            imageView?.setImageResource(introItem.imageResId)
            base?.setImageResource(introItem.baseResId)
            titleTextView?.text = introItem.title
            descriptionTextView?.text = introItem.description

            btnSignUp?.setOnClickListener { onSignUpClick.invoke() }
            btnLogin?.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, Login::class.java)
                context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_intro, parent, false)
        return IntroViewHolder(view)
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.bind(introItems[position])
    }

    override fun getItemCount(): Int = introItems.size
}
