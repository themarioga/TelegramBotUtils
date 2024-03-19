package org.themarioga.bot.service.intf;

import org.themarioga.bot.model.CallbackQueryHandler;
import org.themarioga.bot.model.CommandHandler;

import java.util.Map;

public interface ApplicationService {

	Map<String, CommandHandler> getBotCommands();

	Map<String, CallbackQueryHandler> getCallbackQueries();

}
