package org.themarioga.bot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.themarioga.bot.service.impl.LongPollingBotServiceImpl;
import org.themarioga.bot.service.impl.WebhookBotServiceImpl;
import org.themarioga.bot.service.intf.ApplicationService;

import java.io.File;

public class BotCreationUtils {

	private static final Logger logger = LoggerFactory.getLogger(BotCreationUtils.class);

	private BotCreationUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static LongPollingBotServiceImpl createLongPollingBot(Boolean enabled, String token, String name, TelegramBotsApi telegramBotsApi, ApplicationService applicationService) {
		logger.info("Iniciando {} longpolling...", name);

		LongPollingBotServiceImpl longPollingBotService = new LongPollingBotServiceImpl(token, name, applicationService);

		if (Boolean.TRUE.equals(enabled)) {
			try {
				telegramBotsApi.registerBot(longPollingBotService);

				logger.info("{} iniciado", name);
			} catch (TelegramApiException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return longPollingBotService;
	}

	public static WebhookBotServiceImpl createWebhookBot(Boolean enabled, String token, String name, String path, String webhookURL, String webhookCertPath, TelegramBotsApi telegramBotsApi, ApplicationService applicationService) {
		logger.info("Iniciando {} webhook en la url {}...", name, webhookURL);

		WebhookBotServiceImpl webhookBotService = new WebhookBotServiceImpl(token, name, path, applicationService);

		if (Boolean.TRUE.equals(enabled)) {
			SetWebhook.SetWebhookBuilder webhookBuilder = SetWebhook.builder().url(webhookURL);

			InputFile certificate = BotCreationUtils.getCertificate(webhookCertPath);
			if (certificate != null) {
				logger.info("Sending certificate {}...", webhookCertPath);
				webhookBuilder.certificate(certificate);
			}

			try {
				telegramBotsApi.registerBot(webhookBotService, webhookBuilder.build());

				logger.info("{} iniciado", name);
			} catch (TelegramApiException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return webhookBotService;
	}

	public static InputFile getCertificate(String path) {
		InputFile certificate = null;
		File file = new File(path);
		if (file.exists() && file.canRead()) {
			certificate = new InputFile(file);
		}
		return certificate;
	}

}
