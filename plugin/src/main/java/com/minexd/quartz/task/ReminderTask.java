package com.minexd.quartz.task;

import com.minexd.quartz.Locale;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.queue.Queue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ReminderTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Queue queue = Quartz.get().getQuartzData().getQueueByPlayer(player.getUniqueId());

            if (queue != null) {
                Locale.SENDING.send(player, queue.getName());
            }
        }
    }

}
