package com.example.petto.ui.tips

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.HomeActivity
import com.example.petto.R
import com.example.petto.data.repository.TipRepository
import com.example.petto.ui.notification.NotificationFragment
import com.example.petto.ui.post.CreatePostActivity
import com.example.petto.ui.post.PostListFragment
import com.example.petto.ui.profiles.UserProfile
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TipsActivity : AppCompatActivity() {

    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.tipsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
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


