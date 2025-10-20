package org.themarioga.game.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.themarioga.game.service.intf.BotMessageService;
import org.themarioga.game.service.intf.BotService;

public class BotMessageServiceImpl implements BotMessageService {

	private static final Logger logger = LoggerFactory.getLogger(BotMessageServiceImpl.class);

	private final BotService botService;

	public BotMessageServiceImpl(BotService botService) {
		this.botService = botService;
	}

	@Override
	public void sendMessage(long chatId, String text) {
		try {
			SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
			sendMessage.enableHtml(true);
			botService.getTelegramClient().execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendMessage(long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
		try {
			SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
			sendMessage.enableHtml(true);
			sendMessage.setReplyMarkup(inlineKeyboardMarkup);
			botService.getTelegramClient().execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendMessageAsync(long chatId, String text, Callback callback) {
		try {
			SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
			sendMessage.enableHtml(true);
			botService.getTelegramClient()
				.executeAsync(sendMessage)
					.thenAccept(callback::success)
					.exceptionally(throwable -> {
						callback.failure(throwable);
						return null;
					});
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void editMessage(long chatId, int messageId, String text) {
		try {
			EditMessageText editMessageText = EditMessageText.builder()
					.chatId(chatId)
					.messageId(messageId)
					.text(text)
					.parseMode("HTML")
					.build();
			botService.getTelegramClient().execute(editMessageText);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
		try {
			EditMessageText editMessageText = EditMessageText.builder()
					.chatId(chatId)
					.messageId(messageId)
					.text(text)
					.parseMode("HTML")
					.replyMarkup(inlineKeyboardMarkup)
					.build();
			botService.getTelegramClient().execute(editMessageText);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void deleteMessage(long chatId, int messageId) {
		try {
			botService.getTelegramClient().execute(new DeleteMessage(String.valueOf(chatId), messageId));
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void answerCallbackQuery(String callbackQueryId) {
		try {
			botService.getTelegramClient().execute(new AnswerCallbackQuery(callbackQueryId));
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void answerCallbackQuery(String callbackQueryId, String text) {
		try {
			AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQueryId);
			answerCallbackQuery.setText(text);
			botService.getTelegramClient().execute(answerCallbackQuery);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String sanitizeTextFromCommand(String command, String text) {
		return text.replace(command, "").replace("@" + botService.getBotName(), "").trim();
	}

}
