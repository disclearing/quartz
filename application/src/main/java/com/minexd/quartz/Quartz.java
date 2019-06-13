package com.minexd.quartz;

import com.minexd.pidgin.Pidgin;
import com.minexd.quartz.network.NetworkListener;
import com.minexd.quartz.log.Logger;
import com.minexd.quartz.packet.PlayerAddPacket;
import com.minexd.quartz.packet.PlayerAddedPacket;
import com.minexd.quartz.packet.PlayerRemovePacket;
import com.minexd.quartz.packet.PlayerRemovedPacket;
import com.minexd.quartz.packet.PlayerSendPacket;
import com.minexd.quartz.packet.QueueListPacket;
import com.minexd.quartz.packet.ServerMetadataPacket;
import com.minexd.quartz.packet.ServerUpdatePacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.thread.BroadcastThread;
import com.minexd.quartz.thread.QueueThread;
import com.minexd.quartz.data.QuartzData;
import com.minexd.quartz.file.Config;

import java.util.Arrays;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Quartz {

    private static Quartz quartz;

    @Getter private Config config;
    @Getter private JedisPool jedisPool;
    @Getter private Pidgin pidgin;
    @Getter private QuartzData quartzData;

    private Quartz() {
        quartz = this;

        config = new Config();
        jedisPool = new JedisPool(config.getRedisHost(), config.getRedisPort());

        if (config.getRedisPassword() != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.auth(config.getRedisPassword());
            }
        }

        pidgin = new Pidgin("quartz", config.getRedisHost(), config.getRedisPort(), config.getRedisPassword());
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

        for (String name : config.getQueues()) {
            quartzData.getQueues().add(new Queue(name));

            Logger.print("Loaded queue `" + name + "` from config");
        }

        Logger.print("Quartz is now running...");

        new QueueThread().start();
        new BroadcastThread().start();
    }

    public static void main(String[] args) {
        new Quartz();
    }

    public static Quartz get() {
        return quartz;
    }

}
