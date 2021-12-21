package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.exceptions.UserExistsException;
import com.cakedevs.ChildLabourBot.listeners.UserListener;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import com.cakedevs.ChildLabourBot.services.UserService;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserListenerImpl implements UserListener {
    @Autowired
    private MessagingService messagingService;
    @Autowired
    private UserService userService;
    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageContent().startsWith("!user")) {
            messageCreateEvent.getChannel().type();
            if(messageCreateEvent.getMessageContent().equalsIgnoreCase("!user create")) {
                // Create the user
                User user;
                try {
                    user = userService.createUser(messageCreateEvent.getMessageAuthor().getIdAsString(), messageCreateEvent.getMessageAuthor().getDisplayName());
                } catch (UserExistsException e) {
                    messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                            "User Creator",
                            e.getMessage(),
                            null,
                            null,
                            messageCreateEvent.getChannel());
                    return;
                }

                //If successful send message
                messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                        "User Creator",
                        "User has successfully been created for " + user.getName(),
                        null,
                        null,
                        messageCreateEvent.getChannel());
            } else {
                messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                        "User Creator",
                        "POEPENNNNN",
                        null,
                        null,
                        messageCreateEvent.getChannel());
            }
        }
    }
}
