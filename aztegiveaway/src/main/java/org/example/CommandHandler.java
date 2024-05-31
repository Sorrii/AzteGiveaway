package org.example;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    public static void handleCommand(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
            LOGGER.info("Responded to ping command.");
        }
    }
}
