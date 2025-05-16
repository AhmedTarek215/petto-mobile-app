package com.example.petto.ui.calender

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.petto.R
import com.example.petto.worker.ReminderWorker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddReminder : AppCompatActivity() {

    private lateinit var petDropdown: AutoCompleteTextView
    private lateinit var eventTypeDropdown: AutoCompleteTextView
    private lateinit var eventNameInput: EditText
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var progressOverlay: View

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedTimestamp: Timestamp? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        requestNotificationPermission()

        petDropdown = findViewById(R.id.petNameDropdown)
        eventTypeDropdown = findViewById(R.id.eventTypeDropdown)
        eventNameInput = findViewById(R.id.eventNameInput)
        dateText = findViewById(R.id.eventDateText)
        timeText = findViewById(R.id.eventTimeText)
        progressOverlay = findViewById(R.id.loadingOverlay)

        loadPetNameFromUser()
        setupEventTypeDropdown()
        setupDatePicker()
        setupTimePicker()

        findViewById<AppCompatButton>(R.id.btnConfirm).setOnClickListener {
            handleConfirm()
        }

        findViewById<AppCompatButton>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications may not appear", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadPetNameFromUser() {
        progressOverlay.visibility = View.VISIBLE
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("Users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val petMap = document.get("pet") as? Map<*, *>
                    val petName = petMap?.get("name") as? String
                    if (!petName.isNullOrEmpty()) {
                        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listOf(petName))
                        petDropdown.setAdapter(adapter)
                    } else {
                        Toast.makeText(this, "No pet name found", Toast.LENGTH_SHORT).show()
                    }
                }
                progressOverlay.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load pet name", Toast.LENGTH_SHORT).show()
                progressOverlay.visibility = View.GONE
            }
    }

    private fun setupEventTypeDropdown() {
        val types = listOf("Vaccine", "Checkup", "Grooming", "Training", "Surgery", "Deworming")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        eventTypeDropdown.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        dateText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0)
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateText.text = format.format(calendar.time)
                selectedTimestamp = Timestamp(calendar.time)
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }
    }

    private fun setupTimePicker() {
        timeText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                val amPm = if (hourOfDay < 12) "AM" else "PM"
                val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val formattedTime = String.format("%02d:%02d %s", hour, minute, amPm)
                timeText.text = formattedTime
                selectedTime = formattedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            timePicker.show()
        }
    }

    private fun handleConfirm() {
        val petName = petDropdown.text.toString().trim()
        val eventName = eventNameInput.text.toString().trim()
        val eventType = eventTypeDropdown.text.toString().trim()

        if (petName.isEmpty() || eventName.isEmpty() || eventType.isEmpty()
            || selectedTimestamp == null || selectedTime == null
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: return

        // Merge selected date + selected time into one Calendar
        val fullDateTime = Calendar.getInstance().apply {
            time = selectedTimestamp!!.toDate()

            val timeParts = selectedTime!!.split(" ", ":")
            var hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val amPm = timeParts[2]

            if (amPm == "PM" && hour != 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val finalTimestamp = Timestamp(fullDateTime.time)

        val reminderData = hashMapOf(
            "user_id" to currentUserId,
            "pet_name" to petName,
            "reminder_name" to eventName,
            "reminder_type" to eventType,
            "r_date" to finalTimestamp,
            "r_time" to selectedTime
        )

        progressOverlay.visibility = View.VISIBLE

        firestore.collection("reminders")
            .add(reminderData)
            .addOnSuccessListener {
                scheduleReminderNotifications(eventName, petName, eventType, finalTimestamp)
                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add reminder", Toast.LENGTH_SHORT).show()
                progressOverlay.visibility = View.GONE
            }
    }

    private fun scheduleReminderNotifications(eventName: String, petName: String, type: String, timestamp: Timestamp) {
        val context = this
        val now = Timestamp.now().toDate().time
        val eventTimeMillis = timestamp.toDate().time

        val reminderData = Data.Builder()
            .putString("event_name", eventName)
            .putString("pet_name", petName)
            .putString("event_type", type)
            .build()

        // 2 days before
        val twoDaysBefore = eventTimeMillis - TimeUnit.DAYS.toMillis(2)
        if (twoDaysBefore > now) {
            val delay = twoDaysBefore - now
            val work = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(reminderData)
                .build()
            WorkManager.getInstance(context).enqueue(work)
        }

        // Day of event
        if (eventTimeMillis > now) {
            val delay = eventTimeMillis - now
            val work = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(reminderData)
                .build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
