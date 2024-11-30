package com.zoukos.bus

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.Serializable

class Stop(stop: JsonNode): Serializable {

	val id:String = stop["id"].asText();
	val code:String = stop["code"].asText();
	val name:String = stop["name"].asText();
	val coords:Coordinates = Coordinates(stop["coords"]);
	val lines:List<String> = stop["lines"].map{it.asText()}
	private var isFavoritePrivate:Boolean? = null;

	//If true, we can update favorite status without affecting the private variable or the shared preferences
	private var simpleFavoriteUpdate:Boolean = false;

	var isFavorite: Boolean = false
		get()
		{
			if (isFavoritePrivate != null)
				return isFavoritePrivate!!;

			val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
			val stops:String? = sharedPreferences.getString("favoriteStops", "");

			val mapper:ObjectMapper = ObjectMapper();
			val json:JsonNode = mapper.readTree(stops);

			if (!json.isEmpty)
			{
				Log.d("PVLN", "I remember you're FAVORITE STOPS");
				Log.d("PVLN", mapper.writeValueAsString(json));

				isFavoritePrivate = json[code]?.asBoolean() ?: false;
				Log.d("PVLN", String.format("Stop: %s Favorite Status: %b", code, isFavoritePrivate!!));
				return isFavoritePrivate!!;
			}
			else
			{
				Log.d("PVLN", "I remember nothing. Welcome to your first playthrough!");
				isFavoritePrivate = false
				return isFavoritePrivate!!;
			}
		}

	 	set(value)
		{
			 if (simpleFavoriteUpdate)
			 {
				 field = value;
				return;
			 }

			isFavoritePrivate = value;
			field = value;

			val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
			val editor: SharedPreferences.Editor = sharedPreferences.edit();
			val stops:String? = sharedPreferences.getString("favoriteStops", null);

			val mapper:ObjectMapper = ObjectMapper();

			var json:ObjectNode? = null;

			if (stops == null)
				json = mapper.createObjectNode();
			else
				json = mapper.readTree(stops) as ObjectNode;

			json!!.put(code, value);
			editor.putString("favoriteStops", mapper.writeValueAsString(json));

			editor.commit();
		}

	fun linesFormatted(): String {
		var str:String = "";

		for (i in lines.indices)
			str += lines[i] + if (i < lines.size - 1) ", " else "";

		return str;
	}

	fun toJsonString(): String{
		val mapper: ObjectMapper = ObjectMapper();
		val jsonObject: ObjectNode = mapper.createObjectNode();

		jsonObject.put("id", id);
		jsonObject.put("code", code);
		jsonObject.put("name", name);
		jsonObject.put("favorite", isFavorite);
		jsonObject.set<JsonNode>("coords", mapper.createObjectNode().apply{
			put("lat", coords.lat)
			put("lng", coords.lng)
		})
		jsonObject.set<ArrayNode>("lines", mapper.valueToTree(lines));
		return mapper.writeValueAsString(jsonObject);
	}

	fun resetFavoriteStop()
	{
		simpleFavoriteUpdate = true;

		isFavorite = false;
		isFavoritePrivate = null;

		simpleFavoriteUpdate = false;
	}

	fun verifyFavoriteStop(): Boolean
	{
		val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
		val stops:String? = sharedPreferences.getString("favoriteStops", "");

		val mapper:ObjectMapper = ObjectMapper();
		val json:JsonNode = mapper.readTree(stops);

		if (!json.isEmpty)
		{
			return json[code]?.asBoolean() ?: false;
		}

		return false;
	}
}
