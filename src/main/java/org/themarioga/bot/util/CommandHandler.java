package org.themarioga.bot.util;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {

    void callback(Message message, String params);

}
