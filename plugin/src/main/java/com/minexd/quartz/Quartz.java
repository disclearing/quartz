package com.minexd.quartz;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minexd.pidgin.Pidgin;
import com.minexd.quartz.command.JoinQueueCommand;
import com.minexd.quartz.command.LeaveQueueCommand;
import com.minexd.quartz.command.adapter.QueueTypeAdapter;
import com.minexd.quartz.network.NetworkListener;
import com.minexd.quartz.packet.PlayerAddPacket;
import com.minexd.quartz.packet.PlayerAddedPacket;
import com.minexd.quartz.packet.PlayerRemovePacket;
import com.minexd.quartz.packet.PlayerRemovedPacket;
import com.minexd.quartz.packet.PlayerSendPacket;
import com.minexd.quartz.packet.QueueListPacket;
import com.minexd.quartz.packet.ServerMetadataPacket;
import com.minexd.quartz.packet.ServerUpdatePacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.util.JsonChain;
import com.qrakn.honcho.Honcho;
import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import com.minexd.quartz.data.QuartzData;
import com.minexd.quartz.priority.Priority;
import com.minexd.quartz.priority.impl.DefaultPriority;
import com.minexd.quartz.task.ReminderTask;
import com.minexd.quartz.task.UpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Quartz extends JavaPlugin {

	private static Quartz quartz;

	@Getter private BasicConfigurationFile mainConfig;
	@Getter private JedisPool jedisPool;
	@Getter private Honcho honcho;
	@Getter private Pidgin pidgin;
	@Getter private QuartzData quartzData;
	@Getter @Setter private Priority priority;

	@Getter private String serverId;
	@Getter private String serverName;
	@Getter private JsonObject metadata = new JsonObject();

	@Override
	public void onEnable() {
		quartz = this;

		mainConfig = new BasicConfigurationFile(this, "config");

		final String redisHost = mainConfig.getString("REDIS.HOST");
		final int redisPort = mainConfig.getInteger("REDIS.PORT");
		final String redisPassword;

		if (mainConfig.getBoolean("REDIS.AUTHENTICATION.ENABLED")) {
			redisPassword = mainConfig.getString("REDIS.AUTHENTICATION.PASSWORD");
		} else {
			redisPassword = null;
		}

		jedisPool = new JedisPool(redisHost, redisPort);

		if (redisPassword != null) {
			try (Jedis jedis = jedisPool.getResource()) {
				jedis.auth(redisPassword);
			}
		}

		loadQuartzData();

		honcho = new Honcho(this);
		honcho.registerCommand(new JoinQueueCommand());
		honcho.registerCommand(new LeaveQueueCommand());
		honcho.registerTypeAdapter(Queue.class, new QueueTypeAdapter());

		pidgin = new Pidgin("quartz", redisHost, redisPort, redisPassword);
		pidgin.registerListener(new NetworkListener());

		Arrays.asList(
				PlayerAddedPacket.class,
				PlayerAddPacket.class,
				PlayerRemovedPacket.class,
				PlayerRemovePacket.class,
				PlayerSendPacket.class,
				QueueListPacket.class,
				ServerMetadataPacket.class,
				ServerUpdatePacket.class
		).forEach(pidgin::registerPacket);

		quartzData = new QuartzData(jedisPool);
		quartzData.loadServers();

		priority = new DefaultPriority();

		new ReminderTask().runTaskTimerAsynchronously(this, 0L, 20L * 10L);
		new UpdateTask().runTaskTimerAsynchronously(this, 0L, 20L * 3L);

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent event) {
				Quartz.get().getPidgin().sendPacket(new PlayerRemovePacket(new JsonChain()
						.addProperty("uuid", event.getPlayer().getUniqueId().toString())
						.get()));
			}
		}, this);
	}

	private void loadQuartzData() {
		serverId = mainConfig.getString("SETTINGS.SERVER_ID");
		serverName = mainConfig.getString("SETTINGS.SERVER_NAME");

		try (Jedis jedis = jedisPool.getResource()) {
			String key = "quartz:server-info:" + Bukkit.getPort();

			if (jedis.exists(key)) {
				Map<String, String> map = jedis.hgetAll(key);

				serverId = map.get("id");
				serverName = map.get("name");
				metadata = new JsonParser().parse(map.get("metadata")).getAsJsonObject();
			}
		}

		if (metadata == null || metadata.isJsonNull()) {
			metadata = new JsonObject();
		}
	}

	public static Quartz get() {
		return quartz;
	}

}
