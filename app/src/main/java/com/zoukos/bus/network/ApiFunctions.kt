package com.zoukos.bus.network

import android.util.Log
import com.zoukos.bus.AuthToken
import com.zoukos.bus.GenericCallback
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback


fun getStopData(stop: String, callback: Callback<ResponseBody>): Unit{
	val apiService: ApiService = RetrofitClient.apiService;
	AuthToken.initToken(object: GenericCallback<String> {
		override fun onSuccess(data: String) {
			val call: Call<ResponseBody> = apiService.stopData(stop, "Bearer $data");
			call.enqueue(callback);
		}

		//Could not get token for some reason
		override fun onFailure(e: Exception?) {
			Log.d("BUS", "FAIILURE FOR SOME REASON")
		}
	})

}