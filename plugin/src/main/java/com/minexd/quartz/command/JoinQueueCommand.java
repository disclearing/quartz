package com.minexd.quartz.command;

import com.google.gson.JsonObject;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.packet.PlayerAddPacket;
import com.minexd.quartz.packet.PlayerSendPacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.queue.QueueRank;
import com.minexd.quartz.server.Server;
import com.minexd.quartz.util.JsonChain;
import com.qrakn.honcho.command.CPL;
import com.qrakn.honcho.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandMeta(label = "joinqueue", async = true)
public class JoinQueueCommand {

	public void execute(Player player, @CPL("queue") Queue queue) {
		if (queue == null) {
			player.sendMessage(ChatColor.RED + "A queue with that name does not exist.");
			return;
		}

		Server server = Quartz.get().getQuartzData().getServerById(queue.getName());

		if (server == null || !server.isOnline()) {
			player.sendMessage(ChatColor.RED + "That queue is offline.");
			return;
		}

		if (Quartz.get().getQuartzData().getQueueByPlayer(player.getUniqueId()) != null) {
			player.sendMessage(ChatColor.RED + "You are already in a queue!");
			return;
		}

		if (player.hasPermission("quartz.bypass")) {
			Quartz.get().getPidgin().sendPacket(new PlayerSendPacket(new JsonChain()
					.addProperty("server", queue.getName())
					.addProperty("uuid", player.getUniqueId().toString())
					.get()));
			return;
		}

		QueueRank queueRank = Quartz.get().getPriority().getRank(player);

		JsonObject rankObject = new JsonChain()
				.addProperty("name", queueRank.getName())
				.addProperty("priority", queueRank.getPriority())
				.get();

		JsonObject playerObject = new JsonChain()
				.addProperty("uuid", player.getUniqueId().toString())
				.add("rank", rankObject)
				.get();

		JsonObject data = new JsonChain()
				.addProperty("queue", queue.getName())
				.add("player", playerObject)
				.get();

		Quartz.get().getPidgin().sendPacket(new PlayerAddPacket(data));
	}

}
