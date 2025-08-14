package com.example.projectfinal.api

import com.google.gson.annotations.SerializedName

data class CreditsResponse(
    @SerializedName("cast") val cast: List<Cast>
)