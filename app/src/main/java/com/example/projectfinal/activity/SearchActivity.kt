package com.example.projectfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectfinal.adapter.LatestMoviesAdapter
import com.example.projectfinal.adapter.OnMovieClickListener
import com.example.projectfinal.api.Genre
import com.example.projectfinal.api.GenreResponse
import com.example.projectfinal.api.Movie
import com.example.projectfinal.api.MovieResponse
import com.example.projectfinal.api.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), OnMovieClickListener {

    private val TAG = "SearchActivity_Debug"
    private lateinit var searchAdapter: LatestMoviesAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var searchView: SearchView
    private lateinit var tvSearchResults: TextView
    private lateinit var rvSearchMovies: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var currentQuery: String = ""
    private var selectedGenreId: Int = -1
    private var searchJob: Job? = null

    private var fullSearchResultList: MutableList<Movie> = mutableListOf()
    private var allGenres: List<Genre> = listOf()
    private val apiKey = "174e03eb18b4d165185e02ddcc8932a8"
    private val desiredGenres = setOf("ACTION", "COMEDY", "HORROR", "ROMANCE", "FANTASY", "ANIMATION", "ADVENTURE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupViews()
        setupScrollListener()
        fetchGenres()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (::searchAdapter.isInitialized) searchAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Hàm này vẫn giữ lại để xử lý khi người dùng nhấn enter
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob?.cancel() // Hủy job cũ (nếu có)
                handleNewSearch(query)
                searchView.clearFocus()
                return true
            }

            // =========================================================
            // ==           SỬA LỖI QUAN TRỌNG Ở ĐÂY                  ==
            // =========================================================
            // Giờ chúng ta sẽ xử lý tìm kiếm tự động ở đây
            override fun onQueryTextChange(newText: String?): Boolean {
                val searchQuery = newText.orEmpty().trim()
                searchJob?.cancel() // Hủy bỏ job tìm kiếm trước đó

                // Chỉ bắt đầu tìm kiếm nếu người dùng đã gõ gì đó
                if (searchQuery.isNotEmpty()) {
                    searchJob = lifecycleScope.launch {
                        delay(500L) // Đợi 500 mili giây
                        handleNewSearch(searchQuery)
                    }
                } else {
                    // Nếu người dùng xóa hết chữ, hãy xóa kết quả
                    handleNewSearch("")
                }
                return true
            }
        })

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                selectedGenreId = selectedChip.tag as Int
                displayFilteredResults()
            }
        }
    }

    // Các hàm còn lại giữ nguyên
    private fun setupViews() {
        searchView = findViewById(R.id.search_view)
        chipGroup = findViewById(R.id.chipGroup_search)
        tvSearchResults = findViewById(R.id.tv_search_results)
        rvSearchMovies = findViewById(R.id.rv_search_movies)
        searchAdapter = LatestMoviesAdapter(mutableListOf(), this)
        layoutManager = LinearLayoutManager(this)
        rvSearchMovies.layoutManager = layoutManager
        rvSearchMovies.adapter = searchAdapter
        setupBottomNav()
    }
    private fun setupScrollListener() {
        rvSearchMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage && currentQuery.isNotEmpty()) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        currentPage++
                        performSearch(currentQuery, currentPage)
                    }
                }
            }
        })
    }
    private fun handleNewSearch(query: String?) {
        val newQuery = query?.trim() ?: ""
        currentQuery = newQuery
        currentPage = 1
        isLastPage = false
        fullSearchResultList.clear()
        displayFilteredResults()
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery, currentPage)
        }
    }
    private fun performSearch(query: String, page: Int) {
        if (query.isBlank()) return
        isLoading = true
        RetrofitClient.instance.searchMovies(apiKey, query, page).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val newMovies = response.body()?.movies ?: emptyList()
                    if (newMovies.isNotEmpty()) {
                        fullSearchResultList.addAll(newMovies)
                        displayFilteredResults()
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
    private fun displayFilteredResults() {
        val filteredList = if (selectedGenreId == -1) fullSearchResultList else fullSearchResultList.filter { it.genreIds.contains(selectedGenreId) }
        searchAdapter.updateMovies(filteredList)
        tvSearchResults.text = "Search results (${filteredList.size})"
    }
    private fun setupBottomNav() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNav.selectedItemId = R.id.nav_search
        bottomNav.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == bottomNav.selectedItemId) return@setOnItemSelectedListener false
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
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
    override fun onMovieClick(movieId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("MOVIE_ID", movieId)
        startActivity(intent)
    }
    private fun fetchGenres() {
        RetrofitClient.instance.getGenres(apiKey).enqueue(object : Callback<GenreResponse> {
            override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                if (response.isSuccessful) {
                    allGenres = response.body()?.genres ?: emptyList()
                    setupGenreChips()
                }
            }
            override fun onFailure(call: Call<GenreResponse>, t: Throwable) {}
        })
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