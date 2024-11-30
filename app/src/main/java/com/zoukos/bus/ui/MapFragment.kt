package com.zoukos.bus.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
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
import com.zoukos.bus.Coordinates
import com.zoukos.bus.MapWrapperReadyListener
import com.zoukos.bus.MyApplication
import com.zoukos.bus.R
import com.zoukos.bus.Stop
import com.zoukos.bus.Tostaki
import com.zoukos.bus.network.getAllStops
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.zoukos.bus.Map


class MapFragment: Fragment(), MapWrapperReadyListener, GoogleMap.OnMarkerClickListener
{
	private lateinit var map: com.zoukos.bus.Map;
	private lateinit var stopListAdapter: StopListAdapter;
	private var selectedStop:Stop? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
	{
		val ret = inflater.inflate(R.layout.map_fragment, container, false);

		ret.findViewById<Button>(R.id.button).setOnClickListener(this::confirmStop);

		return ret;
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState);

		//Initialize map
		val mapFragment = childFragmentManager.findFragmentById(R.id.locationMapView) as SupportMapFragment?

		map = Map(mapFragment!!, requireContext())
		map.setListener(this)
		map.setMarkerListener(this)

		selectedStop = requireActivity().intent.extras?.getSerializable("stop") as Stop?

		//We do this because if we change favorite status and go back from the map screen (no confirm)....
		//...then the shared preferences change (potentially for many stops), but the returned stop (on the initial screen) does not maintain that change
		//Favorite status is only meant to be used for CACHING PURPOSES inside the map screen
		//We don't necessarily want it to be passed from and to the previous screen. The map screen should ALWAYS check the shared preferences to verify
		selectedStop?.resetFavoriteStop();

		Log.d("BUS", "HELLO " + selectedStop?.name)

		val stopList = view.findViewById<RecyclerView>(R.id.listId)
		stopList.layoutManager = object : LinearLayoutManager(requireContext()) { override fun canScrollVertically() = false }
		stopListAdapter = StopListAdapter(requireContext(), mutableListOf(selectedStop), true)
		stopList.adapter = stopListAdapter
	}

	override fun onResume()
	{
		super.onResume()

		//We came from the favorites screen
		//Check if favorite status of selected stop changed

		if (selectedStop != null)
		{
			selectedStop!!.resetFavoriteStop();
			stopListAdapter.notifyDataSetChanged();
		}
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
		getAllStops(object: Callback<ResponseBody> {

			override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {

				val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);

				var data:String? = "";

				//If I failed to get server data, I will use the cached stops
				if (resp.body() == null){
					//Ok so because these guys are apparently idiots, sometimes the stops cannot be returned
					//I may have to actually cache the stops
					//For now I'll cache them every time I retrieve them, and only use them when this happens
					//Later, I will probably use cache time

					data = sharedPreferences.getString("stops", null)

					if (data == null){
						Tostaki(requireContext(), "Could not refresh stops", Toast.LENGTH_SHORT)
						return;
					}
				}
				//If I got server data successfully
				else{
					data = resp.body()!!.string()

					val editor: SharedPreferences.Editor = sharedPreferences.edit();

					editor.putString("stops", data);
					editor.apply();
				}

				//After getting stop data either way, we filter it
				//------------------------------------------------------------------------
				placeStopsOnMap(data);
			}

			override fun onFailure(p0: Call<ResponseBody>, p1: Throwable) {
				val sharedPreferences: SharedPreferences = MyApplication.appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
				val data = sharedPreferences.getString("stops", null);

				if (data == null){
					Tostaki(requireContext(), "No stops in cache", Toast.LENGTH_SHORT)
				}
				else placeStopsOnMap(data);
			}
		})
	}


	fun placeStopsOnMap(data: String?): Unit{
		val mapper: ObjectMapper = ObjectMapper()
		val root: ArrayNode = mapper.readTree(data) as ArrayNode

		//Filter stops, to only keep routes 6011 and 6091
		val stops = root.filter{
			val routeCodes = it["routeCodes"].asIterable()

			var res:Boolean = false;

			for (node: JsonNode in routeCodes){
				if ((node.asText() == "6011") || (node.asText() == "6091")){
					res = true;
					break;
				}
			}

			res;
		}
			//Change json structure
			.map{
				val jsonObject: ObjectNode = mapper.createObjectNode()

				jsonObject.set<JsonNode>("id", it["id"])
				jsonObject.set<JsonNode>("code", it["code"])
				jsonObject.set<JsonNode>("name", it["name"])
				jsonObject.set<JsonNode>("coords", mapper.createObjectNode().apply{
					set<JsonNode>("lat", it["latitude"])
					set<JsonNode>("lng", it["longitude"])
				})
				jsonObject.set<ArrayNode>("lines", mapper.valueToTree(it["lineCodes"].filter{ stop -> stop.asText() == "601" || stop.asText() == "609"}))

				jsonObject;
			}

		Log.d("JSON", mapper.writeValueAsString((requireActivity() as MapScreen).stops))

		//Placing the stops on the map
		//------------------------------------------------------------------------
		(requireActivity() as MapScreen).stops.clear();

		for (stop in stops){

			val temp: Stop = Stop(stop)
			(requireActivity() as MapScreen).stops.add(temp);

			val color:Float = when{
				temp.lines.contains("601") && temp.lines.contains("609")-> BitmapDescriptorFactory.HUE_VIOLET
				temp.lines.contains("601")-> BitmapDescriptorFactory.HUE_RED
				temp.lines.contains("609")-> BitmapDescriptorFactory.HUE_BLUE
				else->0f
			}

			map.placePin(Coordinates(stop["coords"]), false, color).tag = temp
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	override fun onMarkerClick(marker: Marker): Boolean {
		selectedStop = marker.tag as Stop
		stopListAdapter.stops = mutableListOf(selectedStop)
		//stopListAdapter.notifyItemChanged(0)
		stopListAdapter.notifyDataSetChanged()
		return false
	}

	fun confirmStop(view: View){
		val resultIntent: Intent = Intent();
		val extras: Bundle = Bundle()
		extras.putSerializable("stop", selectedStop)
		extras.putInt("fragment", 0)
		resultIntent.putExtras(extras)

		with (requireActivity())
		{
			if (selectedStop != null)
				setResult(Activity.RESULT_OK, resultIntent);
			else
				setResult(Activity.RESULT_CANCELED, resultIntent);

			finish()
		}
	}
}