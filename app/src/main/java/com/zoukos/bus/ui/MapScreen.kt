package com.zoukos.bus.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zoukos.bus.Stop
import com.zoukos.bus.Coordinates
import com.zoukos.bus.Map
import com.zoukos.bus.MapWrapperReadyListener
import com.zoukos.bus.MyApplication
import com.zoukos.bus.R
import com.zoukos.bus.Tostaki
import com.zoukos.bus.network.getAllStops
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapScreen: AppCompatActivity()
{
	private val mapFragment:MapFragment = MapFragment();
	private val favoriteFragment:FavoriteFragment = FavoriteFragment();
	var stops: MutableList<Stop> = mutableListOf();

	@Override
	override fun onCreate(savedInstanceState: Bundle?):Unit{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_screen);

		supportActionBar?.hide()

		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation);

		//Inflate default fragment
		//when (intent.extras!!.getInt("fragment"))
		when(0)
		{
			0 -> {
				supportFragmentManager.beginTransaction().replace(R.id.my_fragment, MapFragment()).commit();
				bottomNavigationView.selectedItemId = R.id.option_map;
			}

			1 -> {
				supportFragmentManager.beginTransaction().replace(R.id.my_fragment, FavoriteFragment()).commit();
				bottomNavigationView.selectedItemId = R.id.option_favorites;
			}
		}

		// Add all fragments at the start
		supportFragmentManager.beginTransaction()
			.add(R.id.my_fragment, mapFragment, "MAP")
			.add(R.id.my_fragment, favoriteFragment, "FAV")
			.hide(mapFragment)
			.hide(favoriteFragment)
			.setMaxLifecycle(mapFragment, Lifecycle.State.STARTED)
			.setMaxLifecycle(favoriteFragment, Lifecycle.State.STARTED)
			.commit()

		bottomNavigationView.setOnItemSelectedListener { menuItem ->
			showFragment(menuItem.itemId);
			true;
		}
	}

	fun showFragment(id: Int)
	{
		val fragment = when (id)
		{
			R.id.option_map -> mapFragment
			R.id.option_favorites -> favoriteFragment
			else -> null
		}

		supportFragmentManager.beginTransaction().apply {
			supportFragmentManager.fragments.forEach {
				hide(it);
				setMaxLifecycle(it, Lifecycle.State.STARTED);
			}

			show(fragment!!) // Show the selected fragment
			setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
			commit()
		}
	}
}