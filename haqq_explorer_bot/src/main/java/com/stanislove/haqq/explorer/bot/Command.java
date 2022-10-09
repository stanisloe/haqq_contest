package com.stanislove.haqq.explorer.bot;

public enum Command {
  SHOW_ACTIVE_VALIDATORS("Show Active Validators", "show_active_validators"),
  SHOW_TX_INFO("Show tx info", "show_tx_info"),
  SHOW_VALIDATOR_INFO("Show validator info", "show_validator_info");

  private final String text;
  private final String commandData;

  Command(String text, String commandData) {
    this.text = text;
    this.commandData = commandData;
  }

  public String getText() {
    return text;
  }

  public String getCommandData() {
    return commandData;
  }
}
