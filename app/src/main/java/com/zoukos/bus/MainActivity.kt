package com.zoukos.bus

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

class MyViewModel : ViewModel() {
	private val _myProperty: MutableStateFlow<MutableList<ListEntry>> = MutableStateFlow(mutableListOf())
	val myProperty: StateFlow<MutableList<ListEntry>> get() = _myProperty

	fun add(newValue: ListEntry) {
		_myProperty.value.add(newValue)
		//_myProperty.value.onEach{Log.d("SUS", it.lineName)}
	}

	fun updateProperty(newValue: MutableList<ListEntry>) {
		_myProperty.value = newValue
	}
}

//==============================================================================================

class MainActivity : ComponentActivity()
{
	private var debug:Boolean = true;
	private val entries1:MutableList<ListEntry> = mutableStateListOf()
	private val entries2:MutableList<ListEntry> = mutableStateListOf()

	private var timer: CountDownTimer = object: CountDownTimer(1000, 500){
		override fun onTick(millisUntilFinished: Long) {

		}

		override fun onFinish() {
			errorText.value = "";
		}

	};

	private var errorText: MutableState<String> = mutableStateOf("");

	//@SuppressLint("UnrememberedMutableState UnrememberedSnowgraves")
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
					//TextField(value = "Text", onValueChange = {})

					Column(
						modifier = Modifier
							.fillMaxWidth()
							.fillMaxHeight(0.6f)
							.padding(top = 30.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.SpaceBetween,
					){
						Thing("ΠΑΝΕΠΙΣΤΗΜΙΟ", entries1, modifier = Modifier.requiredHeight(245.dp))
						Spacer(modifier = Modifier.height(30.dp))
						Thing("ΚΕΝΤΡΟ", entries2, modifier = Modifier.requiredHeight(245.dp))
					}

					Column(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1.0f, true),
						verticalArrangement = Arrangement.SpaceAround,
						horizontalAlignment = Alignment.CenterHorizontally
					){
						if (errorText.value != null)
							Text(errorText.value!!, fontWeight = FontWeight.Bold, color = Color.Red)

						Button(
							modifier = Modifier
								.fillMaxWidth(0.5f)
								.height(50.dp),
							onClick=::onRefresh
							//onClick=::test
						){
							Text("Refresh")
						}
					}
				}
			}
		}
	}

	private fun onRefresh(): Unit{

		timer.cancel()
		timer.start()

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
						errorText.value = "No buses at the moment";
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
									errorText.value = "Failed to get a new token";
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
				errorText.value = "Could not reach server";
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
				fontWeight = FontWeight.Bold
			)
		}

		Column(
			modifier = modifier
				.weight(1f, fill = true)
				.background(Color(0xAAFFFFFF))
				.padding(top = 15.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),

			horizontalAlignment = Alignment.Start,
		){
			Text(entry.lineName, fontSize = 14.sp)
			Text("${"%02d".format(entry.min)}:${"%02d".format(entry.sec)}", modifier = modifier
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
			.background(Color(0xFF87D1FF))
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
				fontWeight = FontWeight.Bold
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