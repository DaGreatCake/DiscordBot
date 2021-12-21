package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.NeukseksListener;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class NeukseksListenerImpl implements NeukseksListener {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    private HashMap<String, Instant> cooldowns = new HashMap<String, Instant>();

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        AtomicBoolean done = new AtomicBoolean(false);
        boolean allow = true;

        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                allow = false;
            }
        }
        if (allow) {
            if(messageCreateEvent.getMessageContent().startsWith("+neukseks")) {
                String[] command = messageCreateEvent.getMessageContent().split(" ");
                if (command.length > 1) {
                    String userID = command[1];
                    userID = userID.replace("<", "");
                    userID = userID.replace("@", "");
                    userID = userID.replace("!", "");
                    userID = userID.replace(">", "");
                    Optional<User> userOpt = userRepository.findUserById(userID);

                    if (userOpt.isPresent()) {
                        if (messageCreateEvent.getMessageAuthor().getId() != Long.parseLong(userOpt.get().getId())) {
                            Random r = new Random();
                            int num1 = r.nextInt(20);
                            int num2 = r.nextInt(100);

                            try {
                                String finalUserID = userID;
                                messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                         messageCreateEvent.getApi().getUserById(userID).get().getName() + " wil je kontjebonken met "
                                                 + messageCreateEvent.getApi().getUserById(messageCreateEvent.getMessageAuthor().getId()).get().getName() + "?",
                                        "Degene die als eerst de rekensom oplost, raakt zwanger.",
                                        null,
                                        null,
                                        messageCreateEvent.getChannel())
                                .thenAccept(message -> {
                                    message.addReaction("\uD83D\uDC4D");
                                    message.addReaction("\uD83D\uDC4E");
                                    message.addReactionAddListener(listener -> {
                                        if (listener.getEmoji().equalsEmoji("\uD83D\uDC4D") && listener.getUser().get().getId() == Long.parseLong(finalUserID)) {
                                            message.edit(new EmbedBuilder()
                                                    .setTitle("Lekkere neukseks hmmm")
                                                    .setDescription(num1 + " * " + num2)
                                                    .setFooter("ziek man"));
                                            message.getChannel().addMessageCreateListener(messageCreateListener -> {
                                                Instant cooldown = LocalDateTime.now().plusHours(4).toInstant(ZoneOffset.UTC);
                                                if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
                                                    if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC)) && !done.get()) {
                                                        cooldowns.replace(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                                                    }
                                                } else {
                                                    cooldowns.put(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                                                }
                                                if (messageCreateListener.getMessageContent().equals(Integer.toString(num1*num2)) && !done.get()) {
                                                    messageCreateEvent.getChannel().sendMessage(messageCreateListener.getMessageAuthor().getName() + " took the kids. Can I at least see them at Christmas?");
                                                    done.set(true);
                                                }
                                            });
                                        } else if (listener.getEmoji().equalsEmoji("\uD83D\uDC4E") && listener.getUser().get().getId() == Long.parseLong(finalUserID)) {
                                            message.edit(new EmbedBuilder()
                                                    .setTitle("Jammer dan")
                                                    .setDescription("Geen neukseks for you."));
                                        }
                                    });
                                });
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        } else {
                            messageCreateEvent.getChannel().sendMessage("Bro wtf fak? je kan dit letterlijk niet op jezelf doen.");
                        }
                    } else {
                        messageCreateEvent.getChannel().sendMessage("Deze meneer heeft geen ChildLabourSimulator account.");
                    }
                } else {
                    messageCreateEvent.getChannel().sendMessage("Bro ga iemand pingen in je command dan ofzo?");
                }
            }
        } else {
            Duration difference = Duration.between(LocalDateTime.now().toInstant(ZoneOffset.UTC), cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()));
            messageCreateEvent.getChannel().sendMessage("Bro rustig man bro, je moet nog " + difference.toHours() + " uur, "
                    + difference.toMinutesPart() + " minuten en " + difference.toSecondsPart() + " seconden wachten.");
        }
    }
}
