package com.minexd.quartz.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minexd.pidgin.packet.handler.IncomingPacketHandler;
import com.minexd.pidgin.packet.listener.PacketListener;
import com.minexd.quartz.Locale;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.packet.PlayerAddedPacket;
import com.minexd.quartz.packet.PlayerRemovedPacket;
import com.minexd.quartz.packet.PlayerSendPacket;
import com.minexd.quartz.packet.QueueListPacket;
import com.minexd.quartz.packet.ServerMetadataPacket;
import com.minexd.quartz.packet.ServerUpdatePacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.queue.QueuePlayer;
import com.minexd.quartz.queue.QueuePlayerComparator;
import com.minexd.quartz.server.Server;
import com.minexd.quartz.util.BungeeUtil;
import java.util.PriorityQueue;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NetworkListener implements PacketListener {

	@IncomingPacketHandler
	public void onPlayerAddedPacket(PlayerAddedPacket packet) {
		Queue queue = Quartz.get().getQuartzData().getQueueByName(packet.getData().get("queue").getAsString());

		if (queue != null) {
			QueuePlayer queuePlayer = new QueuePlayer(packet.getData().get("player").getAsJsonObject());
			Player player = Bukkit.getPlayer(queuePlayer.getUuid());

			queue.getPlayers().add(queuePlayer);

			if (player != null) {
				Locale.JOINED_QUEUE.send(player, queue.getName());
			}
		}
	}

	@IncomingPacketHandler
	public void onPlayerRemovedPacket(PlayerRemovedPacket packet) {
		Queue queue = Quartz.get().getQuartzData().getQueueByName(packet.getData().get("queue").getAsString());

		if (queue != null) {
			QueuePlayer queuePlayer = new QueuePlayer(packet.getData().get("player").getAsJsonObject());
			Player player = Bukkit.getPlayer(queuePlayer.getUuid());

			queue.getPlayers().removeIf(check -> check.getUuid().equals(player.getUniqueId()));

			if (player != null) {
				Locale.LEFT_QUEUE.send(player, queue.getName());
			}
		}
	}

	@IncomingPacketHandler
	public void onPlayerSendPacket(PlayerSendPacket packet) {
		Player player = Bukkit.getPlayer(UUID.fromString(packet.getData().get("uuid").getAsString()));

		if (player != null) {
			final String server = packet.getData().get("server").getAsString();

			Locale.SENDING.send(player, packet.getData().get("server").getAsString());
			BungeeUtil.sendToServer(player, server);
		}
	}

	@IncomingPacketHandler
	public void onQueueListPacket(QueueListPacket packet) {
		if (!Quartz.get().getMainConfig().getBoolean("SETTINGS.CACHE_SERVERS")) {
			return;
		}

		JsonArray array = packet.getData();

		for (JsonElement queueElement : array) {
			JsonObject queueObject = queueElement.getAsJsonObject();
			final String queueName = queueObject.get("id").getAsString();

			Queue queue = Quartz.get().getQuartzData().getQueueByName(queueName);

			if (queue == null) {
				queue = new Queue(queueName);
				Quartz.get().getQuartzData().getQueues().add(queue);
			}

			PriorityQueue<QueuePlayer> players = new PriorityQueue<>(new QueuePlayerComparator());

			for (JsonElement playerElement : queueObject.get("players").getAsJsonArray()) {
				QueuePlayer player = new QueuePlayer(playerElement.getAsJsonObject());
				players.add(player);
			}

			queue.setPlayers(players);
			queue.setEnabled(queueObject.get("status").getAsBoolean());
		}
	}

	@IncomingPacketHandler
	public void onServerMetadata(ServerMetadataPacket packet) {
		Server server = Quartz.get().getQuartzData().getServerById(packet.getData().get("id").getAsString());

		if (server != null) {
			server.getMetadata().add(packet.getData().get("field").getAsString(), packet.getData().get("value"));
		}
	}

	@IncomingPacketHandler
	public void onServerUpdate(ServerUpdatePacket packet) {
		String serverId = packet.getData().get("id").getAsString();

		if (Quartz.get().getMainConfig().getBoolean("SETTINGS.CACHE_SERVERS")) {
			Server server = Quartz.get().getQuartzData().getServerById(serverId);

			if (server == null) {
				server = new Server(serverId);
				Quartz.get().getQuartzData().getServers().add(server);
			}

			server.setName(packet.getData().get("name").getAsString());
			server.setOnlinePlayers(packet.getData().get("online-players").getAsInt());
			server.setMaximumPlayers(packet.getData().get("maximum-players").getAsInt());
			server.setWhitelisted(packet.getData().get("whitelisted").getAsBoolean());
			server.setPort(packet.getData().get("port").getAsInt());
			server.setMetadata(packet.getData().get("metadata").getAsJsonObject());
			server.setLastUpdate(System.currentTimeMillis());
		}
	}

}
