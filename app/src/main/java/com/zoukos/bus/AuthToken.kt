package com.zoukos.bus

import android.content.Context
import android.util.Log
import com.zoukos.bus.network.ApiService
import com.zoukos.bus.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import android.content.SharedPreferences
import okhttp3.ResponseBody

class UninitializedTokenException(
	message: String = "Cannot access token when it hasn't been initialized. Make sure to call initToken() first")
	: Exception(message)

object AuthToken {

	private var token: String? = null;

	fun getToken(): String{
		if (token == null)
			throw UninitializedTokenException()

		return token!!;
	}

	//===========================================================================================

	public fun initToken(callback: GenericCallback<String>): Unit{

		if (token == null){
			val tok:String? = loadFromDatabase();

			//If there is no token in the database, retrieve it from the server
			if (tok == null){
				refreshToken(object: GenericCallback<String>{
					override fun onSuccess(data: String) {
						token = data;
						callback.onSuccess(token!!)
					}

					//Server error
					override fun onFailure(e: Exception?) {
						Log.d("BUS", "initToken failure")
						callback.onFailure(e);
					}

				})
			}
			//Found token in the database
			else{
				token = tok;
				callback.onSuccess(token!!);
			}
		}
		else{
			callback.onSuccess(token!!)
		}
	}

	//===========================================================================================

	public fun refreshToken(callback: GenericCallback<String>): Unit{
		//Get token from remote server
		val apiService: ApiService = RetrofitClient.apiService;
		val call: Call<ResponseBody> = apiService.requestToken();

		call.enqueue(object: Callback<ResponseBody>{
			override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {

				val html:String = resp.body()?.string() ?: ""

				val reg: Regex = Regex(".*const token = '(.*)'.*")
				val match: MatchResult? = reg.find(html);

				val newToken: String? = match?.groups?.get(1)?.value;

				if (newToken != null){

					Log.d("FRIEND INSIDE ME", "Extracted token $newToken from HTML")

					token = newToken;
					storeToDatabase(newToken)
					callback.onSuccess(newToken);
				}
				else{
					callback.onFailure(Exception("Could not get token from html"));
				}

			}

			override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
				Log.d("BUS", "refreshToken failure")
				throw t;
				callback.onFailure(Exception(t.message));
			}

		});
	}

	//===========================================================================================

	private fun loadFromDatabase(): String?{
		//Get token from database
		val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
		val tok:String? = sharedPreferences.getString("token", null)

		return tok;
	}

	//===========================================================================================

	private fun storeToDatabase(newToken: String): Unit{
		val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
		val editor: SharedPreferences.Editor = sharedPreferences.edit();

		editor.putString("token", newToken);
		editor.apply();
	}

	//===========================================================================================

	private fun clearDatabase() {
		val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
		val editor: SharedPreferences.Editor = sharedPreferences.edit()
		editor.clear()
		editor.apply()
	}
}