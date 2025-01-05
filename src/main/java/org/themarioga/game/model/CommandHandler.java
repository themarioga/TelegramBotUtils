package org.themarioga.game.model;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface CommandHandler {

    void callback(Message message, String params);

}
