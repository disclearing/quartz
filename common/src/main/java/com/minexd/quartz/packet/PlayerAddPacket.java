package com.minexd.quartz.packet;

import com.google.gson.JsonObject;
import com.minexd.pidgin.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PlayerAddPacket implements Packet {

	private JsonObject data;

	@Override
	public int id() {
		return 2;
	}

	@Override
	public JsonObject serialize() {
		return data;
	}

	@Override
	public void deserialize(JsonObject object) {
		data = object;
	}

}