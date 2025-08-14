package com.example.projectfinal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectfinal.adapter.DiscoverAdapter
import com.example.projectfinal.adapter.OnMovieClickListener
import com.example.projectfinal.api.Genre
import com.example.projectfinal.api.GenreResponse
import com.example.projectfinal.api.Movie
import com.example.projectfinal.api.MovieResponse
import com.example.projectfinal.api.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiscoverActivity : AppCompatActivity(), OnMovieClickListener {

    private lateinit var discoverAdapter: DiscoverAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var rvDiscover: RecyclerView
    private lateinit var layoutManager: GridLayoutManager

    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false

    private var fullMovieList: MutableList<Movie> = mutableListOf()
    private var allGenres: List<Genre> = listOf()
    private val apiKey = "174e03eb18b4d165185e02ddcc8932a8"
    private val desiredGenres = setOf("ACTION", "COMEDY", "HORROR", "ROMANCE", "FANTASY", "ANIMATION", "ADVENTURE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)
        setupViews()
        setupScrollListener()
        fetchGenresAndInitialMovies()
        setupChipGroupListener()
    }

    override fun onResume() {
        super.onResume()
        if (::discoverAdapter.isInitialized) discoverAdapter.notifyDataSetChanged()
    }

    private fun setupViews() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        chipGroup = findViewById(R.id.chipGroup)
        rvDiscover = findViewById(R.id.rvDiscoverMovies)

        discoverAdapter = DiscoverAdapter(mutableListOf(), this)
        layoutManager = GridLayoutManager(this, 2)
        rvDiscover.layoutManager = layoutManager
        rvDiscover.adapter = discoverAdapter

        setupBottomNav()
    }

    private fun setupScrollListener() {
        rvDiscover.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        currentPage++
                        fetchMovies(currentPage)
                    }
                }
            }
        })
    }

    private fun fetchGenresAndInitialMovies() {
        RetrofitClient.instance.getGenres(apiKey).enqueue(object : Callback<GenreResponse> {
            override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                if (response.isSuccessful) {
                    allGenres = response.body()?.genres ?: emptyList()
                    setupGenreChips()
                }
            }
            override fun onFailure(call: Call<GenreResponse>, t: Throwable) {}
        })
        fetchMovies(currentPage)
    }

    private fun fetchMovies(page: Int) {
        isLoading = true
        RetrofitClient.instance.getPopularMovies(apiKey, page).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val newMovies = response.body()?.movies ?: emptyList()
                    if (newMovies.isNotEmpty()) {
                        discoverAdapter.addMovies(newMovies)
                        fullMovieList.addAll(newMovies)
                    } else {
                        isLastPage = true
                    }
                }
                isLoading = false
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                isLoading = false
            }
        })
    }

    private fun setupBottomNav() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_home) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                overridePendingTransition(0, 0)
                return@setOnItemSelectedListener true
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

    private fun setupChipGroupListener() {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                group.check(R.id.chip_all)
                return@setOnCheckedStateChangeListener
            }
            applyGenreFilter()
        }
    }

    private fun applyGenreFilter() {
        if (chipGroup.checkedChipId == -1) return
        val selectedChip = chipGroup.findViewById<Chip>(chipGroup.checkedChipId) ?: return
        val genreId = selectedChip.tag as Int
        val filteredList = if (genreId == -1) fullMovieList else fullMovieList.filter { it.genreIds.contains(genreId) }
        discoverAdapter.updateMovies(filteredList)
    }

    override fun onMovieClick(movieId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("MOVIE_ID", movieId)
        startActivity(intent)
    }

    private fun setupGenreChips() {
        chipGroup.removeAllViews()
        chipGroup.addView(createChip("ALL", R.id.chip_all, -1))
        val filteredGenres = allGenres.filter { desiredGenres.contains(it.name.uppercase()) }
        filteredGenres.forEach { genre ->
            chipGroup.addView(createChip(genre.name.uppercase(), genre.id, genre.id))
        }
        chipGroup.check(R.id.chip_all)
    }

    private fun createChip(text: String, chipId: Int, genreTag: Int): Chip {
        val styledContext = ContextThemeWrapper(this, R.style.CustomChipChoice)
        val chip = Chip(styledContext)
        chip.text = text
        chip.id = chipId
        chip.tag = genreTag
        chip.isCheckable = true
        return chip
    }
}