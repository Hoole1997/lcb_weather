package com.example.lcb.app.weather.data.remote

import com.example.lcb.app.weather.data.remote.dto.IpLocationDto
import retrofit2.http.GET

interface IpGeolocationApi {
    @GET("json/")
    suspend fun getLocation(): IpLocationDto
}
