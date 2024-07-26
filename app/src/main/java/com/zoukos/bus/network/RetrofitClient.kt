package com.zoukos.bus.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Request;
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
	private const val BASE_URL = "https://rest.citybus.gr/"

	private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
		.apply{setLevel(HttpLoggingInterceptor.Level.HEADERS)};

	private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
		.addInterceptor(loggingInterceptor)
		.build()

	val apiService: ApiService by lazy {
		Retrofit.Builder()
			.baseUrl(BASE_URL)
			.client(okHttpClient)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
			.create(ApiService::class.java)
	}
}