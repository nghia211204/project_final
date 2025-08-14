package com.example.projectfinal.api

import com.google.gson.annotations.SerializedName

data class Cast(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?
)