package com.zoukos.bus

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.ArrayList
import kotlin.streams.toList

class Map(mapFragment: SupportMapFragment?, context: Context) : OnMapReadyCallback {
	private var gmap: GoogleMap? = null

	private val view: View =
		(context as Activity).findViewById(R.id.content) //Usually ConstraintLayout
	private val mapFragment: SupportMapFragment? =
		mapFragment //SupportMapFragment or ScrollMapFragment

	var isClickable: Boolean = false
	private var pinCoords: Coordinates? = null
	private var startCoords: Coordinates? = null
	private var markerListener: GoogleMap.OnMarkerClickListener?
	private var clickListener: GoogleMap.OnMapClickListener?
	private var clickedMarker: Marker?

	private var polygonCoords: ArrayList<ArrayList<Coordinates>>? = null
	private var polygons: ArrayList<Polygon>? = null
	private var selectedPolygonPos = -1 //Will be set only after calling withinPolygon()

	private var listener: MapWrapperReadyListener? = null

	val map: GoogleMap?
		get() = gmap

	var zoom: Float
		get() = gmap!!.cameraPosition.zoom
		set(zoomLevel) {
			gmap!!.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel))
		}

	val selectedPolygonCoords: ArrayList<Coordinates>?
		get() = if (selectedPolygonPos != -1) polygonCoords!![selectedPolygonPos]
		else null

	companion object {
		fun withinPolygon(
			point: Coordinates,
			polygonCoords: java.util.ArrayList<Coordinates?>
		): Boolean {
			val list = polygonCoords.map {(Coordinates::toLatLng)(it!!)}
			return PolyUtil.containsLocation(point.toLatLng(), list, false)
		}
	}

	init {
		this.markerListener = null
		this.clickedMarker = null
		this.clickListener = null

		assert(mapFragment != null)
		mapFragment?.getMapAsync(this as OnMapReadyCallback)
	}

	constructor(
		mapFragment: SupportMapFragment?,
		context: Context,
		listener: MapWrapperReadyListener?
	): this(mapFragment, context) {
		this.listener = listener
	}

	@SuppressLint("PotentialBehaviorOverride")
	@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
	override fun onMapReady(googleMap: GoogleMap) {
		this.gmap = googleMap

		//Defaults
		gmap!!.getUiSettings().setRotateGesturesEnabled(false)

		//Initialize clicking
		//THIS IS THE DEFAULT CLICKER. YOU CAN ASSIGN OTHER CLICK HANDLER IF YOU WANT
		gmap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
			override fun onMapClick(latLng: LatLng) {
				if (this@Map.isClickable) {
					if (clickedMarker == null) clickedMarker = placePin(Coordinates(latLng), false)
					else {
						clickedMarker!!.setPosition(latLng)
						pinCoords = Coordinates(latLng)
					}
				}
			}
		})

		//Set listeners if we have previously called setMarkerListener/ setClickListener
		gmap!!.setOnMarkerClickListener(markerListener)

		//Set polygon
		if (this.polygonCoords != null) {
			for (singlePolygonCoords in polygonCoords!!) {

				val list = singlePolygonCoords.map {(Coordinates::toLatLng)(it)}

				polygons!!.add(
					gmap!!.addPolygon(PolygonOptions()
						.addAll(list)
						.strokeColor(-0xffff01)
					)
				)
			}
		}

		if (clickListener != null) gmap!!.setOnMapClickListener(clickListener)

		listener?.onMapWrapperReady()
	}

	//===========================================================================================================

	fun setListener(listener: MapWrapperReadyListener?) {
		this.listener = listener
	}

	//===========================================================================================================

	fun placePin(coords: Coordinates, clear: Boolean): Marker? {
		if (clear) gmap!!.clear()

		clickedMarker = gmap?.addMarker(MarkerOptions().position(coords.toLatLng()))
		pinCoords = coords

		return clickedMarker
	}

	//===========================================================================================================

	fun placePin(coords: Coordinates, clear: Boolean, iconId: Int): Marker? {
		if (clear) gmap!!.clear()

		val b = BitmapFactory.decodeResource(mapFragment?.resources, iconId)
		val smallMarker = Bitmap.createScaledBitmap(b, 128, 128, false)

		val opt: MarkerOptions = MarkerOptions()
		opt.position(coords.toLatLng())
		opt.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))

		clickedMarker = gmap!!.addMarker(opt)

		pinCoords = coords

		return clickedMarker
	}

	//===========================================================================================================

	fun placePin(coords: Coordinates, clear: Boolean, iconId: Int, draggable: Boolean): Marker? {
		if (clear) gmap!!.clear()

		val b = BitmapFactory.decodeResource(mapFragment?.resources, iconId)
		val smallMarker = Bitmap.createScaledBitmap(b, 128, 128, false)

		val opt: MarkerOptions = MarkerOptions()
		opt.draggable(draggable)
		opt.position(coords.toLatLng())
		opt.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))

		clickedMarker = gmap?.addMarker(opt)

		pinCoords = coords

		return clickedMarker
	}

	//===========================================================================================================

	fun placeStartPin(coords: Coordinates, clear: Boolean, iconId: Int) {
		if (clear) gmap!!.clear()

		val b = BitmapFactory.decodeResource(mapFragment?.resources, iconId)
		val smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false)
		startCoords = coords
		val opt: MarkerOptions = MarkerOptions()
		opt.position(coords.toLatLng())
		opt.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
		opt.title("Your Location")

		gmap?.addMarker(opt)
	}

	//===========================================================================================================

	fun setPosition(coords: Coordinates) {
		gmap!!.moveCamera(CameraUpdateFactory.newLatLng(coords.toLatLng()))
	}

	//===========================================================================================================

	fun smoothTransition(coords: Coordinates) {
		gmap!!.animateCamera(CameraUpdateFactory.newLatLng(coords.toLatLng()))
	}

	//===========================================================================================================

	fun smoothTransition(coords: Coordinates, zoom: Float) {
		gmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(coords.toLatLng(), zoom))
	}

	//===========================================================================================================

	fun getPinCoords(): Coordinates? {
		return pinCoords?.let { Coordinates(it) }
	}

	//===========================================================================================================

	@SuppressLint("PotentialBehaviorOverride")
	fun setMarkerListener(listener: GoogleMap.OnMarkerClickListener?) {
		this.markerListener = listener

		if (this.gmap != null) {
			gmap!!.setOnMarkerClickListener(listener)
		}
	}

	//===========================================================================================================

	fun setClickListener(listener: GoogleMap.OnMapClickListener?) {
		this.clickListener = listener

		if (this.gmap != null) {
			gmap!!.setOnMapClickListener(listener)
		}
	}

	//===========================================================================================================

	fun addPolygon(polygonCoords: java.util.ArrayList<Coordinates>) {
		if (this.polygonCoords == null) {
			this.polygonCoords = java.util.ArrayList<java.util.ArrayList<Coordinates>>()
			this.polygons = java.util.ArrayList<Polygon>()
		}

		this.polygonCoords!!.add(polygonCoords)

		if (gmap != null) {

			val list = polygonCoords.map{(Coordinates::toLatLng)(it)}

			polygons!!.add(
				gmap!!.addPolygon(
					PolygonOptions()
						.addAll(list)
						.strokeColor(-0xffff01)
				)
			)
		}
	}

	//===========================================================================================================

	fun withinPolygon(coords: Coordinates): Boolean {
		assert(polygons != null)
		var i = 0

		for (singlePolygonCoords in polygonCoords!!) {

			val list = singlePolygonCoords.map{(Coordinates::toLatLng)(it)}

			if (PolyUtil.containsLocation(coords.toLatLng(), list, false)) {
				this.selectedPolygonPos = i
				return true
			}

			i++
		}

		return false
	}

	//===========================================================================================================

	fun withinPolygon(): Boolean {
		assert(polygons != null)
		var i = 0

		for (singlePolygonCoords in polygonCoords!!) {
			val list = singlePolygonCoords.map{(Coordinates::toLatLng)(it)}

			if (PolyUtil.containsLocation(pinCoords!!.toLatLng(), list, false)) {
				this.selectedPolygonPos = i
				return true
			}

			i++
		}

		return false
	}

	//===========================================================================================================

	fun withinSelectedPolygon(): Boolean {
		val selectedPolygonCoords: java.util.ArrayList<Coordinates> = polygonCoords!![selectedPolygonPos]
		val list = selectedPolygonCoords.map {(Coordinates::toLatLng)(it)}
		return PolyUtil.containsLocation(pinCoords!!.toLatLng(), list, false)
	}

	//===========================================================================================================
}