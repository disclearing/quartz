package com.minexd.quartz.priority.impl;

import com.minexd.quartz.Quartz;
import com.minexd.quartz.queue.QueueRank;
import com.minexd.quartz.util.MapUtil;
import com.minexd.quartz.priority.Priority;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DefaultPriority implements Priority {

    private QueueRank defaultPriority;
    private Map<String, QueueRank> priorities = new HashMap<>();

    public DefaultPriority() {
        FileConfiguration config = Quartz.get().getMainConfig().getConfiguration();

        try {
            this.defaultPriority = new QueueRank("Default", 1);

            if (config.contains("priority.default")) {
                this.defaultPriority.setPriority(config.getInt("priority.default"));
            }

            if (config.contains("priority.ranks") && config.isConfigurationSection("priority.ranks")) {
                for (String rank : config.getConfigurationSection("priority.ranks").getKeys(false)) {
                    String path = "priority.ranks." + rank;

                    if (config.contains(path + ".priority") && config.contains(path + ".permission")) {
                        this.priorities.put(config.getString(path + ".permission"), new QueueRank(rank, config.getInt
                                (path + ".priority")));
                    }
                }
            }

            this.priorities = MapUtil.sortByValue(this.priorities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public QueueRank getRank(Player player) {
        for (Map.Entry<String, QueueRank> entry : this.priorities.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }

        return this.defaultPriority;
    }

}
