package com.stanislove.haqq.explorer.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    try {
      LOGGER.info("Env variables {}", System.getenv());
      String botToken = System.getenv().get("BOT_TOKEN");
      String botName = System.getenv().get("BOT_NAME");
      String apiHost = System.getenv().get("API_HOST");
      String rpcHost = System.getenv().get("RPC_HOST");
      int creatorId = Integer.parseInt(System.getenv().get("CREATOR_ID"));
      TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
      telegramBotsApi.registerBot(new HaqqExplorerHandler(botToken, botName, apiHost, rpcHost,
          creatorId
      ));
    } catch (Exception e) {
      LOGGER.error("Failed to register bot", e);
    }
  }

}
