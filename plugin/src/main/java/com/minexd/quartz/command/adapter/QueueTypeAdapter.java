package com.minexd.quartz.command.adapter;

import com.minexd.quartz.Quartz;
import com.qrakn.honcho.command.adapter.CommandTypeAdapter;

public class QueueTypeAdapter implements CommandTypeAdapter {

	@Override
	public <T> T convert(String string, Class<T> type) {
		return type.cast(Quartz.get().getQuartzData().getQueueByName(string));
	}

}
