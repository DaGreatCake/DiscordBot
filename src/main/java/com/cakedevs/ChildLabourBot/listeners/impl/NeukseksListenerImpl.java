package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.Cooldown;
import com.cakedevs.ChildLabourBot.entities.Upgrades;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.NeukseksListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.CooldownRepository;
import com.cakedevs.ChildLabourBot.repository.UpgradesRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.ChildService;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import com.cakedevs.ChildLabourBot.tools.Tools;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NeukseksListenerImpl implements NeukseksListener {
    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ChildService childService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UpgradesRepository upgradesRepository;

    @Autowired
    private CooldownRepository cooldownRepository;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // constraints
        AtomicInteger maxChilds = new AtomicInteger(10);
        int delayInMinutes = 10;
        int loserBedrockGiven = 200;

        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean active = new AtomicBoolean(false);
        AtomicBoolean thumbsDown = new AtomicBoolean(false);
        AtomicBoolean thumbsUp = new AtomicBoolean(false);
        AtomicBoolean childCreated = new AtomicBoolean(false);

        if(messageCreateEvent.getMessageContent().toLowerCase().startsWith("+neukseks")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                Optional<Cooldown> cooldown = cooldownRepository.findCooldownByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                if (System.nanoTime() > cooldown.get().getNeuksekscooldown()) {
                    String[] command = messageCreateEvent.getMessageContent().split(" ");
                    if (command.length > 1) {
                        String userID = command[1];
                        userID = userID.replace("<", "");
                        userID = userID.replace("@", "");
                        userID = userID.replace("!", "");
                        userID = userID.replace(">", "");
                        Optional<User> userOpt = userRepository.findUserById(userID);

                        if (userOpt.isPresent() && !done.get()) {
                            //if (childRepository.findChildsByUserid(userOpt.get().getId()).size() < maxChilds) {
                            if (messageCreateEvent.getMessageAuthor().getId() != Long.parseLong(userOpt.get().getId()) && cooldown.get().getNeuksekscooldown() != -1) {
                                cooldown.get().setNeuksekscooldown(-1);
                                Random r = new Random();
                                int num1 = r.nextInt(11);
                                int num2 = r.nextInt(100);

                                try {
                                    // make sure to not have double instances of the neukseks
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
                                        message.addReaction("\u274C");
                                        message.addReactionAddListener(listener -> {
                                            if (listener.getEmoji().equalsEmoji("\uD83D\uDC4D") && listener.getUser().get().getId() == Long.parseLong(finalUserID) && !thumbsDown.get()) {
                                                thumbsUp.set(true);
                                                message.edit(new EmbedBuilder()
                                                        .setTitle("Lekkere neukseks hmmm")
                                                        .setDescription(num1 + " * " + num2)
                                                        .setFooter("ziek man"));
                                                message.getChannel().addMessageCreateListener(answerListener -> {
                                                    if (answerListener.getMessageContent().equals(Integer.toString(num1 * num2)) && !done.get()
                                                            && (answerListener.getMessageAuthor().getId() == listener.getUserId() || answerListener.getMessageAuthor().getId() == messageCreateEvent.getMessageAuthor().getId())) {
                                                        cooldown.get().setNeuksekscooldown(System.nanoTime() + (delayInMinutes * 60000000000L));
                                                        cooldownRepository.save(cooldown.get());
                                                        done.set(true);
                                                        messageCreateEvent.getChannel().sendMessage(answerListener.getMessageAuthor().getName() + " took the kids. Can I at least see them at Christmas?");

                                                        Optional<Upgrades> upgrades = upgradesRepository.findUpgradesByUserid(answerListener.getMessageAuthor().getIdAsString());
                                                        maxChilds.addAndGet(upgrades.get().getMaxchildsupgrade() - 1);

                                                        if (childRepository.findChildsByUserid(answerListener.getMessageAuthor().getIdAsString()).size() < maxChilds.get()) {
                                                            messageCreateEvent.getChannel().sendMessage("Hoe mag het strontjong gaan heten?");
                                                            messageCreateEvent.getChannel().addMessageCreateListener(childNameListener -> {
                                                                if (childNameListener.getMessageAuthor().getId() == answerListener.getMessageAuthor().getId() && !childCreated.get()) {
                                                                    childCreated.set(true);
                                                                    Child child;
                                                                    String name = childNameListener.getMessageContent();
                                                                    if (name.length() > 40) {
                                                                        name = name.substring(0, 39);
                                                                    }
                                                                    int miningSpeed = r.nextInt(10);
                                                                    int superChance = r.nextInt(10);
                                                                    int healthPoints = r.nextInt(100);
                                                                    if (superChance == 2) {
                                                                        healthPoints *= 5;
                                                                    }
                                                                    String user_id = childNameListener.getMessageAuthor().getIdAsString();
                                                                    child = childService.createChild(name, miningSpeed, healthPoints, user_id);

                                                                    messagingService.sendMessage(childNameListener.getMessageAuthor(),
                                                                            "Holy shit " + name + " is geboren.",
                                                                            name + " heeft een mining speed van " + miningSpeed + " en " + healthPoints + " hitpoints.",
                                                                            null,
                                                                            "https://c.tenor.com/pY0cFgRIs4wAAAAC/jip-baby.gif",
                                                                            childNameListener.getChannel());
                                                                }
                                                            });
                                                        } else {
                                                            Optional<User> userOptGiveBedrock = userRepository.findUserById(answerListener.getMessageAuthor().getIdAsString());
                                                            if (userOptGiveBedrock.isPresent()) {
                                                                userOptGiveBedrock.get().setBedrock(userOptGiveBedrock.get().getBedrock() + loserBedrockGiven);
                                                                User user = userRepository.save(userOptGiveBedrock.get());
                                                                messageCreateEvent.getChannel().sendMessage("Je had al " + maxChilds + " kinderen, dit is het maximum.\nJe hebt dus het kind verkocht voor "
                                                                        + loserBedrockGiven + " bedrock.\nJe hebt nu " + user.getBedrock() + " bedrock.");
                                                            }
                                                        }
                                                    }
                                                });
                                            } else if ((listener.getEmoji().equalsEmoji("\uD83D\uDC4E") && listener.getUser().get().getId() == Long.parseLong(finalUserID))
                                                        || (listener.getEmoji().equalsEmoji("\u274C") && listener.getUserId() == messageCreateEvent.getMessageAuthor().getId())
                                                        && !thumbsUp.get()) {
                                                message.edit(new EmbedBuilder()
                                                        .setTitle("Jammer dan")
                                                        .setDescription("Geen neukseks for you."));
                                                thumbsDown.set(true);
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
                } else {
                    long difference = cooldown.get().getNeuksekscooldown() - System.nanoTime();
                    messageCreateEvent.getChannel().sendMessage("Bro rustig man bro, je moet nog " + Tools.getReadableTime(difference) + " wachten.");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
