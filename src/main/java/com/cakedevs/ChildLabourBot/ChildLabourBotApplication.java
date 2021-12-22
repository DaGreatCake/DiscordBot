package com.cakedevs.ChildLabourBot;

import com.cakedevs.ChildLabourBot.listeners.*;
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
	private ArbeitenListener arbeitenListener;

	@Autowired
	private NeukseksListener neukseksListener;

	@Autowired
	private UserListener userListener;

	@Autowired
	private BalListener balListener;

	public static void main(String[] args) {
		SpringApplication.run(ChildLabourBotApplication.class, args);
	}

	@Bean
	@ConfigurationProperties(value = "discord-api")
	public DiscordApi discordApi() {
		String token = env.getProperty("TOKEN");
		DiscordApi api = new DiscordApiBuilder().setToken(token)
				.setAllIntents()
				.login()
				.join();

		api.addMessageCreateListener(arbeitenListener);
		api.addMessageCreateListener(neukseksListener);
		api.addMessageCreateListener(userListener);
		api.addMessageCreateListener(balListener);
		return api;
	}
}
