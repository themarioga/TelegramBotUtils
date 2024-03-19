package org.themarioga.bot.service.intf;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface BotService {

	<T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void executeAsync(Method method, Callback callback) throws TelegramApiException;

	<T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> executeAsync(Method method) throws TelegramApiException;

	<T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException;

	void setPendingReply(Long userId, String command);

	String getBotUsername();

}
