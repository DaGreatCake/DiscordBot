package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.Cooldown;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.MergeListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.CooldownRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import com.cakedevs.ChildLabourBot.tools.Tools;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private CooldownRepository cooldownRepository;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // constraints
        int delayInMinutes = 360;

        AtomicBoolean child1chosen = new AtomicBoolean(false);
        AtomicBoolean child2chosen = new AtomicBoolean(false);
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean doneInside = new AtomicBoolean(false);


        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+merge")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                Optional<Cooldown> cooldown = cooldownRepository.findCooldownByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                if (System.nanoTime() > cooldown.get().getMergecooldown()) {
                    List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                    if (userChilds.size() > 1) {
                        cooldown.get().setMergecooldown(System.nanoTime() + (delayInMinutes * 60000000000L));
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
                    long difference = System.nanoTime() - cooldown.get().getMergecooldown();
                    messageCreateEvent.getChannel().sendMessage("Bro rustig man bro, je moet nog " + Tools.getReadableTime(difference) + " wachten.");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
