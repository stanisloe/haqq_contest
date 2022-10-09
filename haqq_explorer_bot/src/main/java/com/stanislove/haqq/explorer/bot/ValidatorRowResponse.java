package com.stanislove.haqq.explorer.bot;

public class ValidatorRowResponse {
  private final NameAndValue validatorAddress;
  private final NameAndValue tokens;

  public ValidatorRowResponse(NameAndValue validatorAddress, NameAndValue tokens) {
    this.validatorAddress = validatorAddress;
    this.tokens = tokens;
  }

  public NameAndValue getValidatorAddress() {
    return validatorAddress;
  }

  public NameAndValue getTokens() {
    return tokens;
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %s\n%s: %s", validatorAddress.getName(), validatorAddress.getValue(),
        tokens.getName(), tokens.getValue()
    );
  }
}
