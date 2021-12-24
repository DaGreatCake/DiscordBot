package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.Cooldown;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.ArbeitenListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.CooldownRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.tools.Tools;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ArbeitenListenerImpl implements ArbeitenListener {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private CooldownRepository cooldownRepository;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // constraints
        int delayInMinutes = 10;

        AtomicBoolean done = new AtomicBoolean(false);
        int bedrockMined = 0;

        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+arbeiten")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                Optional<Cooldown> cooldown = cooldownRepository.findCooldownByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                if (System.nanoTime() > cooldown.get().getArbeitencooldown()) {
                    List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                    if (userChilds.size() != 0 && !done.get()) {
                        cooldown.get().setArbeitencooldown(System.nanoTime() + (delayInMinutes * 60000000000L));
                        cooldownRepository.save(cooldown.get());
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
                    long difference = cooldown.get().getArbeitencooldown() - System.nanoTime();
                    messageCreateEvent.getChannel().sendMessage("Bro rustig man bro, je moet nog " + Tools.getReadableTime(difference) + " wachten.");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
