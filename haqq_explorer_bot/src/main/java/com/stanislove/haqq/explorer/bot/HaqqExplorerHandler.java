package com.stanislove.haqq.explorer.bot;

import static com.stanislove.haqq.explorer.bot.Command.SHOW_ACTIVE_VALIDATORS;
import static com.stanislove.haqq.explorer.bot.Command.SHOW_TX_INFO;
import static com.stanislove.haqq.explorer.bot.Command.SHOW_VALIDATOR_INFO;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.InlineKeyboardMarkupBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class HaqqExplorerHandler extends AbilityBot {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final InlineKeyboardMarkup MAIN_MENU_KEYBOARD = Arrays
      .stream(Command.values())
      .map(c ->
          InlineKeyboardButton
              .builder()
              .text(c.getText())
              .callbackData(c.getCommandData())
              .build()
      )
      .map(Collections::singletonList)
      .collect(
          InlineKeyboardMarkup::builder,
          InlineKeyboardMarkupBuilder::keyboardRow,
          (a, b) -> {
          }
      )
      .build();


  private static final BigInteger ISLM_DENOM = new BigInteger("1000000000000000000");

  private final String apiHost;
  private final String rpcHost;
  private final int creatorId;
  private final HttpClient client;

  public HaqqExplorerHandler(String botToken, String botUsername, String apiHost,
      String rpcHost, int creatorId) {
    super(botToken, botUsername, MapDBContext.offlineInstance(botUsername));
    this.apiHost = apiHost;
    this.rpcHost = rpcHost;
    this.creatorId = creatorId;
    this.client = HttpClients.createDefault();
  }

  public Reply mainMenu() {
    return Reply
        .of(
            (bot, u) -> sendMainMenuMessage(u),
            u -> Objects.nonNull(u.getMessage()) && "/start".equals(u.getMessage().getText())
        )
        .enableStats("Main menu");
  }

  public Reply showActiveValidators() {
    return Reply
        .of(
            (bot, u) -> {
              String url = apiHost + "/staking/validators?status=BOND_STATUS_BONDED";
              HttpGet request = new HttpGet(url);
              JsonNode response = httpRequest(request);
              List<ValidatorRowResponse> validators = Lists
                  .newArrayList(response.get("result").elements())
                  .stream()
                  .map(n -> {
                    String moniker = n.get("description").get("moniker").asText();
                    String tokens = new BigInteger(n.get("tokens").asText())
                        .divide(ISLM_DENOM)
                        .toString();
                    return new ValidatorRowResponse(
                        new NameAndValue("Moniker", moniker),
                        new NameAndValue("Delegation, ISLM", tokens)
                    );
                  })
                  .sorted((v1, v2) ->
                      new BigInteger(v2.getTokens().getValue())
                          .compareTo(new BigInteger(v1.getTokens().getValue()))
                  )
                  .collect(Collectors.toList());

              Lists.partition(validators, 50).forEach(batch -> {
                String message = batch
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("\n\n"));
                silent.send(message, getChatId(u));
              });

              sendMainMenuMessage(u);
            },
            u -> isUpdateFor(u, SHOW_ACTIVE_VALIDATORS)
        )
        .enableStats("Show Active validators");
  }

  public Reply showTxInfo() {
    return ReplyFlow
        .builder(db)
        .action((b, u) -> {
          silent.send("Enter tx hash", getChatId(u));
        })
        .onlyIf(u -> isUpdateFor(u, SHOW_TX_INFO))
        .next(Reply
            .of((b, u) -> {
              String txHash = u.getMessage().getText();
              String url = apiHost + "/cosmos/tx/v1beta1/txs/" + txHash;
              HttpGet request = new HttpGet(url);
              JsonNode response = httpRequest(request);
              final String message;
              if (response.has("code")) {
                message = "tx hash not found";
              } else {
                JsonNode messages = response.get("tx").get("body").get("messages");
                message = Lists
                    .newArrayList(messages)
                    .stream()
                    .map(m -> Lists
                        .newArrayList(m.fieldNames())
                        .stream()
                        .map(p -> p + " -> " + m.get(p).asText())
                        .collect(Collectors.joining("\n"))
                    )
                    .collect(Collectors.joining());
              }
              silent.send(message, getChatId(u));
              sendMainMenuMessage(u);
            })
            .enableStats("Show Tx Info")
        )
        .build();
  }

  public Reply showValidatorInfo() {
    return ReplyFlow
        .builder(db)
        .action((b, u) -> {
          silent.send("Enter valoper address", getChatId(u));
        })
        .onlyIf(u -> isUpdateFor(u, SHOW_VALIDATOR_INFO))
        .next(Reply
            .of((b, u) -> {
              String valoperAddress = u.getMessage().getText();
              String url = apiHost + "/staking/validators/" + valoperAddress;
              HttpGet request = new HttpGet(url);
              JsonNode response = httpRequest(request);
              final String message;
              if (response.has("error")) {
                message = "validator not found";
              } else {
                JsonNode result = response.get("result");
                JsonNode description = result.get("description");
                JsonNode commissionRates = result.get("commission").get("commission_rates");
                List<NameAndValue> lines = new ArrayList<>();
                String operatorAddress = result.get("operator_address").asText();
                String moniker = description.get("moniker").asText();
                String tokens = result.get("tokens").asText();
                String details = description.get("details").asText();
                String website = description.get("website").asText();
                ValidatorStatus status = "3".equals(result.get("status").asText())
                    ? ValidatorStatus.ACTIVE
                    : ValidatorStatus.INACTIVE;
                String isJailed = Boolean.toString(result.has("jailed"));
                String rate = commissionRates.get("rate").asText();
                String maxRate = commissionRates.get("max_rate").asText();
                String maxChangeRate = commissionRates.get("max_change_rate").asText();

                lines.add(new NameAndValue("Operator Address", operatorAddress));
                lines.add(new NameAndValue("Moniker", moniker));

                lines.add(new NameAndValue("Status", status.toString()));
                lines.add(new NameAndValue("In Jail", isJailed));
                lines.add(new NameAndValue("Tokens", tokens));

                lines.add(new NameAndValue("Description", details));
                lines.add(new NameAndValue("Website", website));

                lines.add(new NameAndValue("Rate", rate));
                lines.add(new NameAndValue("Max Rate", maxRate));
                lines.add(new NameAndValue("Max Change Rate", maxChangeRate));

                message = lines
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("\n"));
              }
              silent.send(message, getChatId(u));
              sendMainMenuMessage(u);
            })
            .enableStats("Sho Validator Info")
        )
        .build();
  }

  private void sendMainMenuMessage(Update u) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setReplyMarkup(MAIN_MENU_KEYBOARD);
    sendMessage.setText("Select an option");
    sendMessage.setChatId(getChatId(u));
    silent.execute(sendMessage);
  }

  private boolean isUpdateFor(Update u, Command c) {
    return u.getCallbackQuery() != null &&
        c.getCommandData().equals(u.getCallbackQuery().getData());
  }

  private JsonNode httpRequest(HttpUriRequest request) {
    try {
      HttpResponse response = client.execute(request);
      return MAPPER.readTree(response.getEntity().getContent());
    } catch (Exception e) {
      System.out.println("Failed to execute request");
      throw new RuntimeException(e);
    }
  }

  @Override
  public long creatorId() {
    return creatorId;
  }
}
