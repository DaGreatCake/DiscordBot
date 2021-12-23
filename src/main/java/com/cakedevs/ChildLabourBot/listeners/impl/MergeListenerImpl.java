package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.MergeListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
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
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MergeListenerImpl implements MergeListener {
    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    private HashMap<String, Instant> cooldowns = new HashMap<String, Instant>();

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // constraints
        int delayInMinutes = 360;

        AtomicBoolean child1chosen = new AtomicBoolean(false);
        AtomicBoolean child2chosen = new AtomicBoolean(false);
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean doneInside = new AtomicBoolean(false);
        boolean allow = true;

        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                allow = false;
            }
        }

        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+merge")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                if (allow) {
                    List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                    if (userChilds.size() > 1) {
                        Instant cooldown = LocalDateTime.now().plusMinutes(delayInMinutes).toInstant(ZoneOffset.UTC);

                        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
                            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC)) && !done.get()) {
                                cooldowns.replace(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                            }
                        } else {
                            cooldowns.put(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                        }

                        done.set(true);
                        String userChildChoose1 = "";
                        for (Child child : userChilds) {
                            userChildChoose1 += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                    + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + "\n";
                        }

                        AtomicLong child1Id = new AtomicLong(-1);
                        AtomicLong child2Id = new AtomicLong(-1);

                        messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                "Kies een child id om meer hitpoints te geven.",
                                userChildChoose1,
                                null,
                                null,
                                messageCreateEvent.getChannel());

                        messageCreateEvent.getChannel().addMessageCreateListener(firstChooseListener -> {
                            if (firstChooseListener.getMessageAuthor().getId() == messageCreateEvent.getMessageAuthor().getId() && !child1chosen.get()) {
                                long id;
                                try {
                                    id = Long.parseLong(firstChooseListener.getMessageContent());
                                    for (Child child : userChilds) {
                                        if (child.getId() == id) {
                                            child1Id.set(id);
                                            child1chosen.set(true);
                                        }
                                    }
                                    if (!child1chosen.get()) {
                                        messageCreateEvent.getChannel().sendMessage("Bro die bestaat niet.");
                                    }
                                } catch (NumberFormatException ex) {
                                    messageCreateEvent.getChannel().sendMessage("Bro dat is letterlijk geen getal.");
                                }
                            }

                            if (child1chosen.get() && !doneInside.get()) {
                                String userChildChoose2 = "";
                                for (Child child : userChilds) {
                                    if (child.getId() != child1Id.get()) {
                                        userChildChoose2 += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                                + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + "\n";
                                    }
                                }

                                doneInside.set(true);
                                messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                        "Kies een ander child id in de vorige te mergen.",
                                        userChildChoose2,
                                        null,
                                        null,
                                        messageCreateEvent.getChannel());

                                messageCreateEvent.getChannel().addMessageCreateListener(secondChooseListener -> {
                                    if (secondChooseListener.getMessageAuthor().getId() == messageCreateEvent.getMessageAuthor().getId() && child1chosen.get() && !child2chosen.get()) {
                                        long id;
                                        try {
                                            id = Long.parseLong(secondChooseListener.getMessageContent());
                                            for (Child child : userChilds) {
                                                if (child.getId() == id && id != child1Id.get()) {
                                                    child2Id.set(id);
                                                    child2chosen.set(true);
                                                }
                                            }
                                            if (!child2chosen.get()) {
                                                messageCreateEvent.getChannel().sendMessage("Bro die bestaat niet.");
                                            }
                                        } catch (NumberFormatException ex) {
                                            messageCreateEvent.getChannel().sendMessage("Bro dat is letterlijk geen getal.");
                                        }
                                    }

                                    if (child2chosen.get()) {
                                        Optional<Child> child1 = childRepository.findChildById(child1Id.get());
                                        Optional<Child> child2 = childRepository.findChildById(child2Id.get());

                                        int newHealthpointsmax = child1.get().getHealthpointsmax() + (child2.get().getHealthpointsmax() / 2);
                                        int newHealthpoints = child1.get().getHealthpoints() + (child2.get().getHealthpoints() / 2);

                                        child1.get().setHealthpointsmax(newHealthpointsmax);
                                        child1.get().setHealthpoints(newHealthpoints);
                                        Child child = childRepository.save(child1.get());
                                        childRepository.deleteById(child2.get().getId());

                                        messageCreateEvent.getChannel().sendMessage("Holy shit " + child1.get().getName()
                                                + " heeft nu " + child1.get().getHealthpointsmax() + " max hitpoints.\nHij bezit op het moment over "
                                                + child1.get().getHealthpoints() + " hitpoints.\nHelaas is " + child2.get().getName() + " hierbij overleden.");
                                    }
                                });
                            }
                        });
                    } else {
                        messageCreateEvent.getChannel().sendMessage("Je hebt minimaal 2 kinderen nodig, ga eerst neukseksen ofzo");
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
