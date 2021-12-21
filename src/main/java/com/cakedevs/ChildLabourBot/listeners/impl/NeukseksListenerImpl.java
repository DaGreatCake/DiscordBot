package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.ChildLabourBotApplication;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.NeukseksListener;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class NeukseksListenerImpl implements NeukseksListener {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        ChildLabourBotApplication childLabourBotApplication = new ChildLabourBotApplication();
        DiscordApi discordApi = childLabourBotApplication.discordApi();

        if(messageCreateEvent.getMessageContent().startsWith("#neukseks")) {
            messageCreateEvent.getChannel().sendMessage("piemoool");
            String[] command = messageCreateEvent.getMessageContent().split(" ");
            if (command.length > 1) {
                String userID = command[1];
                Optional<User> userOpt = userRepository.findUserById(userID);
                CompletableFuture<org.javacord.api.entity.user.User> userOptional1 = discordApi.getUserById(userID);
                CompletableFuture<org.javacord.api.entity.user.User> userOptional2 = discordApi.getUserById(messageCreateEvent.getMessageAuthor().getId());
                if (userOpt.isPresent()) {
                    if (messageCreateEvent.getMessageAuthor().getId() != Long.parseLong(userOpt.get().getId())) {
                        Random r = new Random();
                        int num1 = r.nextInt(20);
                        int num2 = r.nextInt(100);

                        try {
                            messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                     userOptional1.get().getMentionTag() + " wil je kontjebonken met " + userOptional2.get().getMentionTag() + "?",
                                    "Degene die als eerst de rekensom oplost, raakt zwanger.",
                                    null,
                                    null,
                                    messageCreateEvent.getChannel())
                            .thenAccept(message -> {
                                message.addReaction("\uD83D\uDC4D");
                                message.addReaction("\uD83D\uDC4E");
                                message.addReactionAddListener(listener -> {
                                    if (listener.getEmoji().equalsEmoji("\uD83D\uDC4D") && listener.getUser().get().getId() == Long.parseLong(userID)) {
                                        message.edit(new EmbedBuilder()
                                                .setTitle("Lekkere neukseks hmmm")
                                                .setDescription(num1 + " * " + num2)
                                                .setFooter("ziek man"));
                                    } else if (listener.getEmoji().equalsEmoji("\uD83D\uDC4E") && listener.getUser().get().getId() == Long.parseLong(userID)) {
                                        message.edit(new EmbedBuilder()
                                                .setTitle("Jammer dan")
                                                .setDescription("Geen neukseks for you."));
                                    }
                                }); // listener.getUser().get().getName()
                            });
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
