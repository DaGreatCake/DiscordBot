package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.Cooldown;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.HealListener;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class HealListenerImpl implements HealListener {
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
        int delayInMinutes = 180;

        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean doneInside = new AtomicBoolean(false);

        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+heal")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                Optional<Cooldown> cooldown = cooldownRepository.findCooldownByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                if (System.nanoTime() > cooldown.get().getHealcooldown()) {
                    List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                    if (userChilds.size() != 0) {
                        cooldown.get().setHealcooldown(System.nanoTime() + (delayInMinutes * 60000000000L));
                        cooldownRepository.save(cooldown.get());
                        String childChoose = "";

                        for (Child child : userChilds) {
                            childChoose += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                    + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + ", max hitpoints: " + child.getHealthpointsmax() + "\n";
                        }

                        messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                "Kies een child id om te healen",
                                childChoose,
                                null,
                                null,
                                messageCreateEvent.getChannel());

                        AtomicLong childId = new AtomicLong(-1);

                        messageCreateEvent.getChannel().addMessageCreateListener(chooseListener -> {
                            if (chooseListener.getMessageAuthor().getId() == messageCreateEvent.getMessageAuthor().getId() && !done.get()) {
                                long id;
                                try {
                                    id = Long.parseLong(chooseListener.getMessageContent());
                                    for (Child child : userChilds) {
                                        if (child.getId() == id) {
                                            childId.set(id);
                                            done.set(true);
                                        }
                                    }
                                    if (!done.get()) {
                                        messageCreateEvent.getChannel().sendMessage("Bro die bestaat niet.");
                                    }
                                } catch (NumberFormatException ex) {
                                    messageCreateEvent.getChannel().sendMessage("Bro dat is letterlijk geen getal.");
                                }
                            }

                            if (done.get() && !doneInside.get()) {
                                doneInside.set(true);

                                Optional<Child> childOpt = childRepository.findChildById(childId.get());
                                if (childOpt.isPresent()) {
                                    childOpt.get().setHealthpoints(childOpt.get().getHealthpointsmax());
                                    Child child = childRepository.save(childOpt.get());

                                    messageCreateEvent.getChannel().sendMessage(child.getName() + " vindt je heel lief.\nHij heeft nu "
                                            + child.getHealthpoints() + " hitpoints.");
                                }
                            }
                        }).removeAfter(60, TimeUnit.SECONDS);
                    } else {
                        messageCreateEvent.getChannel().sendMessage("Gast je kan niemand healen als je geen kinderen hebt.\nDoe eerst +neukseks");
                    }
                } else {
                    long difference = System.nanoTime() - cooldown.get().getHealcooldown();
                    messageCreateEvent.getChannel().sendMessage("Bro rustig man bro, je moet nog " + Tools.getReadableTime(difference) + " wachten.");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
