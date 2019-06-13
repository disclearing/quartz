package com.minexd.quartz.queue;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class Queue {

    private String name;
    @Setter
    private PriorityQueue<QueuePlayer> players = new PriorityQueue<>(new QueuePlayerComparator());
    @Setter
    private boolean enabled;

    public Queue(String name) {
        this.name = name;
    }

    public boolean containsPlayer(UUID uuid) {
        for (QueuePlayer player : this.players) {
            if (player.getUuid().equals(uuid)) {
                return true;
            }
        }

        return false;
    }

    public int getPosition(UUID uuid) {
        if (!this.containsPlayer(uuid)) {
            return 0;
        }

        PriorityQueue<QueuePlayer> queue = new PriorityQueue<>(this.players);

        int position = 0;

        while (!queue.isEmpty()) {
            QueuePlayer player = queue.poll();

            if (player.getUuid().equals(uuid)) {
                break;
            }

            position++;
        }

        return position + 1;
    }

}
