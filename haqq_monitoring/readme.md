# Tenderduty


[Tenderduty](https://github.com/blockpane/tenderduty]) is a comprehensive monitoring tool for
Tendermint chains. Its primary function is to alert a validator if they are missing blocks,
and has many other features.

### Instalation

First install docker and docker compose
```
apt update && \
apt install apt-transport-https ca-certificates curl software-properties-common -y && \
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add - && \
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable" && \
apt update && \
apt-cache policy docker-ce && \
sudo apt install docker-ce -y && \
docker --version && \
apt install docker-compose
```

Install and configure tenderduty docker image
```
mkdir tenderduty && cd tenderduty
```
```
cat > docker-compose.yml << EOF
---
version: '3.2'
services:

  v2:
    image: ghcr.io/blockpane/tenderduty:latest
    command: ""
    ports:
      - "8888:8888" # Dashboard
      - "28686:28686" # Prometheus exporter
    volumes:
      - home:/var/lib/tenderduty
      - ./config.yml:/var/lib/tenderduty/config.yml
    logging:
      driver: "json-file"
      options:
        max-size: "20m"
        max-file: "10"
    restart: unless-stopped

volumes:
  home:
EOF
```

```
docker-compose pull
docker run --rm ghcr.io/blockpane/tenderduty:latest -example-config >config.yml
```

Setup required `config.yml` fields like

* user friendly chain name: - the name you want to see in the dashboard.
* chain-id: - chain id you want to monitor
* valoper_address: validator you want to monitor
* url: archive node you will use for monitoring

Optionally you can add telegram bot token to notify you once something is wrong: you miss blocks,
got jailed or if one of archive nodes is down.

Once config is ready run `docker-compose start`. Dashboard will be available at `localhost:8888`


