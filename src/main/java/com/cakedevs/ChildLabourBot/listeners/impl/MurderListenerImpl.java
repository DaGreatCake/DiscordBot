package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.MurderListener;
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
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MurderListenerImpl implements MurderListener {
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
        int delayInMinutes = 1;

        AtomicBoolean victimChosen = new AtomicBoolean(false);
        AtomicBoolean attackerChosen = new AtomicBoolean(false);
        AtomicBoolean done = new AtomicBoolean(false);
        boolean allow = true;

        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                allow = false;
            }
        }

        if(messageCreateEvent.getMessageContent().toLowerCase(Locale.ROOT).startsWith("+murder")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                if (allow) {
                    String[] command = messageCreateEvent.getMessageContent().split(" ");
                    if (command.length > 1) {
                        String userID = command[1];
                        userID = userID.replace("<", "");
                        userID = userID.replace("@", "");
                        userID = userID.replace("!", "");
                        userID = userID.replace(">", "");
                        Optional<User> userOpt = userRepository.findUserById(userID);

                        if (userOpt.isPresent()) {
                            if (!messageCreateEvent.getMessageAuthor().getIdAsString().equals(userOpt.get().getId())) {
                                List<Child> enemyChilds = childRepository.findChildsByUserid(userOpt.get().getId());
                                List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                                if (enemyChilds.size() != 0) {
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
                                        String enemyChildChoose = "";
                                        String userChildChoose = "";
                                        for (Child child : enemyChilds) {
                                            enemyChildChoose += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                                    + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + "\n";
                                        }
                                        for (Child child : userChilds) {
                                            userChildChoose += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                                    + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + "\n";
                                        }

                                        AtomicLong victimChildId = new AtomicLong(-1);
                                        AtomicLong attackerChildId = new AtomicLong(-1);

                                        messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                                "Kies een child id om aan te vallen",
                                                enemyChildChoose,
                                                null,
                                                null,
                                                messageCreateEvent.getChannel());

                                        messageCreateEvent.getChannel().addMessageCreateListener(enemyChooseListener -> {
                                            if (enemyChooseListener.getMessageAuthor().getId() == messageCreateEvent.getMessageAuthor().getId() && !victimChosen.get()) {
                                                long id;
                                                try {
                                                    id = Long.parseLong(enemyChooseListener.getMessageContent());
                                                    for (Child child : enemyChilds) {
                                                        if (child.getId() == id) {
                                                            victimChildId.set(id);
                                                            victimChosen.set(true);
                                                        }
                                                    }
                                                    if (!victimChosen.get()) {
                                                        messageCreateEvent.getChannel().sendMessage("Bro die bestaat niet.");
                                                    }
                                                } catch (NumberFormatException ex) {
                                                    messageCreateEvent.getChannel().sendMessage("Bro dat is letterlijk geen getal.");
                                                }
                                            }
                                        });

                                        if (victimChosen.get()) {
                                            messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                                    "Kies een child id die de moordpoging doet",
                                                    userChildChoose,
                                                    null,
                                                    null,
                                                    messageCreateEvent.getChannel());

                                            messageCreateEvent.getChannel().addMessageCreateListener(attackerChooseListener -> {
                                                if (attackerChooseListener.getMessageAuthor().getId() == messageCreateEvent.getMessageAuthor().getId() && victimChosen.get() && !attackerChosen.get()) {
                                                    long id;
                                                    try {
                                                        id = Long.parseLong(attackerChooseListener.getMessageContent());
                                                        for (Child child : userChilds) {
                                                            if (child.getId() == id) {
                                                                attackerChildId.set(id);
                                                                attackerChosen.set(true);
                                                            }
                                                        }
                                                        if (!attackerChosen.get()) {
                                                            messageCreateEvent.getChannel().sendMessage("Bro die bestaat niet.");
                                                        }
                                                    } catch (NumberFormatException ex) {
                                                        messageCreateEvent.getChannel().sendMessage("Bro dat is letterlijk geen getal.");
                                                    }
                                                }

                                                if (attackerChosen.get()) {
                                                    Optional<Child> victimChild = childRepository.findChildById(victimChildId.get());
                                                    Optional<Child> attackerChild = childRepository.findChildById(attackerChildId.get());
                                                    int victimHealthpoints = victimChild.get().getHealthpoints();
                                                    int attackerHealthpoints = attackerChild.get().getHealthpoints();

                                                    if (attackerHealthpoints > victimHealthpoints) {
                                                        attackerHealthpoints -= victimHealthpoints;

                                                        messageCreateEvent.getChannel().sendMessage("Holy shit " + attackerChild.get().getName()
                                                                + " heeft " + victimChild.get().getName() + " vermoord." + "\n"
                                                                + attackerChild.get().getName() + " heeft nu " + attackerHealthpoints + " hitpoints.");

                                                        attackerChild.get().setHealthpoints(attackerHealthpoints);
                                                        Child child = childRepository.save(attackerChild.get());
                                                        childRepository.deleteById(victimChild.get().getId());
                                                    } else if (attackerHealthpoints < victimHealthpoints) {
                                                        victimHealthpoints -= attackerHealthpoints;

                                                        messageCreateEvent.getChannel().sendMessage("Dit waren niet genoeg hitpoints om " + victimChild.get().getName()
                                                                + " te vermoorden. \n" + attackerChild.get().getName() + " is zelf dood. \n"
                                                                + victimChild.get().getName() + " heeft nu " + victimHealthpoints + " hitpoints.");

                                                        victimChild.get().setHealthpoints(victimHealthpoints);
                                                        Child child = childRepository.save(victimChild.get());
                                                        childRepository.deleteById(attackerChild.get().getId());
                                                    } else if (attackerHealthpoints == victimHealthpoints) {
                                                        messageCreateEvent.getChannel().sendMessage("Ze waren erg gewaagd aan elkaar.\nAls resultaat zijn beide kinderen dood.");

                                                        childRepository.deleteById(victimChild.get().getId());
                                                        childRepository.deleteById(attackerChild.get().getId());
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        messageCreateEvent.getChannel().sendMessage("Bro je bent letterlijk noob, ga eerst neukseksen ofzo");
                                    }
                                } else {
                                    messageCreateEvent.getChannel().sendMessage("Bro deze noob heeft geen kindjes.");
                                }
                            } else {
                                messageCreateEvent.getChannel().sendMessage("Bro niet je eigen kinderen killen.");
                            }
                        } else {
                            messageCreateEvent.getChannel().sendMessage("Bro deze noob heeft geen account.");
                        }
                    } else {
                        messageCreateEvent.getChannel().sendMessage("Bro ga iemand pingen ofzo.");
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
