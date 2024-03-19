package org.themarioga.bot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.themarioga.bot.constants.BotResponseErrorI18n;
import org.themarioga.bot.model.CallbackQuery;
import org.themarioga.bot.model.CallbackQueryHandler;
import org.themarioga.bot.model.Command;
import org.themarioga.bot.model.CommandHandler;
import org.themarioga.bot.service.intf.ApplicationService;
import org.themarioga.bot.service.intf.BotService;
import org.themarioga.bot.util.BotMessageUtils;

import java.util.HashMap;
import java.util.Map;

public class WebhookBotServiceImpl extends TelegramWebhookBot implements BotService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookBotServiceImpl.class);

    private final String botName;
    private final String botPath;

	private final Map<String, CommandHandler> commands;
    private final Map<String, CallbackQueryHandler> callbackQueries;
    private final Map<Long, String> pendingReplies = new HashMap<>();

    public WebhookBotServiceImpl(String token, String botName, String botPath, ApplicationService applicationService) {
        super(token);

        this.botName = botName;
        this.botPath = botPath;

	    commands = applicationService.getBotCommands();
        callbackQueries = applicationService.getCallbackQueries();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
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
                        execute(new SendMessage(String.valueOf(update.getMessage().getChatId()), BotResponseErrorI18n.COMMAND_DOES_NOT_EXISTS));
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
                    execute(answerCallbackQuery);
                } catch (TelegramApiException e) {
                    logger.error("Error al enviar mensaje {}", e.getMessage(), e);
                }
            }
        }

        return null;
    }

    @Override
    public void setPendingReply(Long userId, String command) {
        if (pendingReplies.containsKey(userId))
            throw new UnsupportedOperationException();

        pendingReplies.put(userId, command);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
