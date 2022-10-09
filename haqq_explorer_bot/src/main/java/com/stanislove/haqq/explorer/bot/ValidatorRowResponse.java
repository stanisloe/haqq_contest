package com.stanislove.haqq.explorer.bot;

public class ValidatorRowResponse {
  private final NameAndValue moniker;
  private final NameAndValue tokens;

  public ValidatorRowResponse(NameAndValue moniker, NameAndValue tokens) {
    this.moniker = moniker;
    this.tokens = tokens;
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %s\n %s: %s", moniker.getName(), moniker.getValue(),
        tokens.getName(), tokens.getValue()
    );
  }
}
