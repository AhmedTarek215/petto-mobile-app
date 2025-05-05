package com.example.petto
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.petto.ui.notification.NotificationFragment
import com.example.petto.ui.post.CreatePostActivity
import com.example.petto.ui.post.PostListFragment
import com.example.petto.ui.profiles.UserProfile
import com.example.petto.ui.tips.TipsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupBottomNavigation()

        val openFragment = intent.getStringExtra("open_fragment")
        when (openFragment) {
            "posts" -> loadFragment(PostListFragment())
            "notifications" -> loadFragment(NotificationFragment())
            else -> showHelloPetto() // for home screen only
        }


    }


    private fun showHelloPetto() {
        findViewById<TextView>(R.id.hello_text).visibility = View.VISIBLE
        findViewById<FragmentContainerView>(R.id.fragment_container).visibility = View.GONE
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Optional: set current tab based on activity/intent
        // bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_posts -> {
                    if (this is HomeActivity) {
                        loadFragment(PostListFragment())
                    } else {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("open_fragment", "posts")
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_notifications -> {
                    if (this is HomeActivity) {
                        loadFragment(NotificationFragment())
                    } else {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("open_fragment", "notifications")
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_tips -> {
                    if (this !is TipsActivity) {
                        val intent = Intent(this, TipsActivity::class.java)
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }




    private fun loadFragment(fragment: Fragment) {
        findViewById<TextView>(R.id.hello_text).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.fragment_container).visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }




}