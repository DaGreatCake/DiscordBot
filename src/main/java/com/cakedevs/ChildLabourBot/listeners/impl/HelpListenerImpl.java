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
                            "+buy - Koop een upgrade of status. \n" +
                            "+heal - Heal een kind tot full hitpoints. \n" +
                            "+listchilds - Laat al je kinderen zien. \n" +
                            "+merge - Merge 2 kinderen voor 150% van hun hp. \n" +
                            "+murder - Doe een poging tot moord.\n" +
                            "+neukseks - Maak een kind.\n" +
                            "+status - Laat je eigen status zien.\n" +
                            "+top - Laat de status leaderboard zien.",
                    null,
                    null,
                    messageCreateEvent.getChannel());
        }
    }
}
