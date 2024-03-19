# TelegramBotUtils
Utilidades para la creación de bots de telegram

Para utilizarlo debemos crear una clase de configuración como la siguiente:

    @Configuration
    public class ExampleBotConfig {

        @Value("${example.bot.enabled}")
        private Boolean enabled;
    
        @Value("${example.bot.token}")
        private String token;
    
        @Value("${example.bot.name}")
        private String name;
    
        @Value("${example.bot.path}")
        private String path;
    
        @Value("${example.bot.webhook.url}")
        private String webhookURL;
    
        @Value("${example.bot.webhook.cert.path}")
        private String webhookCertPath;
    
        // Long polling instantiation
    
        @Bean("exampleBot")
        @DependsOn({"telegramBotsApiLongPolling", "exampleBotApplicationService"})
        @ConditionalOnProperty(prefix = "example.bot", name="type", havingValue = "longpolling")
        public LongPollingBotServiceImpl longPollingBotService(
                @Qualifier("telegramBotsApiLongPolling") TelegramBotsApi telegramBotsApi,
                @Qualifier("exampleBotApplicationService") ApplicationService applicationService) {
            return BotCreationUtils.createLongPollingBot(enabled, token, name, telegramBotsApi, applicationService);
        }
    
        // Webhook instantiation
    
        @Bean("exampleBot")
        @DependsOn({"telegramBotsApiWebhook", "exampleBotApplicationService"})
        @ConditionalOnProperty(prefix = "example.bot", name="type", havingValue = "webhook")
        public WebhookBotServiceImpl webhookBotService(
                @Qualifier("telegramBotsApiWebhook") TelegramBotsApi telegramBotsApi,
                @Qualifier("exampleBotApplicationService") ApplicationService applicationService) {
            return BotCreationUtils.createWebhookBot(enabled, token, name, path, webhookURL, webhookCertPath, telegramBotsApi, applicationService);
        }
    
    }

Y además debemos implementar la clase ApplicationService de una forma como la siguiente:

    @Service("exampleBotApplicationService")
    public class ApplicationServiceImpl implements ApplicationService {
    
        private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    
        private BotMessageService dictionariesBotMessageService;
    
        @Override
        public Map<String, CommandHandler> getBotCommands() {
            Map<String, CommandHandler> commands = new HashMap<>();
    
            commands.put("/start", (message, data) -> {
                if (!message.getChat().getType().equals(BotConstants.TELEGRAM_MESSAGE_TYPE_PRIVATE)) {
                    logger.error("Comando /start enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));
    
                    dictionariesBotMessageService.sendMessage(message.getChat().getId(), BotResponseErrorI18n.COMMAND_SHOULD_BE_ON_PRIVATE);
    
                    return;
                }
    
                try {
                    // Register user
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
    
            commands.put("/menu", (message, data) -> {
                if (message.getChat().getType().equals(BotConstants.TELEGRAM_MESSAGE_TYPE_PRIVATE)) {
                    logger.error("Comando /create enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));
    
                    dictionariesBotMessageService.sendMessage(message.getChat().getId(), BotResponseErrorI18n.COMMAND_SHOULD_BE_ON_GROUP);
    
                    return;
                }
    
                try {
                    // Show menu
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
    
            commands.put("/help", (message, data) -> {
                // Show help
            });
    
            return commands;
        }
    
        @Override
        public Map<String, CallbackQueryHandler> getCallbackQueries() {
            Map<String, CallbackQueryHandler> callbackQueryHandlerMap = new HashMap<>();
    
            callbackQueryHandlerMap.put("example_query", (callbackQuery, data) -> {
                try {
                    // Answer query

                    return;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
    
                dictionariesBotMessageService.answerCallbackQuery(callbackQuery.getId());
            });
    
            return callbackQueryHandlerMap;
        }
    
        @Autowired
        public void setBotMessageService(BotMessageService dictionariesBotMessageService) {
            this.dictionariesBotMessageService = dictionariesBotMessageService;
        }

    }

Además tenemos que tener un fichero de configuración como el siguiente:

    spring.profiles.active=@spring.profiles.active@
    
    server.port=${SERVER_PORT}
    
    spring.main.allow-circular-references=true
    
    telegram.bot.type=longpolling
    
    example.bot.enabled=${EXAMPLE_BOT_ENABLED}
    example.bot.token=${EXAMPLE_BOT_TOKEN}
    example.bot.name=${EXAMPLE_BOT_NAME}
    example.bot.path=${EXAMPLE_BOT_PATH}
    example.bot.webhook.url=${EXAMPLE_BOT_WEBHOOK_URL}
    example.bot.webhook.cert.path=${EXAMPLE_BOT_WEBHOOK_CERT_PATH}

O en caso de que queramos webhooks

    telegram.bot.type=webhook
    server.ssl.enabled=true
    server.ssl.key-store=${SERVER_KEYSTORE_FILE}
    server.ssl.key-store-password=${SERVER_KEYSTORE_PASSWORD}
    server.ssl.key-alias=${SERVER_KEYSTORE_ALIAS}
    server.ssl.key-store-type=PKCS12
    
    lets-encrypt-helper.domain=${LETS_ENCRYPT_DOMAIN}
    lets-encrypt-helper.contact=mailto:${LETS_ENCRYPT_EMAIL}