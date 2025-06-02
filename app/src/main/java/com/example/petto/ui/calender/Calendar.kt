package com.example.petto.ui.calender

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.Reminder
import com.example.petto.ui.HomeActivity
import com.example.petto.ui.notification.NotificationActivity
import com.example.petto.ui.post.CreatePostActivity
import com.example.petto.ui.profiles.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate
import java.time.ZoneId

class Calendar : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var reminderRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var backButton: ImageView

    private val allReminders = mutableListOf<Reminder>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        calendarView = findViewById(R.id.calendarView)
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.back_icon)

        reminderRecyclerView.layoutManager = LinearLayoutManager(this)
        reminderAdapter = ReminderAdapter(emptyList())
        reminderRecyclerView.adapter = reminderAdapter

        calendarView.selectedDate = CalendarDay.today()
        calendarView.setOnDateChangedListener { _, _, _ -> }

        decorateToday()
        setupNavigation()
        loadReminders()

        findViewById<ImageView>(R.id.add_icon).setOnClickListener {
            startActivity(Intent(this, AddReminder::class.java))
            finish()
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.nav_calendar).setOnClickListener { }
        findViewById<ImageView>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.nav_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java))
            finish()
        }
    }

    private fun loadReminders() {
        progressBar.visibility = View.VISIBLE

        db.collection("reminders")
            .whereEqualTo("user_id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                allReminders.clear()
                val dotDates = mutableSetOf<CalendarDay>()
                val today = LocalDate.now()

                snapshot.documents.forEach { doc ->
                    val reminder = doc.toObject(Reminder::class.java)
                    val timestamp = reminder?.r_date

                    if (reminder != null && timestamp != null) {
                        val localDate = timestamp.toDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        if (!localDate.isBefore(today)) {
                            allReminders.add(reminder)

                            val calendarDay = CalendarDay.from(
                                localDate.year,
                                localDate.monthValue,
                                localDate.dayOfMonth
                            )
                            dotDates.add(calendarDay)
                        }
                    }
                }

                progressBar.visibility = View.GONE
                decorateCalendarWithDots(dotDates)

                val sortedReminders = allReminders.sortedBy { it.r_date?.toDate() }
                reminderAdapter.updateReminders(sortedReminders)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
            }
    }

    private fun decorateCalendarWithDots(dates: Set<CalendarDay>) {
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
            override fun decorate(view: DayViewFacade) {
                view.addSpan(DotSpan(8f, getColor(R.color.teal_700)))
            }
        })
    }

    private fun decorateToday() {
        val today = CalendarDay.today()

        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean = day == today
            override fun decorate(view: DayViewFacade) {
                view.setSelectionDrawable(getDrawable(R.drawable.selector_today)!!)
            }
        })
    }
}
