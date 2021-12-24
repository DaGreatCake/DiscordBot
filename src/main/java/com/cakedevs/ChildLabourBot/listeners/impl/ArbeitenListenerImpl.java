package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.ArbeitenListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ArbeitenListenerImpl implements ArbeitenListener {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    private HashMap<String, Instant> cooldowns = new HashMap<String, Instant>();

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // constraints
        int delayInMinutes = 10;

        AtomicBoolean done = new AtomicBoolean(false);
        boolean allow = true;
        int bedrockMined = 0;

        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                allow = false;
            }
        }

        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+arbeiten")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                if (allow) {
                    List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                    if (userChilds.size() != 0) {
                        Instant cooldown = LocalDateTime.now().plusMinutes(delayInMinutes).toInstant(ZoneOffset.UTC);

                        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
                            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC)) && !done.get()) {
                                cooldowns.replace(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                            }
                        } else {
                            cooldowns.put(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                        }

                        done.set(true);
                        for (Child child : userChilds) {
                            bedrockMined += child.getMiningspeed();
                        }

                        userOptPrimary.get().setBedrock(userOptPrimary.get().getBedrock() + bedrockMined);
                        User user = userRepository.save(userOptPrimary.get());
                        messageCreateEvent.getChannel().sendMessage("Lekker man, je kinderen hebben " + bedrockMined + " bedrock gemined.");
                        messageCreateEvent.getChannel().sendMessage("Je hebt nu " + user.getBedrock() + " bedrock.");

                    } else {
                        messageCreateEvent.getChannel().sendMessage("Gast je kan niemand laten werken als je geen kindslaven hebt.\nDoe eerst +neukseks");
                    }
                } else {
                    Duration difference = Duration.between(LocalDateTime.now().toInstant(ZoneOffset.UTC), cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()));
                    messageCreateEvent.getChannel().sendMessage("Bro rustig man bro, je moet nog " + difference.toHours() + " uur, "
                            + difference.toMinutesPart() + " minuten en " + difference.toSecondsPart() + " seconden wachten.");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
