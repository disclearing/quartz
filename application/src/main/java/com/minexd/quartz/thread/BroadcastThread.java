package com.minexd.quartz.thread;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minexd.quartz.log.Logger;
import com.minexd.quartz.packet.QueueListPacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.util.JsonChain;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.queue.QueuePlayer;

public class BroadcastThread extends Thread {

    @Override
    public void run() {
        while (true) {
            try {
                JsonArray queues = new JsonArray();

                for (Queue queue : Quartz.get().getQuartzData().getQueues()) {
                    JsonArray players = new JsonArray();

                    for (QueuePlayer player : queue.getPlayers()) {
                        JsonObject rankObject = new JsonChain()
                                .addProperty("name", player.getRank().getName())
                                .addProperty("priority", player.getRank().getPriority())
                                .get();

                        JsonObject playerObject = new JsonChain()
                                .addProperty("uuid", player.getUuid().toString())
                                .addProperty("inserted-at", player.getInserted())
                                .add("rank", rankObject)
                                .get();

                        players.add(playerObject);
                    }

                    JsonObject queueObject = new JsonObject();
                    queueObject.addProperty("id", queue.getName());
                    queueObject.addProperty("status", queue.isEnabled());
                    queueObject.add("players", players);

                    queues.add(queueObject);
                }

                Quartz.get().getPidgin().sendPacket(new QueueListPacket(queues));

                Logger.print("Broadcast server and queue list");

                Thread.sleep(5000L);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

}
