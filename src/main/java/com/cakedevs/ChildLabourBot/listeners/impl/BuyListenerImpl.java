package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.Upgrades;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.BuyListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.UpgradesRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import com.cakedevs.ChildLabourBot.services.UpgradesService;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class BuyListenerImpl implements BuyListener {
    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UpgradesRepository upgradesRepository;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        //constraints
        int statusPrice = 2000;
        int speedUpgradePrice = 200;
        int maxChildUpgradePrice = 1000;
        int userMaxChildUpgradePrice = 1000;

        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean doneInside = new AtomicBoolean(false);

        if(messageCreateEvent.getMessageContent().toLowerCase().startsWith("+buy")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
            Optional<Upgrades> upgrades = upgradesRepository.findUpgradesByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                String[] command = messageCreateEvent.getMessageContent().split(" ");
                if (command.length == 1) {
                    userMaxChildUpgradePrice = upgrades.get().getMaxchildsupgrade() * maxChildUpgradePrice;

                    String shopOptions = "Mining speed upgrade, id: mining, cost: " + speedUpgradePrice + " bedrock\n"
                            + "Max childs upgrade, id: max, cost: " + userMaxChildUpgradePrice + " bedrock\n"
                            + "Status, id: status, cost: " + statusPrice + " bedrock.";
    //                for (Child child : userChilds) {
    //                    userChildChoose1 += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
    //                            + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + "\n";
    //                }

                    messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                            "+buy [id] [aantal]",
                            shopOptions,
                            null,
                            null,
                            messageCreateEvent.getChannel());
                } else if (command.length == 3) {
                    String item = command[1];
                    try {
                        int amount = Integer.parseInt(command[2]);
                        if (item.equals("status")) {
                            if (amount >= 1) {
                                int cost = amount * statusPrice;
                                if (userOptPrimary.get().getBedrock() >= cost) {
                                    userOptPrimary.get().setBedrock(userOptPrimary.get().getBedrock() - cost);
                                    userOptPrimary.get().setStatus(userOptPrimary.get().getStatus() + amount);
                                    User userSave = userRepository.save(userOptPrimary.get());
                                    messageCreateEvent.getChannel().sendMessage("Ziek man je hebt " + amount + " status gekocht.\n"
                                            + "Je hebt nu " + userOptPrimary.get().getStatus() + " status en " + userOptPrimary.get().getBedrock() + " bedrock.");
                                } else {
                                    messageCreateEvent.getChannel().sendMessage("Bro je hebt te weinig bedrock. Laat je kinderen harder werken.");
                                }
                            } else {
                                messageCreateEvent.getChannel().sendMessage("Je moet minimaal 1 kopen.");
                            }
                        } else if (item.equals("max")) {
                            if (amount >= 1) {
                                int cost = 0;
                                userMaxChildUpgradePrice = upgrades.get().getMaxchildsupgrade() * maxChildUpgradePrice;
                                for (int i = upgrades.get().getMaxchildsupgrade(); i <= upgrades.get().getMaxchildsupgrade() + amount - 1; i++) {
                                    cost += i * 1000;
                                }

                                if (userOptPrimary.get().getBedrock() >= cost) {
                                    userOptPrimary.get().setBedrock(userOptPrimary.get().getBedrock() - cost);
                                    upgrades.get().setMaxchildsupgrade(upgrades.get().getMaxchildsupgrade() + amount);
                                    User userSave = userRepository.save(userOptPrimary.get());
                                    Upgrades upgradesSave = upgradesRepository.save(upgrades.get());
                                    messageCreateEvent.getChannel().sendMessage("Ziek man je hebt " + amount + " extra kinderslots gekocht.\n"
                                            + "Je kan nu " + upgrades.get().getMaxchildsupgrade() + 9 + " kinderen hebben.\n"
                                            + "Je hebt nog " + userOptPrimary.get().getBedrock() + " bedrock.");
                                } else {
                                    messageCreateEvent.getChannel().sendMessage("Bro je hebt te weinig bedrock. Laat je kinderen harder werken.");
                                }
                            } else {
                                messageCreateEvent.getChannel().sendMessage("Je moet minimaal 1 kopen.");
                            }
                        } else if (item.equals("mining")) {
                            if (amount >= 1) {
                                if (userOptPrimary.get().getBedrock() >= speedUpgradePrice) {
                                    String childChoose = "";

                                    for (Child child : userChilds) {
                                        childChoose += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                                + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + ", max hitpoints: " + child.getHealthpointsmax() + "\n";
                                    }

                                    messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                            "Kies een child id om de mining speed te upgraden",
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
                                                userOptPrimary.get().setBedrock(userOptPrimary.get().getBedrock() - speedUpgradePrice * amount);
                                                User userSave = userRepository.save(userOptPrimary.get());
                                                childOpt.get().setMiningspeed(childOpt.get().getMiningspeed() + 1);
                                                Child child = childRepository.save(childOpt.get());

                                                messageCreateEvent.getChannel().sendMessage(child.getName() + " werkt nu bonkie hard.\nHij heeft nu "
                                                        + child.getMiningspeed() + " mining speed.\nJij beschikt nog over " + userOptPrimary.get().getBedrock() + " bedrock.");
                                            }
                                        }
                                    }).removeAfter(30, TimeUnit.SECONDS);
                                } else {
                                    messageCreateEvent.getChannel().sendMessage("Bro je hebt te weinig bedrock. Laat je kinderen harder werken.");
                                }
                            } else {
                                messageCreateEvent.getChannel().sendMessage("Je moet minimaal 1 kopen.");
                            }
                        }
                    } catch (NumberFormatException ex) {
                        messageCreateEvent.getChannel().sendMessage("Bro dat is geen getal.");
                    }
                } else {
                    messageCreateEvent.getChannel().sendMessage("Bro letterlijk verkeerde input.");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
