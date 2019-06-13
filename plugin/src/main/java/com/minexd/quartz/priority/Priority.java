package com.minexd.quartz.priority;

import com.minexd.quartz.queue.QueueRank;
import org.bukkit.entity.Player;

public interface Priority {

    QueueRank getRank(Player player);

}
