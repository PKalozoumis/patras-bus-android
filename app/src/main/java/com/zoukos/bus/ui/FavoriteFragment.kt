package com.zoukos.bus.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zoukos.bus.R
import com.zoukos.bus.Stop

class FavoriteFragment: Fragment() {

	private lateinit var stopListAdapter: StopListAdapter;

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
	{
		val ret = inflater.inflate(R.layout.favorite_fragment, container, false);
		return ret;
	}

	override fun onResume()
	{
		super.onResume()

		Log.d("DISTA", "These are the distas:");

		//Filter stops
		val favoriteStops: MutableList<Stop?> = (requireActivity() as MapScreen).stops.filter{
			it.verifyFavoriteStop()
		}.toMutableList();

		//Testing
		for (stop in favoriteStops)
		{
			Log.d("DISTA", stop!!.toJsonString());
		}

		//Initialize list
		val stopList = view?.findViewById<RecyclerView>(R.id.listId)
		stopList?.layoutManager = object : LinearLayoutManager(requireContext()) { override fun canScrollVertically() = true }
		stopListAdapter = StopListAdapter(requireActivity(), favoriteStops, false)
		stopList?.adapter = stopListAdapter
	}
}