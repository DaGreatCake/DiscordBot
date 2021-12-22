package com.cakedevs.ChildLabourBot.listeners.impl;

import com.cakedevs.ChildLabourBot.listeners.HelpListener;

import com.cakedevs.ChildLabourBot.services.MessagingService;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HelpListenerImpl implements HelpListener {
    @Autowired
    private MessagingService messagingService;

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageContent().equalsIgnoreCase("+help")) {
            messagingService.sendMessage(messageCreateEvent.getMessageAuthor(),
                    "Alle commands",
                    "+arbeiten - Laat je kinderen werken. \n" +
                            "+bal - Check hoeveel moneys je hebt. \n" +
                            "+neukseks - Maak een kind. \n" +
                            "+murder - Doe een poging tot moord.",
                    null,
                    null,
                    messageCreateEvent.getChannel());
        }
    }
}
