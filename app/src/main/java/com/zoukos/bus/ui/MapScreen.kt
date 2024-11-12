package com.zoukos.bus.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.zoukos.bus.Stop
import com.zoukos.bus.Coordinates
import com.zoukos.bus.Map
import com.zoukos.bus.MapWrapperReadyListener
import com.zoukos.bus.R
import com.zoukos.bus.Tostaki
import com.zoukos.bus.network.getAllStops
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapScreen: AppCompatActivity(), MapWrapperReadyListener, GoogleMap.OnMarkerClickListener {

	private lateinit var map: Map;
	private lateinit var stopListAdapter: StopListAdapter;
	private var selectedStop:Stop? = null

	@Override
	override fun onCreate(savedInstanceState: Bundle?):Unit{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_screen);

		getSupportActionBar()?.hide()

		//Initialize map
		val mapFragment = supportFragmentManager.findFragmentById(R.id.locationMapView) as SupportMapFragment?

		map = Map(mapFragment, this)
		map.setListener(this)
		map.setMarkerListener(this)

		selectedStop = intent.extras?.getSerializable("stop") as Stop?
		Log.d("BUS", "HELLO " + selectedStop?.name)

		val stopList = findViewById<RecyclerView>(R.id.listId)
		stopList.layoutManager = object : LinearLayoutManager(this) { override fun canScrollVertically() = false }
		stopListAdapter = StopListAdapter(this, selectedStop)
		stopList.adapter = stopListAdapter
	}

	override fun onMapWrapperReady() {

		if (selectedStop == null){
			val Patra: Coordinates = Coordinates(38.246639, 21.734573)
			map.zoom = 12f;
			map.setPosition(Patra)
		}
		else{
			map.zoom = 16f;
			map.setPosition(selectedStop!!.coords)
		}


		//Retrieve all stops

		getAllStops(object: Callback<ResponseBody>{

			override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {

				if (resp.body() == null){
					//Ok so because these guys are apparently idiots, sometimes the stops cannot be returned
					//I may have to actually cache the stops
					//For now I'll cache them every time I retrieve them, and only use them when this happens
					//Later, I will probably use cache time
					Tostaki(this@MapScreen, "Could not retrieve stops", Toast.LENGTH_SHORT)
					return;
				}
				val data:String = resp.body()!!.string()

				val mapper: ObjectMapper = ObjectMapper()
				val root: ArrayNode = mapper.readTree(data) as ArrayNode


				//Filter stops, to only keep routes 6011 and 6091
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
				//Change json structure
				.map{
					var jsonObject:ObjectNode = mapper.createObjectNode()

					jsonObject.set<JsonNode>("id", it["id"])
					jsonObject.set<JsonNode>("code", it["code"])
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

					val temp:Stop = Stop(stop)

					val color:Float = when{
						temp.lines.contains("601") && temp.lines.contains("609")->BitmapDescriptorFactory.HUE_VIOLET
						temp.lines.contains("601")->BitmapDescriptorFactory.HUE_RED
						temp.lines.contains("609")->BitmapDescriptorFactory.HUE_BLUE
						else->0f
					}

					map.placePin(Coordinates(stop["coords"]), false, color).tag = temp
				}
			}

			override fun onFailure(p0: Call<ResponseBody>, p1: Throwable) {
				TODO("Not yet implemented")
			}

		})


	}

	@SuppressLint("NotifyDataSetChanged")
	override fun onMarkerClick(marker: Marker): Boolean {
		selectedStop = marker.tag as Stop
		stopListAdapter.stop = selectedStop
		//stopListAdapter.notifyItemChanged(0)
		stopListAdapter.notifyDataSetChanged()
		return false
	}

	public fun confirmStop(view: View){
		val resultIntent: Intent = Intent();
		val extras: Bundle = Bundle()
		extras.putSerializable("stop", selectedStop)
		resultIntent.putExtras(extras)

		if (selectedStop != null)
			setResult(Activity.RESULT_OK, resultIntent);
		else
			setResult(Activity.RESULT_CANCELED, resultIntent);

		finish()
	}
}