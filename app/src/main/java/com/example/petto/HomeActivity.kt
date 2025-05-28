package com.example.petto.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.PetService
import com.example.petto.data.model.Post
import com.example.petto.data.model.Tip
import com.example.petto.ui.Services.ServiceProfile
import com.example.petto.ui.Services.ServicesAdapter
import com.example.petto.ui.post.PostAdapter
import com.example.petto.ui.tips.TipsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.petto.ui.calender.Calendar
class HomeActivity : AppCompatActivity() {

    private lateinit var greetingText: TextView
    private lateinit var servicesRecyclerView: RecyclerView
    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout

    private lateinit var tipAdapter: TipsAdapter
    private lateinit var postAdapter: PostAdapter

    private var currentTipIndex = 0
    private var currentPostIndex = 0
    private var tipsList: List<Tip> = listOf()
    private var postList: List<Post> = listOf()

    private var servicesLoaded = false
    private var tipsLoaded = false
    private var postsLoaded = false

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        greetingText = findViewById(R.id.greetingText)
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView)
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        contentLayout = findViewById(R.id.homeSectionsLayout)

        contentLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        loadGreeting()
        setupPetServices()
        setupTips()
        setupPosts()
        setupNavigation()
    }

    private fun loadGreeting() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("Users").document(uid).get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: "there"
                greetingText.text = "Hello $firstName!"
            }
    }

    private fun setupPetServices() {
        firestore.collection("services").get().addOnSuccessListener { result ->
            val services = result.documents.mapNotNull { doc ->
                doc.toObject(PetService::class.java)?.apply {
                    service_id = doc.id
                }
            }

            val adapter = ServicesAdapter(services.shuffled()) { service ->
                val intent = Intent(this, ServiceProfile::class.java)
                intent.putExtra("service_id", service.service_id)
                startActivity(intent)
            }

            servicesRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            servicesRecyclerView.adapter = adapter

            servicesLoaded = true
            checkIfAllLoaded()
        }

        findViewById<TextView>(R.id.showAllServices).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.Services.ServicesPage::class.java))
        }
    }

    private fun setupTips() {
        tipAdapter = TipsAdapter(listOf())
        tipsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tipsRecyclerView.adapter = tipAdapter

        firestore.collection("Tips").get().addOnSuccessListener { result ->
            tipsList = result.documents.mapNotNull { it.toObject(Tip::class.java) }
            showTipAt(currentTipIndex)
            tipsLoaded = true
            checkIfAllLoaded()
        }

        findViewById<TextView>(R.id.showAllTips).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.tips.TipsActivity::class.java))
        }

        findViewById<ImageView>(R.id.tipsLeftArrow).setOnClickListener {
            if (currentTipIndex > 0) {
                currentTipIndex--
                showTipAt(currentTipIndex)
            }
        }

        findViewById<ImageView>(R.id.tipsRightArrow).setOnClickListener {
            if (currentTipIndex < tipsList.size - 1) {
                currentTipIndex++
                showTipAt(currentTipIndex)
            }
        }
    }

    private fun showTipAt(index: Int) {
        if (index in tipsList.indices) {
            tipAdapter.updateTips(listOf(tipsList[index]))
        }
    }

    private fun setupPosts() {
        postAdapter = PostAdapter(listOf())
        postsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        postsRecyclerView.adapter = postAdapter

        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                postList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.apply { id = doc.id }
                }

                showPostAt(currentPostIndex)
                postsLoaded = true
                checkIfAllLoaded()
            }

        findViewById<TextView>(R.id.showAllPosts).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.post.PostListActivity::class.java))
        }

        findViewById<ImageView>(R.id.postsLeftArrow).setOnClickListener {
            if (currentPostIndex > 0) {
                currentPostIndex--
                showPostAt(currentPostIndex)
            }
        }

        findViewById<ImageView>(R.id.postsRightArrow).setOnClickListener {
            if (currentPostIndex < postList.size - 1) {
                currentPostIndex++
                showPostAt(currentPostIndex)
            }
        }
    }

    private fun showPostAt(index: Int) {
        if (index in postList.indices) {
            postAdapter.updatePosts(listOf(postList[index]))
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.nav_calendar).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.calender.Calendar::class.java))
        }

        findViewById<ImageView>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.post.CreatePostActivity::class.java))
        }

        findViewById<ImageView>(R.id.nav_notifications).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.notification.NotificationActivity::class.java))
        }

        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, com.example.petto.ui.profiles.UserProfile::class.java))
        }
    }

    private fun checkIfAllLoaded() {
        if (servicesLoaded && tipsLoaded && postsLoaded) {
            contentLayout.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}
