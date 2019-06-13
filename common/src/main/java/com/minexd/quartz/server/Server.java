package com.minexd.quartz.server;

import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.Getter;

@Data
public class Server {

	private String id;
	private String name;
	private int onlinePlayers;
	private int maximumPlayers;
	private boolean whitelisted;
	private int port;
	private JsonObject metadata;
	private long lastUpdate;

	public Server(String id) {
		this.id = id;
	}

	public boolean isOnline() {
		return System.currentTimeMillis() - this.lastUpdate <= 15_000L;
	}

}
