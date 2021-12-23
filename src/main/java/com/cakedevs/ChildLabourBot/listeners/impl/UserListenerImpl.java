package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.entities.Upgrades;
import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.exceptions.UserExistsException;
import com.cakedevs.ChildLabourBot.listeners.UserListener;
import com.cakedevs.ChildLabourBot.services.MessagingService;
import com.cakedevs.ChildLabourBot.services.UpgradesService;
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

    @Autowired
    private UpgradesService upgradesService;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+start")) {
            // Create the user
            User user;
            Upgrades upgrades;
            try {
                user = userService.createUser(messageCreateEvent.getMessageAuthor().getIdAsString(), messageCreateEvent.getMessageAuthor().getDisplayName());
                upgrades = upgradesService.createUpgrades(messageCreateEvent.getMessageAuthor().getIdAsString());
            } catch (UserExistsException e) {
                messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                        "Wat de frick man, je hebt dit al gedaan.",
                        e.getMessage(),
                        null,
                        null,
                        messageCreateEvent.getChannel());
                return;
            }

            //If successful send a message about the success
            messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                    "Child Labour Initiate",
                    "Lekker man " + user.getName() + ". Nu kun je enge doekoes verdienen.",
                    "Typ +help voor de commands.",
                    "https://static.wikia.nocookie.net/clashofclans/images/5/56/Miner_info.png",
                    messageCreateEvent.getChannel());
        }
    }
}
