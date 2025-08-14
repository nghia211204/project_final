package com.example.projectfinal.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("movie/top_rated")
    fun getTopRatedMovies(@Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("movie/now_playing")
    fun getNowPlayingMovies(@Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Call<MovieResponse>

    @GET("search/movie")
    fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int
    ): Call<MovieResponse>

    @GET("genre/movie/list")
    fun getGenres(@Query("api_key") apiKey: String): Call<GenreResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String): Call<MovieDetail>

    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<CreditsResponse>
}