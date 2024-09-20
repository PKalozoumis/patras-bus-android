package com.zoukos.bus

import android.location.Location
import android.util.Log
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Coordinates : Serializable {
	var lat: Double
	var lng: Double

	//Constructors
	//=====================================================================================================================

	constructor(lat: Double, lng: Double) {
		this.lat = lat
		this.lng = lng
	}

	constructor(coords: Coordinates) {
		this.lat = coords.lat
		this.lng = coords.lng
	}

	constructor(coords: LatLng) {
		this.lat = coords.latitude
		this.lng = coords.longitude
	}

	constructor(coords: JsonNode) {
		this.lat = coords["lat"].asDouble()
		this.lng = coords["lng"].asDouble()
	}

	//=====================================================================================================================

	override fun toString(): String {
		return String.format("(%.7f, %.7f)", lat, lng)
	}

	//=====================================================================================================================

	fun toLatLng(): LatLng {
		return LatLng(this.lat, this.lng)
	}

	//=====================================================================================================================

	fun withinRadius(other: Coordinates, radius: Float): Boolean {
		val center = Location("center")
		center.latitude = lat
		center.longitude = lng

		val point = Location("point")
		point.latitude = other.lat
		point.longitude = other.lng

		val dista = center.distanceTo(point)

		return dista <= radius
	}

	//=====================================================================================================================

	fun distance(other: Coordinates): Float {
		val myloc = Location("myloc")
		myloc.latitude = lat
		myloc.longitude = lng

		val point = Location("point")
		point.latitude = other.lat
		point.longitude = other.lng

		return myloc.distanceTo(point)
	}

	//=====================================================================================================================

	fun coordsToJson(): String {
		val map: MutableMap<String, Double> = HashMap()
		map["lat"] = lat
		map["lng"] = lng
		val jsonString: String
		val mapper = ObjectMapper()
		try {
			jsonString = mapper.writeValueAsString(map)
		}
		catch (e: JsonProcessingException) {
			throw RuntimeException(e)
		}

		return jsonString
	}

	//Static companion object
	//=====================================================================================================================
	companion object {
		@JvmStatic
		fun parseCoords(coordsString: String): Coordinates{
			val coordsString2 = coordsString.replace("POINT(", "").replace(")", "")
			val coords = coordsString2.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

			val lat = coords[0].toDouble()
			val lng = coords[1].toDouble()

			return Coordinates(lat, lng)
		}
	}
}