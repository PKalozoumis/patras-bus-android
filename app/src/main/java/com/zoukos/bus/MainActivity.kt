package com.zoukos.bus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoukos.bus.ui.theme.BusTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.ui.draw.clip

//==============================================================================================

class MainActivity : ComponentActivity()
{
	private var debug:Boolean = true;

	private val entires: List<ListEntry> = List(6){
		ListEntry("601", "line", 3, 45, 24.5, 34.6)
	}

	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent{
			BusTheme{
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.background(Color(0xFFCCFFFF))
						.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween,
				){

					Column(
						modifier = Modifier
							.fillMaxWidth()
							.fillMaxHeight(0.80f)
							.padding(top = 30.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.SpaceBetween,
					){
						Spacer(modifier = Modifier.height(15.dp))
						Thing(this@MainActivity.entires)
						Spacer(modifier = Modifier.height(35.dp))
						Thing(this@MainActivity.entires)
					}

					Column(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1.0f, true),
						verticalArrangement = Arrangement.Center,
						horizontalAlignment = Alignment.CenterHorizontally
					){
						Button(
							modifier = Modifier
								.fillMaxWidth(0.5f)
								.height(50.dp)
								.offset(y = (-30).dp),
							onClick={}
						){
							Text("Refresh")
						}
					}
				}
			}
		}
	}
}

//==============================================================================================

@Composable
fun ListItem(name: String, modifier: Modifier = Modifier) {
	Text(
		text = "Hello $name!",
		modifier = modifier
			.fillMaxWidth()
			.background(Color(0xAAFFFFFF))
			.padding(32.dp)
	)
	Separator()
}

@Composable
fun Separator()
{
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(1.dp)
			.background(Color.Black)
	)
}

@Composable
fun ColumnScope.Thing(data: List<ListEntry>)
{
	val textModifier: Modifier = Modifier
		.fillMaxWidth()
		.padding(top = 10.dp, bottom = 15.dp)

	//University
	//===================================================================================
	Column(
		modifier = Modifier
			.fillMaxWidth(0.9f)
			.weight(1.0f)
			//.border(3.dp, Color.Black, RoundedCornerShape(20.dp))
			.clip(shape = RoundedCornerShape(20.dp))
			.background(Color(0xFF87D1FF))
			.padding(10.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	){
		Text(
			"ΠΑΝΕΠΙΣΤΗΜΙΟ",
			textAlign = TextAlign.Center,
			modifier = textModifier
		)

		LazyColumn(modifier = Modifier.fillMaxSize(1f)
		){
			item{ Separator()}
			items(data){
				ListItem(it.lineCode)
			}
		}
	}
}