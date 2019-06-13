package com.minexd.quartz.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.minexd.quartz.Quartz;
import org.bukkit.entity.Player;

public final class BungeeUtil {

	private BungeeUtil() {
		throw new RuntimeException("Cannot instantiate a utility class.");
	}

	public static void sendToServer(Player player, String server) {
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(server);

			player.sendPluginMessage(Quartz.get(), "BungeeCord", out.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
