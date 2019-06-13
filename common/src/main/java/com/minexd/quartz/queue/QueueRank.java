package com.minexd.quartz.queue;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueueRank implements Comparable {

    private String name;
    private int priority;

    public QueueRank(JsonObject object) {
        this.name = object.get("name").getAsString();
        this.priority = object.get("priority").getAsInt();
    }

    @Override
    public int compareTo(Object o) {
        int result = 0;

        if (o instanceof QueueRank) {
            result = this.priority - ((QueueRank) o).priority;
        }

        return result;
    }

}
