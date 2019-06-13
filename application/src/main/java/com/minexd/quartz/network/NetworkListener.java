package com.minexd.quartz.network;

import com.google.gson.JsonObject;
import com.minexd.pidgin.packet.handler.IncomingPacketHandler;
import com.minexd.pidgin.packet.listener.PacketListener;
import com.minexd.quartz.packet.PlayerAddPacket;
import com.minexd.quartz.packet.PlayerAddedPacket;
import com.minexd.quartz.packet.PlayerRemovePacket;
import com.minexd.quartz.packet.PlayerRemovedPacket;
import com.minexd.quartz.packet.ServerUpdatePacket;
import java.util.Iterator;
import java.util.UUID;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.log.Logger;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.queue.QueuePlayer;
import com.minexd.quartz.server.Server;

public class NetworkListener implements PacketListener {

	@IncomingPacketHandler
	public void onPlayerAddPacket(PlayerAddPacket packet) {
		Queue queue = Quartz.get().getQuartzData().getQueueByName(packet.getData().get("queue").getAsString());

		if (queue != null) {
			JsonObject playerObject = packet.getData().get("player").getAsJsonObject();
			QueuePlayer player = new QueuePlayer(playerObject);

			if (Quartz.get().getQuartzData().getQueueByPlayer(player.getUuid()) != null) {
				return;
			}

			player.setInserted(System.currentTimeMillis());

			queue.getPlayers().add(player);

			playerObject.addProperty("inserted-at", player.getInserted());
			packet.getData().add("player", playerObject);

			Quartz.get().getPidgin().sendPacket(new PlayerAddedPacket(packet.getData()));
		}
	}

	@IncomingPacketHandler
	public void onPlayerRemovePacket(PlayerRemovePacket packet) {
		UUID uuid = UUID.fromString(packet.getData().get("uuid").getAsString());
		Queue queue = Quartz.get().getQuartzData().getQueueByPlayer(uuid);

		if (queue != null) {
			QueuePlayer queuePlayer = null;

			Iterator<QueuePlayer> iterator = queue.getPlayers().iterator();

			while (iterator.hasNext()) {
				QueuePlayer other = iterator.next();

				if (other.getUuid().equals(uuid)) {
					queuePlayer = other;

					iterator.remove();
				}
			}

			if (queuePlayer != null) {
				JsonObject rankObject = new JsonObject();
				rankObject.addProperty("name", queuePlayer.getRank().getName());
				rankObject.addProperty("priority", queuePlayer.getRank().getPriority());

				JsonObject playerObject = new JsonObject();
				playerObject.addProperty("uuid", queuePlayer.getUuid().toString());
				playerObject.addProperty("inserted-at", queuePlayer.getInserted());
				playerObject.add("rank", rankObject);

				JsonObject data = new JsonObject();
				data.addProperty("queue", queue.getName());
				data.add("player", playerObject);

				Quartz.get().getPidgin().sendPacket(new PlayerRemovedPacket(data));
			}
		}
	}

	@IncomingPacketHandler
	public void onServerUpdatePacket(ServerUpdatePacket packet) {
		String serverId = packet.getData().get("id").getAsString();
		Server server = Quartz.get().getQuartzData().getServerById(serverId);
		Queue queue = Quartz.get().getQuartzData().getQueueByName(serverId);

		if (server == null) {
			server = new Server(serverId);

			if (queue != null) {
				queue.setEnabled(true);
			}

			Logger.print("Initiated server `" + serverId + "`");
		}

		server.setName(packet.getData().get("name").getAsString());
		server.setOnlinePlayers(packet.getData().get("online-players").getAsInt());
		server.setMaximumPlayers(packet.getData().get("maximum-players").getAsInt());
		server.setWhitelisted(packet.getData().get("whitelisted").getAsBoolean());
		server.setPort(packet.getData().get("port").getAsInt());
		server.setMetadata(packet.getData().get("metadata").getAsJsonObject());
		server.setLastUpdate(System.currentTimeMillis());

		Logger.print("Updated server `" + serverId + "`");
	}

}
