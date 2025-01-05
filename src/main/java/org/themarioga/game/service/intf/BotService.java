package org.themarioga.game.service.intf;


import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import org.themarioga.game.model.CallbackQueryHandler;
import org.themarioga.game.model.CommandHandler;

import java.util.Map;

public interface BotService {

	void setPendingReply(Long userId, String command);

	String getBotToken();

	String getBotName();

	Map<String, CallbackQueryHandler> getCallbackQueries();

	Map<String, CommandHandler> getCommands();

	Map<Long, String> getPendingReplies();

	TelegramClient getTelegramClient();

	Object getBean();

}
