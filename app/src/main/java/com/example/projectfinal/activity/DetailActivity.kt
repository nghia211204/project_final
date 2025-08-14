package com.example.projectfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectfinal.adapter.CastAdapter
import com.example.projectfinal.api.CreditsResponse
import com.example.projectfinal.api.MovieDetail
import com.example.projectfinal.api.RetrofitClient
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    private val apiKey = "174e03eb18b4d165185e02ddcc8932a8"

    private lateinit var ivBackdrop: ImageView
    private lateinit var tvMovieTitle: TextView
    private lateinit var tvMovieRating: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvMovieGenres: TextView
    private lateinit var tvMovieOverview: TextView
    private lateinit var collapsingToolbar: CollapsingToolbarLayout

    private lateinit var rvCast: RecyclerView
    private lateinit var castAdapter: CastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        bindViews()
        setupBottomNav()

        val movieId = intent.getIntExtra("MOVIE_ID", -1)

        if (movieId != -1) {
            fetchMovieDetails(movieId)
            fetchMovieCredits(movieId)
        } else {
            Log.e("DetailActivity", "FATAL ERROR: No movie ID was provided.")
            Toast.makeText(this, "Error: Movie not found.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun bindViews() {
        ivBackdrop = findViewById(R.id.iv_backdrop)
        tvMovieTitle = findViewById(R.id.tv_movie_title)
        tvMovieRating = findViewById(R.id.tv_movie_rating)
        ratingBar = findViewById(R.id.rating_bar)
        tvMovieGenres = findViewById(R.id.tv_movie_genres)
        tvMovieOverview = findViewById(R.id.tv_movie_overview)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)

        rvCast = findViewById(R.id.rv_cast)
        castAdapter = CastAdapter(emptyList())
        rvCast.adapter = castAdapter
    }

    private fun setupBottomNav() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_view)

        bottomNav.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNav.menu.size()) {
            bottomNav.menu.getItem(i).isChecked = false
        }
        bottomNav.menu.setGroupCheckable(0, true, true)

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    overridePendingTransition(0, 0)
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.nav_bookmarks -> {
                    startActivity(Intent(this, BookmarksActivity::class.java))
                    overridePendingTransition(0, 0)
                }
            }
            false
        }
    }

    private fun fetchMovieDetails(movieId: Int) {
        RetrofitClient.instance.getMovieDetails(movieId, apiKey).enqueue(object : Callback<MovieDetail> {
            override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                if (response.isSuccessful) {
                    response.body()?.let { movieDetail -> updateUI(movieDetail) }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load movie details.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Network error.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchMovieCredits(movieId: Int) {
        RetrofitClient.instance.getMovieCredits(movieId, apiKey).enqueue(object : Callback<CreditsResponse> {
            override fun onResponse(call: Call<CreditsResponse>, response: Response<CreditsResponse>) {
                if (response.isSuccessful) {
                    val castList = response.body()?.cast ?: emptyList()
                    castAdapter.updateCast(castList)
                }
            }
            override fun onFailure(call: Call<CreditsResponse>, t: Throwable) {
                Log.e("DetailActivity", "Failed to fetch movie credits", t)
            }
        })
    }

    private fun updateUI(movie: MovieDetail) {
        collapsingToolbar.title = movie.title
        tvMovieTitle.text = movie.title
        tvMovieOverview.text = if (movie.overview.isNotBlank()) movie.overview else "No overview available for this movie."

        val imageUrl = movie.backdropPath ?: movie.posterPath
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w1280$imageUrl")
            .placeholder(R.color.material_grey_800)
            .error(R.color.material_grey_800)
            .into(ivBackdrop)

        val ratingValue = movie.voteAverage.toFloat() / 2
        tvMovieRating.text = String.format("%.1f", ratingValue)
        ratingBar.rating = ratingValue

        tvMovieGenres.text = movie.genres.joinToString(", ") { it.name }
    }
}