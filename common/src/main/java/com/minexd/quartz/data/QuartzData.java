package com.minexd.quartz.data;

import com.google.gson.JsonParser;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.server.Server;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import com.minexd.quartz.queue.QueuePlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class QuartzData {

	private final JedisPool jedisPool;
	@Getter private final List<Server> servers = new ArrayList<>();
	@Getter private final List<Queue> queues = new ArrayList<>();

	public QuartzData(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public void loadServers() {
		try (Jedis jedis = jedisPool.getResource()) {
			ScanResult<String> found = jedis.scan("0", new ScanParams().match("quartz:server-info").count(100));

			for (String path : found.getResult()) {
				String[] split = path.split(":");
				int port = Integer.valueOf(split[2]);

				Map<String, String> map = jedis.hgetAll(path);

				if (map == null || map.isEmpty()) {
					continue;
				}

				// Try and get the server if it is already cached
				Server server = this.getServerById(map.get("id"));

				// If not, create it
				if (server == null) {
					this.servers.add((server = new Server(map.get("id"))));
				}

				// Update fields
				server.setName(map.get("name"));
				server.setPort(port);
				server.setOnlinePlayers(Integer.valueOf(map.get("online-players")));
				server.setMaximumPlayers(Integer.valueOf(map.get("maximum-players")));
				server.setWhitelisted(Boolean.valueOf(map.get("whitelisted")));
				server.setMetadata(new JsonParser().parse(map.get("metadata")).getAsJsonObject());
				server.setLastUpdate(Long.valueOf(map.get("last-update")));
			}
		}
	}

	public Server getServerById(String id) {
		for (Server server : this.servers) {
			if (server.getId().equalsIgnoreCase(id)) {
				return server;
			}
		}

		return null;
	}

	public Server getByPort(int port) {
		for (Server server : this.servers) {
			if (server.getPort() == port) {
				return server;
			}
		}

		return null;
	}

	public Queue getQueueByName(String name) {
		for (Queue queue : this.queues) {
			if (queue.getName().equalsIgnoreCase(name)) {
				return queue;
			}
		}

		return null;
	}

	public Queue getQueueByPlayer(UUID uuid) {
		for (Queue queue : this.queues) {
			for (QueuePlayer queuePlayer : queue.getPlayers()) {
				if (queuePlayer.getUuid().equals(uuid)) {
					return queue;
				}
			}
		}

		return null;
	}

}
