package com.example.petto.ui.Services

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.*
import com.example.petto.R

class ReviewDialog(
    context: Context,
    private val listener: ReviewSubmitListener
) : Dialog(context) {

    interface ReviewSubmitListener {
        fun onReviewSubmitted(rating: Float, text: String?)
    }

    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_review_dialog)

        ratingBar = findViewById(R.id.dialogRatingBar)
        reviewEditText = findViewById(R.id.reviewEditText)
        submitButton = findViewById(R.id.submitReviewButton)
        cancelButton = findViewById(R.id.closeDialogIcon)

        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            val reviewText = reviewEditText.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
            } else {
                listener.onReviewSubmitted(rating, if (reviewText.isEmpty()) null else reviewText)
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
}
