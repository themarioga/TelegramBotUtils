package org.themarioga.bot.service.intf;

import org.themarioga.bot.util.CallbackQueryHandler;
import org.themarioga.bot.util.CommandHandler;

import java.util.Map;

public interface ApplicationService {

	Map<String, CommandHandler> getBotCommands();

	Map<String, CallbackQueryHandler> getCallbackQueries();

}
