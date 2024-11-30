package com.zoukos.bus.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class StopListAdapter(private var context:Context, public var stops:MutableList<Stop?>, private val singleElement: Boolean): RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

	//View Holders
	//====================================================================================================================
	inner class StopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		var idx: Int = 0;
		var icon: ImageView = view.findViewById<ImageView>(R.id.icon)
		val nameField: TextView = view.findViewById<TextView>(R.id.stopName)
		val idField: TextView = view.findViewById<TextView>(R.id.idField)
		val linesField: TextView = view.findViewById<TextView>(R.id.linesField)
	}

	inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
	}

	//Overrides
	//====================================================================================================================

	override fun getItemViewType(position: Int): Int {
		return if (stops[position] == null) 0 else 1
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

		holder.itemView.setOnClickListener{

			if (singleElement)
				return@setOnClickListener;

			val selectedStop: Stop? = stops[holder.bindingAdapterPosition]

			val resultIntent: Intent = Intent();
			val extras: Bundle = Bundle()
			extras.putSerializable("stop", selectedStop)
			extras.putInt("fragment", 1)
			resultIntent.putExtras(extras)

			with(context as MapScreen)
			{
				if (selectedStop != null)
					setResult(android.app.Activity.RESULT_OK, resultIntent);
				else
					setResult(android.app.Activity.RESULT_CANCELED, resultIntent);

				finish()
			}
		}

		val	stop: Stop = stops.get(position)!!;

		if (stop.isFavorite)
			holder.icon.setImageResource(R.drawable.heart_shaped_object);
		else
			holder.icon.setImageResource(R.drawable.grey_heart_shaped_object);

		holder.idx = position;

		holder.icon.scaleX - 0.8;
		holder.icon.scaleY - 0.8;

		holder.nameField.text = stop.name ?: "NULL"
		holder.idField.text = String.format("Code: %s", stop.code ?: "NULL")
		holder.linesField.text = String.format("Lines: %s", stop.linesFormatted() ?: "NULL")

		if (!singleElement)
			holder.itemView.setPadding(0, 0, 0, 40) // Adjust padding as needed

		holder.icon.setOnClickListener{
			if (stop.isFavorite)
			{
				stop.isFavorite = false;
				holder.icon.setImageResource(R.drawable.grey_heart_shaped_object);
				Log.d("KORT", String.format("Billkort says \"fake\""))

				if (!singleElement)
				{
					stops.removeAt(position);
					notifyItemRemoved(position);
				}
			}
			else
			{
				stop.isFavorite = true;
				holder.icon.setImageResource(R.drawable.heart_shaped_object);
				Log.d("KORT", String.format("Billkort says \"real\""))
			}

			Log.d("KORT", String.format("Billkort 2FA: %b", stop.isFavorite));
		}

	}

	override fun getItemCount(): Int = stops?.size ?: 1
}