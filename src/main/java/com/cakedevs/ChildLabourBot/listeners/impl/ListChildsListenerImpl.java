package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.ListChildsListener;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class ListChildsListenerImpl implements ListChildsListener {
    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    private HashMap<String, Instant> cooldowns = new HashMap<String, Instant>();

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+listchilds")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                List<Child> userChilds = childRepository.findChildsByUserid(messageCreateEvent.getMessageAuthor().getIdAsString());
                if (userChilds.size() != 0) {
                    String childChoose = "";

                    for (Child child : userChilds) {
                        childChoose += "Id: " + child.getId() + ", name: " + child.getName() + ", mining speed: "
                                + child.getMiningspeed() + ", hitpoints: " + child.getHealthpoints() + ", max hitpoints: " + child.getHealthpointsmax() + "\n";
                    }

                    messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                            "Al je kinderen:",
                            childChoose,
                            null,
                            null,
                            messageCreateEvent.getChannel());
                } else {
                    messageCreateEvent.getChannel().sendMessage("Je hebt geen kinderen.\nDoe eerst +neukseks");
                }
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
