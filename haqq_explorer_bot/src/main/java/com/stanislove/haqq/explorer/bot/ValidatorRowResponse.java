package com.stanislove.haqq.explorer.bot;

public class ValidatorRowResponse {
  private final String moniker;
  private final String tokens;

  public ValidatorRowResponse(String moniker, String tokens) {
    this.moniker = moniker;
    this.tokens = tokens;
  }

  @Override
  public String toString() {
    return String.format("Moniker: %s. Delegation %s ISLM", moniker, tokens);
  }
}