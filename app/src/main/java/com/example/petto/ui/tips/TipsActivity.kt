package com.example.petto.ui.tips

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.repository.TipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TipsActivity : AppCompatActivity() {

    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var loadingProgressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }



        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        tipsRecyclerView.layoutManager = LinearLayoutManager(this)
        tipsAdapter = TipsAdapter(emptyList())
        tipsRecyclerView.adapter = tipsAdapter



        // Setup Bottom Navigation
//        setupBottomNavigation()

        // data fetching
        fetchTipsFromFirestore()


    }

    private fun fetchTipsFromFirestore() {
        loadingProgressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val tips = TipRepository.getTips()

            if (tips.isNotEmpty()) {
                tipsAdapter.updateTips(tips)
            } else {
                Toast.makeText(this@TipsActivity, "Failed to load tips.", Toast.LENGTH_SHORT).show()
            }
            loadingProgressBar.visibility = View.GONE
        }
    }
}


