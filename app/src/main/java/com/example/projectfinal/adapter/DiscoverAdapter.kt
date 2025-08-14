package com.example.projectfinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectfinal.R
import com.example.projectfinal.api.Movie
import com.example.projectfinal.manager.AuthManager
import com.example.projectfinal.manager.BookmarkManager

class DiscoverAdapter(
    private var movies: MutableList<Movie>,
    private val listener: OnMovieClickListener
) : RecyclerView.Adapter<DiscoverAdapter.MovieViewHolder>() {

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val posterImage: ImageView = view.findViewById(R.id.ivPoster)
        private val titleText: TextView = view.findViewById(R.id.tvTitle)
        private val ratingText: TextView = view.findViewById(R.id.tvRating)
        private val bookmarkIcon: ImageView = view.findViewById(R.id.iv_bookmark)

        fun bind(movie: Movie, listener: OnMovieClickListener) {
            titleText.text = movie.title
            ratingText.text = String.format("%.1f", movie.voteAverage / 2)
            val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
            Glide.with(itemView.context).load(posterUrl).into(posterImage)
            itemView.setOnClickListener { listener.onMovieClick(movie.id) }
            updateBookmarkIcon(movie.id)
            bookmarkIcon.setOnClickListener {
                if (AuthManager.isUserLoggedIn()) {
                    if (BookmarkManager.isBookmarked(movie.id)) {
                        BookmarkManager.removeBookmark(movie.id)
                        Toast.makeText(itemView.context, "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                    } else {
                        BookmarkManager.addBookmark(movie.id)
                        Toast.makeText(itemView.context, "Added to bookmarks", Toast.LENGTH_SHORT).show()
                    }
                    updateBookmarkIcon(movie.id)
                } else {
                    Toast.makeText(itemView.context, "Đăng nhập để lưu trữ", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun updateBookmarkIcon(movieId: Int) {
            if (BookmarkManager.isBookmarked(movieId)) {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)
                bookmarkIcon.setColorFilter(itemView.context.getColor(R.color.yellow))
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
                bookmarkIcon.setColorFilter(itemView.context.getColor(android.R.color.white))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_discover, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position], listener)
    }

    override fun getItemCount() = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun addMovies(newMovies: List<Movie>) {
        val startPosition = movies.size
        movies.addAll(newMovies)
        notifyItemRangeInserted(startPosition, newMovies.size)
    }
}