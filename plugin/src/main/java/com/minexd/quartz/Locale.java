package com.minexd.quartz;

import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public enum Locale {

	JOINED_QUEUE("QUEUE.JOINED"),
	LEFT_QUEUE("QUEUE.LEFT"),
	REMINDER("QUEUE.REMINDER"),
	SENDING("QUEUE.SENDING");

	private String path;

	public void send(CommandSender sender, Object... objects) {
		if (Quartz.get().getMainConfig().get(path) instanceof String) {
			sender.sendMessage(new MessageFormat(ChatColor.translateAlternateColorCodes('&',
					Quartz.get().getMainConfig().getString(path))).format(objects));
		} else {
			for (String string : Quartz.get().getMainConfig().getStringList(path)) {
				sender.sendMessage(new MessageFormat(ChatColor.translateAlternateColorCodes('&', string))
						.format(objects));
			}
		}
	}

}
