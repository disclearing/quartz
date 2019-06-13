package com.minexd.quartz.queue;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueuePlayer implements Comparable {

    private UUID uuid;
    private QueueRank rank;
    private long inserted;

    public QueuePlayer(JsonObject object) {
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.rank = new QueueRank(object.get("rank").getAsJsonObject());

        if (object.has("inserted-at")) {
            this.inserted = object.get("inserted-at").getAsLong();
        }
    }

    @Override
    public int compareTo(Object object) {
        int result = 0;

        if (object instanceof QueuePlayer) {
            QueuePlayer otherPlayer = (QueuePlayer) object;
            result = this.rank.getPriority() - otherPlayer.getRank().getPriority();

            if (result == 0) {
                if (this.inserted < otherPlayer.getInserted()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        return result;
    }

}
