package com.example.projectfinal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectfinal.adapter.LatestMoviesAdapter
import com.example.projectfinal.adapter.OnMovieClickListener
import com.example.projectfinal.adapter.TopMoviesAdapter
import com.example.projectfinal.api.MovieResponse
import com.example.projectfinal.api.RetrofitClient
import com.example.projectfinal.manager.AuthManager
import com.example.projectfinal.manager.BookmarkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnMovieClickListener {

    private lateinit var topMoviesAdapter: TopMoviesAdapter
    private lateinit var latestMoviesAdapter: LatestMoviesAdapter
    private val apiKey = "174e03eb18b4d165185e02ddcc8932a8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setupRecyclerViews()
        setupClickListeners()
        setupBottomNav()
        fetchData()

        if (AuthManager.isUserLoggedIn()) {
            BookmarkManager.fetchBookmarksForCurrentUser {
                if (::topMoviesAdapter.isInitialized) topMoviesAdapter.notifyDataSetChanged()
                if (::latestMoviesAdapter.isInitialized) latestMoviesAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::topMoviesAdapter.isInitialized) topMoviesAdapter.notifyDataSetChanged()
        if (::latestMoviesAdapter.isInitialized) latestMoviesAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerViews() {
        val rvTopFive: RecyclerView = findViewById(R.id.rvTopFiveMovies)
        rvTopFive.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        topMoviesAdapter = TopMoviesAdapter(mutableListOf(), this)
        rvTopFive.adapter = topMoviesAdapter

        val rvLatest: RecyclerView = findViewById(R.id.rvLatestMovies)
        rvLatest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        latestMoviesAdapter = LatestMoviesAdapter(mutableListOf(), this)
        rvLatest.adapter = latestMoviesAdapter
    }

    private fun setupClickListeners() {
        val tvSeeMore: TextView = findViewById(R.id.tvSeeMore)
        tvSeeMore.setOnClickListener {
            startActivity(Intent(this, DiscoverActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == bottomNav.selectedItemId) {
                return@setOnItemSelectedListener false
            }
            when (menuItem.itemId) {
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.nav_bookmarks -> {
                    startActivity(Intent(this, BookmarksActivity::class.java))
                    overridePendingTransition(0, 0)
                }
            }
            true
        }
    }

    private fun fetchData() {
        fetchTopRatedMovies()
        fetchLatestMovies()
    }

    private fun fetchTopRatedMovies() {
        RetrofitClient.instance.getTopRatedMovies(apiKey).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val movies = response.body()?.movies ?: emptyList()
                    topMoviesAdapter.updateMovies(movies.take(5))
                }
            }
            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {}
        })
    }

    private fun fetchLatestMovies() {
        RetrofitClient.instance.getNowPlayingMovies(apiKey).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val movies = response.body()?.movies ?: emptyList()
                    latestMoviesAdapter.updateMovies(movies)
                }
            }
            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {}
        })
    }

    override fun onMovieClick(movieId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("MOVIE_ID", movieId)
        startActivity(intent)
    }
}