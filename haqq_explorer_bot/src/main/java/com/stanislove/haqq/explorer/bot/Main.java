package com.stanislove.haqq.explorer.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {



  public static void main(String[] args) {

    try {
      TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
      telegramBotsApi.registerBot(new HaqqExplorerHandler(
          "5676473997:AAF_BU_IN25ZnInXmGfjbTWUxYtsWdtHN5M", "HaqqExplorerBot",
          "https://haqq-t.api.manticore.team", "https://haqq-t.rpc.manticore.team"
      ));
    } catch (Exception e) {
      System.out.println("Failed to register bot" + e);
    }
  }

}
