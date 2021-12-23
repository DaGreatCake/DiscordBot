package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.KidnapListener;
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
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class KidnapListenerImpl implements KidnapListener {
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
        int delayInMinutes = 1440;
        int kidnapCost = 20000;

        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean doneInside = new AtomicBoolean(false);
        boolean allow = true;

        if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
            if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                allow = false;
            }
        }

        if(messageCreateEvent.getMessageContent().toLowerCase().startsWith("+kidnap")) {
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
                            if (messageCreateEvent.getMessageAuthor().getId() != Long.parseLong(userOpt.get().getId())) {
                                List<Child> enemyChilds = childRepository.findChildsByUserid(userOpt.get().getId());
                                if (enemyChilds.size() != 0) {
                                    Instant cooldown = LocalDateTime.now().plusMinutes(delayInMinutes).toInstant(ZoneOffset.UTC);

                                    if (cooldowns.containsKey(messageCreateEvent.getMessageAuthor().getIdAsString())) {
                                        if (cooldowns.get(messageCreateEvent.getMessageAuthor().getIdAsString()).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC)) && !done.get()) {
                                            cooldowns.replace(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                                        }
                                    } else {
                                        cooldowns.put(messageCreateEvent.getMessageAuthor().getIdAsString(), cooldown);
                                    }

                                    String childChoose = "";

                                    for (Child child : enemyChilds) {
                                        childChoose += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                                + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + ", max hitpoints: " + child.getHealthpointsmax() + "\n";
                                    }

                                    messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                                            "Kies een child id om te kidnappen",
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
                                                for (Child child : enemyChilds) {
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
                                                if (userOptPrimary.get().getBedrock() >= kidnapCost) {
                                                    userOptPrimary.get().setBedrock(userOptPrimary.get().getBedrock() - kidnapCost);
                                                    User user = userRepository.save(userOptPrimary.get());

                                                    Random r = new Random();
                                                    int chance = r.nextInt(100);
                                                    if (chance < 50) {
                                                        childOpt.get().setUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                                                        Child child = childRepository.save(childOpt.get());

                                                        messageCreateEvent.getChannel().sendMessage("No way je hebt " + child.getName() + " je van ingelokt.\nHij is nu van jou.");
                                                    } else {
                                                        messageCreateEvent.getChannel().sendMessage("Hij had het door en is hem geboekt.");
                                                    }

                                                    messageCreateEvent.getChannel().sendMessage("Je hebt nog " + userOptPrimary.get().getBedrock() + " bedrock.");
                                                } else {
                                                    messageCreateEvent.getChannel().sendMessage("Bro letterlijk te weinig bedrock noob.");
                                                }
                                            }
                                        }
                                    }).removeAfter(30, TimeUnit.SECONDS);
                                } else {
                                    messageCreateEvent.getChannel().sendMessage("Deze noob heeft geen kinderen om te jatten.");
                                }
                            } else {
                                messageCreateEvent.getChannel().sendMessage("Niet jezelf pingen man bro.");
                            }
                        } else {
                            messageCreateEvent.getChannel().sendMessage("Deze meneer heeft geen ChildLabourSimulator account.");
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
