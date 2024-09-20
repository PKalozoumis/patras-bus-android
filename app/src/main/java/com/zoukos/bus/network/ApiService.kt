package com.zoukos.bus.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {
	@GET("/api/v1/el/112/stops/live/{selected_stop}")
	@Headers("Accept: application/json; charset=utf-8")
	fun stopData(@Path(value="selected_stop") stop: String, @Header("Authorization") token: String): Call<ResponseBody>;

	@GET("https://patra.citybus.gr/el/stops")
	fun requestToken(): Call<ResponseBody>;

	@GET("/api/v1/el/112/stops")
	@Headers("Accept: application/json; charset=utf-8")
	fun allStops(@Header("Authorization") token: String): Call<ResponseBody>;
}