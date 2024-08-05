package com.zoukos.bus

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoukos.bus.network.getStopData
import com.zoukos.bus.ui.theme.BusTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//==============================================================================================

class MainActivity : ComponentActivity()
{
	private val entries1:MutableList<ListEntry> = mutableStateListOf()
	private val entries2:MutableList<ListEntry> = mutableStateListOf()

	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent{
			BusTheme{
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.fillMaxWidth()
						.background(color = MaterialTheme.colorScheme.background),
					horizontalAlignment = Alignment.CenterHorizontally
				){
					//TextField(value = "Text", onValueChange = {})

					TextField(
						value = "Text",
						onValueChange = {},
						label = {Text("Selected Stop:")},
						enabled = false,
						modifier = Modifier
							.fillMaxWidth(0.85f)
							.padding(top = 45.dp)
							.clickable { Tostaki(this@MainActivity, "sus", Toast.LENGTH_SHORT) }
					)

					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Top,
						modifier = Modifier.fillMaxHeight(0.8f).padding(top=15.dp)
					){

						Column(
							modifier = Modifier
								.fillMaxWidth()
								.weight(1.0f, fill=true),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.Top
						){
							Thing("ΠΑΝΕΠΙΣΤΗΜΙΟ", entries1, modifier = Modifier.requiredHeight(245.dp))
							//Spacer(modifier = Modifier.height(5.dp))
							Thing("ΚΕΝΤΡΟ", entries2, modifier = Modifier.requiredHeight(245.dp))
							//Spacer(modifier = Modifier.weight(1.0f, fill = true))
						}

						Button(
							modifier = Modifier
								.fillMaxWidth(0.5f)
								.padding(top=40.dp)
								.height(50.dp),

							//onClick=::onRefresh
							onClick=::test
						){
							Text("Refresh")
						}

						//Spacer(modifier = Modifier.weight(1.0f, fill = true))
					}
				}
			}
		}
	}

	private fun onRefresh(): Unit{

		getStopData("678", object: Callback<ResponseBody> {
			override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

				Log.d("BUS", "Communication success")

				//Token probably expired
				if (!response.isSuccessful){

					val authHeader: String = response.headers().get("WWW-Authenticate") ?: "";
					val match = Regex("error=\"(.*?)\"").find(authHeader)

					//Failure due to lack of busses
					if (match == null){
						Log.d("BUS", "No buses at the moment");
						Tostaki(this@MainActivity, "No buses at the moment", Toast.LENGTH_SHORT)
						entries1.clear()
						entries2.clear()
					}
					//We need to check if the token is valid or not
					else{
						val err: String? = match.groups[1]?.value

						Log.d("BUS",  err?: "nullmatch");

						if (err == "invalid_token"){

							//Get a new token from the server and try to send another request
							AuthToken.refreshToken(object : GenericCallback<String> {
								override fun onSuccess(data: String) {
									onRefresh()
								}

								override fun onFailure(e: Exception?) {
									Log.d("BUS", "Failed to get a new token")
									Tostaki(this@MainActivity, "Failed to get a new token", Toast.LENGTH_SHORT)
								}

							})
						}
					}
					return;
				}

				//Valid token
				//=================================================================================
				Log.d("BUS", "Good token: ${AuthToken.getToken()}")




				//Print json response
				val mapper: ObjectMapper = ObjectMapper()
				val rootNode: JsonNode = mapper.readTree(response.body()!!.string())
				//val rootNode: JsonNode = mapper.readTree("{vehicles:[{lineCode:608,lineName:ΑΝΩ ΚΑΣΤΡΙΤΣΙ,routeCode:6081,routeName:ΑΝΩ ΚΑΣΤΡΙΤΣΙ,latitude:38.257454,longitude:21.748185,departureMins:6,departureSeconds:30,vehicleCode:20240724_6081_0010000_21_05,lineColor:#ccab1d,lineTextColor:#ffffff,borderColor:#A88D18},{lineCode:601,lineName:ΠΑΝΕΠΙΣΤΗΜΙΟ ΝΟΣΟΚΟΜΕΙΟ,routeCode:6011,routeName:ΕΡΜΟΥ - ΠΑΝΕΠΙΣΤΗΜΙΟΥ,latitude:38.249644,longitude:21.740153,departureMins:9,departureSeconds:7,vehicleCode:20240724_6011_0010000_21_10,lineColor:#512da8,lineTextColor:#ffffff,borderColor:#412488},{lineCode:609,lineName:ΝΟΣΟΚΟΜΕΙΟ-ΠΑΝΕΠΙΣΤΗΜΙΟ-ΚΕΝΤΡΟ,routeCode:6091,routeName:ΝΟΣΟΚΟΜΕΙΟ  ΠΑΝΕΠΙΣΤΗΜΙΟ ΕΡΜΟΥ,latitude:38.290284,longitude:21.78427,departureMins:11,departureSeconds:0,vehicleCode:1296109903164736446,lineColor:#512d44,lineTextColor:#ffffff,borderColor:#371E2E}]}")

				entries1.clear()
				entries2.clear()

				for (item: JsonNode in rootNode.get("vehicles")){

					var container: MutableList<ListEntry>? = null;

					if (item.get("lineCode").asText() == "601")
						container = entries1
					else if (item.get("lineCode").asText() == "609")
						container = entries2

					container?.add(ListEntry(
						item.get("lineCode").asText(),
						item.get("lineName").asText(),
						item.get("departureMins").asInt().toByte(),
						item.get("departureSeconds").asInt().toByte(),
						item.get("latitude").asDouble(),
						item.get("longitude").asDouble()
					))
				}
			}

			override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
				Log.d("BUS", "Could not reach server")

				Tostaki(this@MainActivity, "Could not reach server", Toast.LENGTH_SHORT)
			}

		})
	}

	fun test(){
		entries1.clear()
		entries2.clear()

		//Print json response
		val mapper: ObjectMapper = ObjectMapper()
		//val rootNode: JsonNode = mapper.readTree(response.body()!!.string())
		val rootNode: JsonNode = mapper.readTree("{\"vehicles\":[{\"lineCode\":\"608\",\"lineName\":\"ΑΝΩ ΚΑΣΤΡΙΤΣΙ\",\"routeCode\":\"6081\",\"routeName\":\"ΑΝΩ ΚΑΣΤΡΙΤΣΙ\",\"latitude\":\"38.257454\",\"longitude\":\"21.748185\",\"departureMins\":6,\"departureSeconds\":30,\"vehicleCode\":\"20240724_6081_0010000_21_05\",\"lineColor\":\"#ccab1d\",\"lineTextColor\":\"#ffffff\",\"borderColor\":\"#A88D18\"},{\"lineCode\":\"601\",\"lineName\":\"ΠΑΝΕΠΙΣΤΗΜΙΟ ΝΟΣΟΚΟΜΕΙΟ\",\"routeCode\":\"6011\",\"routeName\":\"ΕΡΜΟΥ - ΠΑΝΕΠΙΣΤΗΜΙΟΥ\",\"latitude\":\"38.249644\",\"longitude\":\"21.740153\",\"departureMins\":9,\"departureSeconds\":7,\"vehicleCode\":\"20240724_6011_0010000_21_10\",\"lineColor\":\"#512da8\",\"lineTextColor\":\"#ffffff\",\"borderColor\":\"#412488\"},{\"lineCode\":\"609\",\"lineName\":\"ΝΟΣΟΚΟΜΕΙΟ-ΠΑΝΕΠΙΣΤΗΜΙΟ-ΚΕΝΤΡΟ\",\"routeCode\":\"6091\",\"routeName\":\"ΝΟΣΟΚΟΜΕΙΟ  ΠΑΝΕΠΙΣΤΗΜΙΟ ΕΡΜΟΥ\",\"latitude\":\"38.290284\",\"longitude\":\"21.78427\",\"departureMins\":11,\"departureSeconds\":0,\"vehicleCode\":\"1296109903164736446\",\"lineColor\":\"#512d44\",\"lineTextColor\":\"#ffffff\",\"borderColor\":\"#371E2E\"}]}")

		for (item: JsonNode in rootNode.get("vehicles")){
			var container: MutableList<ListEntry>? = null;

			if (item.get("lineCode").asText() == "601")
				container = entries1
			else if (item.get("lineCode").asText() == "609")
				container = entries2

			container?.add(ListEntry(
				item.get("lineCode").asText(),
				item.get("lineName").asText(),
				item.get("departureMins").asInt().toByte(),
				item.get("departureSeconds").asInt().toByte(),
				item.get("latitude").asDouble(),
				item.get("longitude").asDouble()
			))
		}
	}
}

