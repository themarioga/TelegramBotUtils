package org.themarioga.game.service.intf;

import org.themarioga.game.model.CallbackQueryHandler;
import org.themarioga.game.model.CommandHandler;

import java.util.Map;

public interface ApplicationService {

	Map<String, CommandHandler> getBotCommands();

	Map<String, CallbackQueryHandler> getCallbackQueries();

}
