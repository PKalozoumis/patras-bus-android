package com.zoukos.bus

import android.content.Context
import android.widget.Toast

class Tostaki(context:Context, text:CharSequence, duration:Int) {

	init{
		toast = Toast.makeText(context, text, duration)
	}

	private companion object{
		@JvmStatic
		var toast: Toast? = null
			set(value){
				field?.cancel()
				field = value
				field!!.show()
			}

	}
}