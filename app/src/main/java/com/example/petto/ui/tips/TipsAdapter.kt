package com.example.petto.ui.tips


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R // ✅ Replace with your real package name
import com.example.petto.data.model.Tip

class TipsAdapter(private var tipsList: List<Tip>) :
    RecyclerView.Adapter<TipsAdapter.TipViewHolder>() {

    class TipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.tipImageView)
        val titleTextView: TextView = view.findViewById(R.id.tipTitleTextView)
        val contentTextView: TextView = view.findViewById(R.id.tipContentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tipsList[position]
        holder.titleTextView.text = tip.title
        holder.contentTextView.text = tip.content

        if (tip.imageUrl.isNotEmpty()) {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(tip.imageUrl)
                .into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = tipsList.size

    fun updateTips(newTips: List<Tip>) {
        tipsList = newTips
        notifyDataSetChanged()
    }
}
