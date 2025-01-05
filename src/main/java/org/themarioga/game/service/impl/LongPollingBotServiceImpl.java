package org.themarioga.game.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.themarioga.game.constants.BotResponseErrorI18n;
import org.themarioga.game.model.CallbackQuery;
import org.themarioga.game.model.CallbackQueryHandler;
import org.themarioga.game.model.Command;
import org.themarioga.game.model.CommandHandler;
import org.themarioga.game.service.intf.ApplicationService;
import org.themarioga.game.service.intf.BotService;

import org.themarioga.game.util.BotMessageUtils;

import java.util.HashMap;
import java.util.Map;

public class LongPollingBotServiceImpl implements BotService, SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingBotServiceImpl.class);

	private final String botToken;
    private final String botName;

	private final TelegramClient telegramClient;

	private final Map<String, CommandHandler> commands;
    private final Map<String, CallbackQueryHandler> callbackQueries;
	private final Map<Long, String> pendingReplies = new HashMap<>();

    public LongPollingBotServiceImpl(String botToken, String botName, ApplicationService applicationService) {
	    logger.info("Iniciando {} como longpolling...", botName);

		this.botToken = botToken;
        this.botName = botName;

	    commands = applicationService.getBotCommands();
        callbackQueries = applicationService.getCallbackQueries();

	    telegramClient = new OkHttpTelegramClient(botToken);
    }

	@Override
	public void consume(Update update) {
		if (update.hasMessage()) {
			String receivedCommand = BotMessageUtils.getReceivedCommand(botName, update.getMessage(), pendingReplies);

			if (receivedCommand != null && !receivedCommand.isBlank()) {
				Command command = BotMessageUtils.getCommandFromMessage(receivedCommand);
				CommandHandler commandHandler = commands.get(command.getCommand());
				if (commandHandler != null) {
					commandHandler.callback(update.getMessage(), command.getCommandData());
				} else {
					logger.error("Comando desconocido {} enviado por {}",
							update.getMessage().getText(),
							BotMessageUtils.getUserInfo(update.getMessage().getFrom()));

					try {
						telegramClient.execute(new SendMessage(String.valueOf(update.getMessage().getChatId()), BotResponseErrorI18n.COMMAND_DOES_NOT_EXISTS));
					} catch (TelegramApiException e) {
						logger.error("Error al enviar mensaje {}", e.getMessage(), e);
					}
				}
			}
		} else if (update.hasCallbackQuery()) {
			CallbackQuery callbackQuery = BotMessageUtils.getCallbackQueryFromMessageQuery(update.getCallbackQuery().getData());

			CallbackQueryHandler callbackQueryHandler = callbackQueries.get(callbackQuery.getQuery());
			if (callbackQueryHandler != null) {
				callbackQueryHandler.callback(update.getCallbackQuery(), callbackQuery.getQueryData());
			} else {
				logger.error("Querie desconocida {} enviado por {}",
						update.getCallbackQuery().getData(),
						BotMessageUtils.getUserInfo(update.getCallbackQuery().getFrom()));

				try {
					AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
					answerCallbackQuery.setText(BotResponseErrorI18n.COMMAND_DOES_NOT_EXISTS);
					telegramClient.execute(answerCallbackQuery);
				} catch (TelegramApiException e) {
					logger.error("Error al enviar mensaje {}", e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public LongPollingUpdateConsumer getUpdatesConsumer() {
		return this;
	}

	@Override
	public void setPendingReply(Long userId, String command) {
		if (pendingReplies.containsKey(userId))
			throw new UnsupportedOperationException();

		pendingReplies.put(userId, command);
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
    public String getBotName() {
        return botName;
    }

	@Override
	public Map<String, CallbackQueryHandler> getCallbackQueries() {
		return callbackQueries;
	}

	@Override
	public Map<String, CommandHandler> getCommands() {
		return commands;
	}

	@Override
	public Map<Long, String> getPendingReplies() {
		return pendingReplies;
	}

	@Override
	public TelegramClient getTelegramClient() {
		return telegramClient;
	}

	@Override
	public SpringLongPollingBot getBean() {
		return this;
	}

}
