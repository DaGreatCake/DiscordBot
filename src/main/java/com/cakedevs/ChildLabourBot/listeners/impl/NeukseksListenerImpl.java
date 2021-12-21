package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.NeukseksListener;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

@Component
public class NeukseksListenerImpl implements NeukseksListener {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageContent().startsWith("#neukseks")) {
            messageCreateEvent.getChannel().type();
            String[] command = messageCreateEvent.getMessageContent().split(" ");
            if (command.length > 1) {
                String userID = command[1];
                Optional<User> userOpt = userRepository.findUserById(userID);
                if (userOpt.isPresent()) {
                    Random r = new Random();
                    int num1 = r.nextInt(20);
                    int num2 = r.nextInt(100);

                    messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                            "<@" + userOpt.get().getId() + "> wil je kontjebonken met <@" + messageCreateEvent.getMessageAuthor().getId() + ">?",
                            "Degene die als eerst de rekensom oplost, raakt zwanger.",
                            null,
                            null,
                            messageCreateEvent.getChannel())
                    .thenAccept(message -> {
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

                }
            }
        }
    }
}