//==============================================================================================

@Composable
fun ListItem(entry: ListEntry, modifier: Modifier = Modifier) {
	Row(modifier = modifier.height(84.dp)){

		Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
			.fillMaxWidth(0.18f)
			.fillMaxHeight()
		){
			val painter: Painter = painterResource(id = R.drawable.bus);
			Image(painter, "Bus", modifier = modifier
				.fillMaxWidth()
				.fillMaxHeight(0.5f));

			Text(
				text = entry.lineCode,
				fontSize = 25.sp,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSecondaryContainer
			)
		}

		Column(
			modifier = modifier
				.weight(1f, fill = true)
				.background(Color(0x22FFFFFF))
				.padding(top = 15.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),

			horizontalAlignment = Alignment.Start,
		){
			Text(entry.lineName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
			Text("${"%02d".format(entry.min)}:${"%02d".format(entry.sec)}",
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = modifier
				.padding(vertical = 10.dp));
			Spacer(modifier = modifier.weight(1.0f, fill = true))
		}
	}
	Separator()
}

//==============================================================================================

@Composable
fun EmptyListItem(modifier: Modifier = Modifier) {
	Row(modifier = modifier
		.fillMaxWidth()
		.height(84.dp),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	){
		Text("---", fontStyle = FontStyle.Italic);
	}
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
fun ColumnScope.Thing(name: String, data: List<ListEntry>, modifier: Modifier)
{
	val textModifier: Modifier = Modifier
		.fillMaxWidth()
		.padding(top = 10.dp, bottom = 15.dp)

	Column(
		modifier = modifier
			.fillMaxWidth(0.9f)
			.weight(1.0f)
			//.border(3.dp, Color.Black, RoundedCornerShape(20.dp))
			.clip(shape = RoundedCornerShape(20.dp))
			.background(color = MaterialTheme.colorScheme.secondaryContainer)
			.padding(10.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	){

		Box(){
			var notifs: Boolean by remember{mutableStateOf(false)}
			val painter: Painter = painterResource(id = (if (notifs) R.drawable.notif_active else R.drawable.notif_off));

			Button(onClick = {notifs = !notifs},modifier = Modifier
				.zIndex(1.0f)
				.align(Alignment.CenterEnd),
			colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified)){

				Image(painter, "Notification off")
			}


			Text(
				name,
				textAlign = TextAlign.Center,
				modifier = textModifier,
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSecondaryContainer
			)
		}

		LazyColumn(modifier = Modifier.fillMaxSize(1f)
		){
			item{ Separator()}

			for (i:Int in 0..maxOf(1, data.size)){
				if (i < data.size)
					item{ListItem(data[i])}
				else
					item{ EmptyListItem()}
			}
		}
	}
}