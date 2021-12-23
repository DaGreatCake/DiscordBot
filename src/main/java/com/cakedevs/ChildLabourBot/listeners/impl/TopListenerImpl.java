package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.comparators.UserStatusComparator;
import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.listeners.TopListener;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class TopListenerImpl implements TopListener {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MessagingService messagingService;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+top")) {
            Optional<User> userOptPrimary = userRepository.findUserById(messageCreateEvent.getMessageAuthor().getIdAsString());
            if (userOptPrimary.isPresent()) {
                List<User> userList = entityManager.createQuery("SELECT e FROM user e").getResultList();
                Collections.sort(userList, new UserStatusComparator());

                String topList = "";
                for (int i = 1; i <= 5; i++) {
                    topList += i + ". " + userList.get(userList.size() - i).getName() + ", status: " +  userList.get(userList.size() - i).getStatus() + "\n";
                }

                messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                        "De allerleipste mensen:",
                        topList,
                        null,
                        null,
                        messageCreateEvent.getChannel());
            } else {
                messageCreateEvent.getChannel().sendMessage("Je hebt nog geen account, doe eerst +start");
            }
        }
    }
}
