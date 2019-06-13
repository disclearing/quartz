package com.minexd.quartz.task;

import com.google.gson.JsonObject;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.packet.ServerUpdatePacket;
import com.minexd.quartz.util.JsonChain;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

public class UpdateTask extends BukkitRunnable {

	private Quartz quartz = Quartz.get();

	@Override
	public void run() {
		// Update server info in redis
		{
			try (Jedis jedis = Quartz.get().getJedisPool().getResource()) {
				final String infoKey = "quartz:server-info:" + Bukkit.getPort();

				jedis.hset(infoKey, "id", quartz.getServerId());
				jedis.hset(infoKey, "name", quartz.getServerName());
				jedis.hset(infoKey, "port", "" + Bukkit.getPort());
				jedis.hset(infoKey, "online-players", Bukkit.getOnlinePlayers().size() + "");
				jedis.hset(infoKey, "maximum-players", Bukkit.getMaxPlayers() + "");
				jedis.hset(infoKey, "whitelisted", Bukkit.hasWhitelist() + "");
				jedis.hset(infoKey, "metadata", quartz.getMetadata().toString());
				jedis.hset(infoKey, "last-update", System.currentTimeMillis() + "");

				// Update server lookup data
				jedis.hset("quartz:lookup:port", "" + Bukkit.getPort(), quartz.getServerId());
				jedis.hset("quartz:lookup:id", quartz.getServerId(), Bukkit.getPort() + "");
			}
		}

		// Broadcast server info to other Quartz instances
		{
			JsonObject data = new JsonChain()
					.addProperty("id", quartz.getServerId())
					.addProperty("name", quartz.getServerName())
					.addProperty("online-players", Bukkit.getOnlinePlayers().size())
					.addProperty("maximum-players", Bukkit.getMaxPlayers())
					.addProperty("whitelisted", Bukkit.hasWhitelist())
					.addProperty("port", Bukkit.getPort())
					.add("metadata", quartz.getMetadata())
					.get();

			Quartz.get().getPidgin().sendPacket(new ServerUpdatePacket(data));
		}
	}

}
