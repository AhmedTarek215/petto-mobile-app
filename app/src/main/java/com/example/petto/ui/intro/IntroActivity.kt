package com.example.petto.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.petto.R
import com.example.petto.ui.Login.Login
import com.example.petto.ui.SignUp.SignUp1

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        viewPager.adapter = IntroAdabter(getIntroItems(),
            onSignUpClick = {
                startActivity(Intent(this, SignUp1::class.java))
            },
            onLoginClick = {
                startActivity(Intent(this, Login::class.java)) // Make sure Login activity exists
            }
        )
    }

    private fun getIntroItems(): List<IntroItem> {
        return listOf(
            IntroItem("Pet Profile",
                "Keep Everything About Your Pet in One Place...create a personal space for each of your pets. Just add their details and a cute avatar to get started! You can also keep track of important details like medical history and vaccination records. It’s the easiest way to stay organized and make sure your furry friend always gets the care they need.",
                R.drawable.pet,
                R.drawable.base1),

            IntroItem("Reminder Notifications",
                "Never Miss a Moment That Matters...Life gets busy, but we’ve got your back! With the Reminder Notifications, you can set alerts for your pet’s vet visits, vaccination dates, grooming appointments, medication schedules, and more. Just set it once, and we’ll remind you when it’s time",
                R.drawable.notification,
                R.drawable.base1),

            IntroItem("Posting",
                "Tell the World About Your Pet...Share what your pet’s been up to? With the Posting feature, you can write updates, share stories, ask questions, or give tips. It’s a great way to connect with other pet lovers, and be part of a caring, pet-friendly community.",
                R.drawable.post,
                R.drawable.base1),

            IntroItem("Nearby Pet Services",
                "Find Trusted Help for Your Pet...Looking for a vet, Hotel, or shelter? The Pet Services feature helps you discover reliable pet care providers around you. You can view service details, read reviews from other users, check ratings, and choose what’s best for your furry friend all in one convenient place.",
                R.drawable.sitting,
                R.drawable.base2),

            IntroItem("Tips and Tricks",
                "Smart Advice for Happier Pets...Want to make pet care easier and more fun? Our Tips and Tricks section offers helpful advice on everything from training and grooming to health and behavior. Whether you’re a new pet parent or a seasoned pro, you’ll find quick, practical tips to keep your pet happy, healthy, and well-behaved.",
                R.drawable.quick_tips,
                R.drawable.base1),
        )
    }
}
