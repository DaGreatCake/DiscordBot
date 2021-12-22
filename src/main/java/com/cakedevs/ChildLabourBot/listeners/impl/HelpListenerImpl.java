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
            messagingService.sendMessage(null,
                    "Alle commands",
                    "+neukseks - Maak een kind. \n" +
                              "+arbeiten - Laat je kinderen werken. \n" +
                              "+bal - Check hoeveel moneys je hebt. \n",
                    null,
                    null,
                    messageCreateEvent.getChannel());
        }
    }
}
