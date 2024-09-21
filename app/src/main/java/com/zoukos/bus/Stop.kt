package com.zoukos.bus

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.Serializable

class Stop(stop: JsonNode): Serializable {

	val id:String = stop["id"].asText();
	val code:String = stop["code"].asText();
	val name:String = stop["name"].asText();
	val coords:Coordinates = Coordinates(stop["coords"]);
	val lines:List<String> = stop["lines"].map{it.asText()}

	fun linesFormatted(): String {
		var str:String = "";

		for (i in lines.indices)
			str += lines[i] + if (i < lines.size - 1) ", " else "";

		return str
	}

	fun toJsonString(): String{
		val mapper: ObjectMapper = ObjectMapper();
		val jsonObject: ObjectNode = mapper.createObjectNode();

		jsonObject.put("id", id)
		jsonObject.put("code", code)
		jsonObject.put("name", name)
		jsonObject.set<JsonNode>("coords", mapper.createObjectNode().apply{
			put("lat", coords.lat)
			put("lng", coords.lng)
		})
		jsonObject.set<ArrayNode>("lines", mapper.valueToTree(lines));

		return mapper.writeValueAsString(jsonObject)
	}
}
