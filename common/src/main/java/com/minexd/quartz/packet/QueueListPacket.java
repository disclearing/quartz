package com.minexd.quartz.packet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minexd.pidgin.packet.Packet;
import com.minexd.quartz.util.JsonChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class QueueListPacket implements Packet {

	private JsonArray data;

	@Override
	public int id() {
		return 6;
	}

	@Override
	public JsonObject serialize() {
		return new JsonChain().add("data", data).get();
	}

	@Override
	public void deserialize(JsonObject object) {
		data = object.getAsJsonArray("data");
	}

}