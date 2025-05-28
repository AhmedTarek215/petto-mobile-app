package com.example.petto.ui.calender

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.Reminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(private var reminders: List<Reminder>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    fun updateReminders(newReminders: List<Reminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])
    }

    override fun getItemCount(): Int = reminders.size

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petNameText: TextView = itemView.findViewById(R.id.pet_name)
        private val reminderTitleText: TextView = itemView.findViewById(R.id.reminder_title)
        private val reminderDateText: TextView = itemView.findViewById(R.id.reminder_date)
        private val reminderTimeText: TextView = itemView.findViewById(R.id.reminder_time)

        fun bind(reminder: Reminder) {
            petNameText.text = reminder.pet_name
            reminderTitleText.text = reminder.reminder_name

            // Format r_date
            reminder.r_date?.toDate()?.let { date ->
                val dateFormat = SimpleDateFormat("dd  MMM", Locale.getDefault())
                reminderDateText.text = dateFormat.format(date)
            }

            reminderTimeText.text = reminder.r_time
        }
    }
}