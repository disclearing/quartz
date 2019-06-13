package com.minexd.quartz.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;

@Getter
public class Config {

	private String[] queues;
	private String redisHost;
	private int redisPort;
	private String redisPassword;

	public Config() {
		File file = new File("config.properties");

		if (!file.exists()) {
			try {
				file.createNewFile();

				FileOutputStream output = new FileOutputStream(file);
				output.write("queues=test1,test2,test3\n".getBytes());
				output.write("redis-host=127.0.0.1\n".getBytes());
				output.write("redis-port=6379\n".getBytes());
				output.write("redis-password=dev\n".getBytes());
				output.flush();
				output.close();
			} catch (IOException io) {
				io.printStackTrace();
			}
		}

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("config.properties");

			prop.load(input);

			String queues = ((String) prop.getOrDefault("queues", ""));

			this.queues = queues.equals("") ? new String[0] : queues.split(",");
			this.redisHost = ((String) prop.getOrDefault("redis-host", "127.0.0.1"));
			this.redisPort = Integer.valueOf((String) prop.getOrDefault("redis-port", "6379"));
			this.redisPassword = ((String) prop.getOrDefault("redis-password", null));

			if (this.redisPassword.isEmpty()) {
				this.redisPassword = null;
			}
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
