package org.themarioga.bot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.themarioga.bot.service.impl.LongPollingBotServiceImpl;
import org.themarioga.bot.service.impl.WebhookBotServiceImpl;
import org.themarioga.bot.service.intf.ApplicationService;

import java.io.File;

public class BotUtils {

    private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

    private BotUtils() {
        throw new UnsupportedOperationException();
    }

    public static LongPollingBotServiceImpl createLongPollingBot(Boolean enabled, String token, String name, TelegramBotsApi telegramBotsApi, ApplicationService applicationService) {
        logger.info("Iniciando bot longpolling...");

        LongPollingBotServiceImpl longPollingBotService = new LongPollingBotServiceImpl(token, name, applicationService);

        if (Boolean.TRUE.equals(enabled)) {
            try {
                telegramBotsApi.registerBot(longPollingBotService);
            } catch (TelegramApiException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return longPollingBotService;
    }

    public static WebhookBotServiceImpl createWebhookBot(Boolean enabled, String token, String name, String path, String webhookURL, String webhookCertPath, TelegramBotsApi telegramBotsApi, ApplicationService applicationService) {
        logger.info("Iniciando bot webhook en la url {}...", webhookURL);

        WebhookBotServiceImpl webhookBotService = new WebhookBotServiceImpl(token, name, path, applicationService);

        if (Boolean.TRUE.equals(enabled)) {
            SetWebhook.SetWebhookBuilder webhookBuilder = SetWebhook.builder().url(webhookURL);

            InputFile certificate = BotUtils.getCertificate(webhookCertPath);
            if (certificate != null) {
                logger.info("Sending certificate {}...", webhookCertPath);
                webhookBuilder.certificate(certificate);
            }

            try {
                telegramBotsApi.registerBot(webhookBotService, webhookBuilder.build());
            } catch (TelegramApiException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return webhookBotService;
    }

    public static String getUserInfo(User user) {
        String output = String.valueOf(user.getId());
        if (StringUtils.hasText(user.getFirstName()) || StringUtils.hasText(user.getLastName()) || StringUtils.hasText(user.getUserName())) {
            output += " [" + getUsername(user) + "]";
        }

        return output;
    }

    public static String getUsername(User user) {
        String output = "";
        if (StringUtils.hasText(user.getFirstName())) output += user.getFirstName();
        if (StringUtils.hasText(user.getLastName())) output += " " + user.getLastName();
        if (StringUtils.hasText(user.getUserName())) output += " (@" + user.getUserName() + ")";
        return output;
    }

    public static String getUsername(Chat chat) {
        String output = "";
        if (StringUtils.hasText(chat.getFirstName())) output += chat.getFirstName();
        if (StringUtils.hasText(chat.getLastName())) output += " " + chat.getLastName();
        if (StringUtils.hasText(chat.getUserName())) output += " (@" + chat.getUserName() + ")";
        return output;
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
