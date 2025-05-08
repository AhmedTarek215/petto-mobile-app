package com.example.petto.ui.post


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListFragment : Fragment() {

//    private lateinit var recyclerView: RecyclerView
//    private lateinit var postAdapter: PostAdapter
//    private lateinit var progressBar: ProgressBar
//    private val firestore = FirebaseFirestore.getInstance()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_post_list, container, false)
//
//        recyclerView = view.findViewById(R.id.PostsRecyclerView)
//        progressBar = view.findViewById(R.id.loadingProgressBar)
//
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        postAdapter = PostAdapter(emptyList())
//        recyclerView.adapter = postAdapter
//
//        loadPosts()
//
//        return view
//    }
//
//    private fun loadPosts() {
//        progressBar.visibility = View.VISIBLE
//
//        firestore.collection("posts")
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { result ->
//                val posts = result.mapNotNull { it.toObject(Post::class.java) }
//                postAdapter.updatePosts(posts)
//                progressBar.visibility = View.GONE
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
//                progressBar.visibility = View.GONE
//            }
//    }
}
