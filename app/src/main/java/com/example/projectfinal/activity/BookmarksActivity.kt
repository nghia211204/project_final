package com.example.projectfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.projectfinal.adapter.LatestMoviesAdapter
import com.example.projectfinal.adapter.OnMovieClickListener
import com.example.projectfinal.api.Movie
import com.example.projectfinal.api.RetrofitClient
import com.example.projectfinal.manager.AuthManager
import com.example.projectfinal.manager.BookmarkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarksActivity : AppCompatActivity(), OnMovieClickListener {

    private lateinit var rvBookmarks: RecyclerView
    private lateinit var tvLoginPrompt: TextView
    private lateinit var tvEmptyBookmark: TextView
    private lateinit var bookmarksAdapter: LatestMoviesAdapter
    private val apiKey = "174e03eb18b4d165185e02ddcc8932a8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        val toolbar: Toolbar = findViewById(R.id.toolbar_bookmarks)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Bookmarks."

        rvBookmarks = findViewById(R.id.rv_bookmarks)
        tvLoginPrompt = findViewById(R.id.tv_login_prompt)
        tvEmptyBookmark = findViewById(R.id.tv_empty_bookmark)

        bookmarksAdapter = LatestMoviesAdapter(mutableListOf(), this, onBookmarkChanged = { movieId ->
            bookmarksAdapter.removeItem(movieId)
            if (bookmarksAdapter.itemCount == 0) {
                tvEmptyBookmark.visibility = View.VISIBLE
            }
        })

        rvBookmarks.adapter = bookmarksAdapter
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        checkLoginStateAndLoadData()
    }

    private fun checkLoginStateAndLoadData() {
        invalidateOptionsMenu()
        if (AuthManager.isUserLoggedIn()) {
            tvLoginPrompt.visibility = View.GONE
            loadBookmarkedMovies()
        } else {
            tvLoginPrompt.visibility = View.VISIBLE
            rvBookmarks.visibility = View.GONE
            tvEmptyBookmark.visibility = View.GONE
            bookmarksAdapter.updateMovies(emptyList())
        }
    }

    private fun loadBookmarkedMovies() {
        val bookmarkedIds = BookmarkManager.getBookmarks().toList()

        if (bookmarkedIds.isEmpty()) {
            rvBookmarks.visibility = View.GONE
            tvEmptyBookmark.visibility = View.VISIBLE
            bookmarksAdapter.updateMovies(emptyList())
            return
        }

        rvBookmarks.visibility = View.VISIBLE
        tvEmptyBookmark.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val fetchedMovies = mutableListOf<Movie>()
            for (id in bookmarkedIds) {
                try {
                    val response = RetrofitClient.instance.getMovieDetails(id, apiKey).execute()
                    if (response.isSuccessful) {
                        response.body()?.let { detail ->
                            fetchedMovies.add(
                                Movie(
                                    id = detail.id,
                                    title = detail.title,
                                    overview = detail.overview,
                                    posterPath = detail.posterPath,
                                    voteAverage = detail.voteAverage,
                                    genreIds = detail.genres.map { it.id }
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookmarksActivity", "Failed to fetch details for id: $id", e)
                }
            }
            withContext(Dispatchers.Main) {
                bookmarksAdapter.updateMovies(fetchedMovies)
                if (fetchedMovies.isEmpty()) {
                    rvBookmarks.visibility = View.GONE
                    tvEmptyBookmark.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun onMovieClick(movieId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("MOVIE_ID", movieId)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bookmarks_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val loginItem = menu?.findItem(R.id.action_login)
        if (AuthManager.isUserLoggedIn()) {
            loginItem?.setIcon(R.drawable.ic_logout)
            loginItem?.title = "Logout"
        } else {
            loginItem?.setIcon(R.drawable.ic_login)
            loginItem?.title = "Login"
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                if (AuthManager.isUserLoggedIn()) {
                    AuthManager.logout()
                    BookmarkManager.clearLocalBookmarks()
                    Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show()
                    checkLoginStateAndLoadData()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBottomNav() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNav.selectedItemId = R.id.nav_bookmarks
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNav.selectedItemId) {
                return@setOnItemSelectedListener false
            }
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    overridePendingTransition(0, 0)
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(0, 0)
                }
            }
            true
        }
    }
}