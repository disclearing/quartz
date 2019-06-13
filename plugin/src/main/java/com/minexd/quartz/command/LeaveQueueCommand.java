package com.minexd.quartz.command;

import com.minexd.quartz.Quartz;
import com.minexd.quartz.packet.PlayerRemovePacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.util.JsonChain;
import com.qrakn.honcho.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandMeta(label = "leavequeue", async = true)
public class LeaveQueueCommand {

	public void execute(Player player) {
		Queue queue = Quartz.get().getQuartzData().getQueueByPlayer(player.getUniqueId());

		if (queue == null) {
			player.sendMessage(ChatColor.RED + "You are not in a queue!");
			return;
		}

		Quartz.get().getPidgin().sendPacket(new PlayerRemovePacket(new JsonChain()
				.addProperty("uuid", player.getUniqueId().toString())
				.get()));
	}

}
