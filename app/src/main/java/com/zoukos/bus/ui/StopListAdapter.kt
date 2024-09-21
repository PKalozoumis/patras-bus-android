package com.zoukos.bus.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.zoukos.bus.R
import com.zoukos.bus.Stop
import java.lang.String
import java.time.format.DateTimeFormatter

class StopListAdapter(private var context:Context, public var stop:Stop?): RecyclerView.Adapter<RecyclerView.ViewHolder?>(), View.OnClickListener {

	//View Holders
	//====================================================================================================================
	inner class StopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val icon: ImageView = view.findViewById<ImageView>(R.id.icon)
		val nameField: TextView = view.findViewById<TextView>(R.id.stopName)
		val idField: TextView = view.findViewById<TextView>(R.id.idField)
		val linesField: TextView = view.findViewById<TextView>(R.id.linesField)
	}

	inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
	}

	//Overrides
	//====================================================================================================================

	override fun getItemViewType(position: Int): Int {
		return if (stop == null) 0 else 1
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		if (viewType == 1) {
			val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.stop_list_item, parent, false)
			return StopViewHolder(itemView)
		}
		else {
			val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.stop_list_item_empty, parent, false)
			return EmptyViewHolder(itemView)
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

		if (holder !is StopViewHolder)
			return;

		holder.nameField.text = stop?.name ?: "NULL"
		holder.idField.text = String.format("Code: %s", stop?.code ?: "NULL")
		holder.linesField.text = String.format("Lines: %s", stop?.linesFormatted() ?: "NULL")

	}

	override fun getItemCount(): Int = 1

	override fun onClick(v: View?) {
		return
	}
}