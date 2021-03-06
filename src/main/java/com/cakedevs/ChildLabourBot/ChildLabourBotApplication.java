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
	private BalListener balListener;

	@Autowired
	private BuyListener buyListener;

	@Autowired
	private HealListener healListener;

	@Autowired
	private HelpListener helpListener;

	@Autowired
	private KidnapListener kidnapListener;

	@Autowired
	private ListChildsListener listChildsListener;

	@Autowired
	private MergeListener mergeListener;

	@Autowired
	private MurderListener murderListener;

	@Autowired
	private NeukseksListener neukseksListener;

	@Autowired
	private StatusListener statusListener;

	@Autowired
	private TopListener topListener;

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
				.setAllIntents()
				.login()
				.join();

		api.addMessageCreateListener(arbeitenListener);
		api.addMessageCreateListener(balListener);
		api.addMessageCreateListener(buyListener);
		api.addMessageCreateListener(healListener);
		api.addMessageCreateListener(helpListener);
		api.addMessageCreateListener(kidnapListener);
		api.addMessageCreateListener(listChildsListener);
		api.addMessageCreateListener(mergeListener);
		api.addMessageCreateListener(murderListener);
		api.addMessageCreateListener(neukseksListener);
		api.addMessageCreateListener(statusListener);
		api.addMessageCreateListener(topListener);
		api.addMessageCreateListener(userListener);

		return api;
	}
}
