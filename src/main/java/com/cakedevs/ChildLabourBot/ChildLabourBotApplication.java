package com.cakedevs.ChildLabourBot;

import com.cakedevs.ChildLabourBot.listeners.SpeakListener;
import com.cakedevs.ChildLabourBot.listeners.UserListener;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ChildLabourBotApplication {

	@Autowired
	private Environment env;

	@Autowired
	private SpeakListener speakListener;

	@Autowired
	private UserListener userListener;

	public static void main(String[] args) {
		SpringApplication.run(ChildLabourBotApplication.class, args);
	}

	@Bean
	@ConfigurationProperties(value = "discord-api")
	public DiscordApi discordApi() {
		String token = env.getProperty("TOKEN");
		DiscordApi api = new DiscordApiBuilder().setToken(token)
				.setAllNonPrivilegedIntents()
				.login()
				.join();

		api.addMessageCreateListener(speakListener);
		api.addMessageCreateListener(userListener);

		return api;
	}
}
