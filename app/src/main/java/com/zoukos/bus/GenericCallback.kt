package com.zoukos.bus

interface GenericCallback<T> {
	fun onSuccess(data: T)
	fun onFailure(e: Exception?)
}