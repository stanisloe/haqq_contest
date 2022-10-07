package com.stanislove.haqq.explorer.bot;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

public class HaqqExplorerHandler extends AbilityBot {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final String apiHost;
  private final HttpClient client;

  public HaqqExplorerHandler(String botToken, String botUsername, String apiHost) {
    super(botToken, botUsername);
    this.apiHost = apiHost;
    this.client = HttpClients.createDefault();
  }

  public Ability showActiveValidators() {

    return Ability
        .builder()
        .name("show_active")
        .info("says hello world!")
        .privacy(PUBLIC)
        .action(ctx -> {
          String url = apiHost + "/staking/validators?status=BOND_STATUS_BONDED";
          HttpGet request = new HttpGet(url);
          JsonNode response = execute(request);
          List<ValidatorRow> validators = new ArrayList<>();
          response.get("result").elements().forEachRemaining(v -> {
            String moniker = v.get("description").get("moniker").asText();
            String tokens = new BigInteger(v.get("tokens").asText())
                .divide(new BigInteger("1000000000000000000"))
                .toString();
            validators.add(new ValidatorRow(moniker, tokens));
          });

          for (List<ValidatorRow> validatorBatch : Lists.partition(validators, 50)) {
            String message = validatorBatch
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
            silent.send(message, ctx.chatId());
          }
        })
        .build();
  }

  public Ability showTransactionInfo() {
    return Ability
        .builder()
        .name("show_transaction_info")
        .locality(ALL)
        .privacy(PUBLIC)
        .action(ctx -> {
          String url = apiHost + "/staking/validators?status=BOND_STATUS_BONDED";
          HttpGet request = new HttpGet(url);
          JsonNode response = execute(request);
          List<ValidatorRow> validators = new ArrayList<>();
          response.get("result").elements().forEachRemaining(v -> {
            String moniker = v.get("description").get("moniker").asText();
            String tokens = new BigInteger(v.get("tokens").asText())
                .divide(new BigInteger("1000000000000000000"))
                .toString();
            validators.add(new ValidatorRow(moniker, tokens));
          });

          for (List<ValidatorRow> validatorBatch : Lists.partition(validators, 50)) {
            String message = validatorBatch
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
            silent.send(message, ctx.chatId());
          }
        })
        .build();
  }


  private JsonNode execute(HttpUriRequest request) {
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
    return 105026306;
  }
}
