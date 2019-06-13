package com.minexd.quartz.thread;

import com.minexd.quartz.packet.PlayerSendPacket;
import com.minexd.quartz.queue.Queue;
import com.minexd.quartz.server.Server;
import com.minexd.quartz.util.JsonChain;
import com.minexd.quartz.Quartz;
import com.minexd.quartz.queue.QueuePlayer;

public class QueueThread extends Thread {

	private static final Long SEND_DELAY = 500L;

	@Override
	public void run() {
		while (true) {
			try {
				for (Queue queue : Quartz.get().getQuartzData().getQueues()) {
					if (!this.canSend(queue)) {
						continue;
					}

					QueuePlayer next = queue.getPlayers().poll();

					if (next != null) {
						Quartz.get().getPidgin().sendPacket(new PlayerSendPacket(new JsonChain()
								.addProperty("server", queue.getName())
								.addProperty("uuid", next.getUuid().toString())
								.get()));
					}
				}

				Thread.sleep(SEND_DELAY);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private boolean canSend(Queue queue) {
		if (queue.isEnabled()) {
			Server server = Quartz.get().getQuartzData().getServerById(queue.getName());

			if (server != null) {
				return server.isOnline() && !server.isWhitelisted() &&
				       server.getOnlinePlayers() < server.getMaximumPlayers();
			}
		}

		return false;
	}

}
