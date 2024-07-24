package com.zoukos.bus

import android.location.Location

class ListEntry constructor(
	val lineCode: String,
	val lineName: String,
	val min: Byte,
	val sec: Byte,
	val coords: Location
){
	constructor(lineCode: String, lineName: String, min: Byte, sec: Byte, lat: Double, lng: Double)
			:this(lineCode, lineName, min, sec, Location("me").apply{latitude = lat; longitude=lng})
}