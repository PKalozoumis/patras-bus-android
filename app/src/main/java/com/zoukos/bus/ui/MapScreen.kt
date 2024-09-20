package com.zoukos.bus.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.gms.maps.SupportMapFragment
import com.zoukos.bus.Coordinates
import com.zoukos.bus.Map
import com.zoukos.bus.MapWrapperReadyListener
import com.zoukos.bus.R
import com.zoukos.bus.network.getAllStops
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapScreen: AppCompatActivity(), MapWrapperReadyListener {

	private lateinit var map: Map;

	@Override
	override fun onCreate(savedInstanceState: Bundle?):Unit{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_screen);

		//Initialize map
		val mapFragment = supportFragmentManager.findFragmentById(R.id.locationMapView) as SupportMapFragment?

		map = Map(mapFragment, this)
		map.setListener(this)
	}

	override fun onMapWrapperReady() {
		val Patra: Coordinates = Coordinates(38.246639, 21.734573)
		map.zoom = 12f;
		map.setPosition(Patra)

		//Retrieve all stops

		getAllStops(object: Callback<ResponseBody>{

			override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {

				val data:String = resp.body()!!.string()

				val mapper: ObjectMapper = ObjectMapper()
				val root: ArrayNode = mapper.readTree(data) as ArrayNode

				val stops = root.filter{
					val routeCodes = it["routeCodes"].asIterable()

					var res:Boolean = false;

					for (node:JsonNode in routeCodes){
						if ((node.asText() == "6011") || (node.asText() == "6091")){
							res = true;
							break;
						}
					}

					res;
				}
				.map{
					var jsonObject:ObjectNode = mapper.createObjectNode()

					jsonObject.set<JsonNode>("id", it["id"])
					jsonObject.set<JsonNode>("name", it["name"])
					jsonObject.set<JsonNode>("coords", mapper.createObjectNode().apply{
						set<JsonNode>("lat", it["latitude"])
						set<JsonNode>("lng", it["longitude"])
					})
					jsonObject.set<ArrayNode>("lines", mapper.valueToTree(it["lineCodes"].filter{stop -> stop.asText() == "601" || stop.asText() == "609"}))

					jsonObject;
				}

				Log.d("JSON", mapper.writeValueAsString(stops))

				for (stop in stops){
					map.placePin(Coordinates(stop["coords"]), false)
				}
			}

			override fun onFailure(p0: Call<ResponseBody>, p1: Throwable) {
				TODO("Not yet implemented")
			}

		})


	}
}