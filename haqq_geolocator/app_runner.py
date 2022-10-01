import countries_locator

import items_stats

RPC_ENDPOINT = "https://haqq-t.rpc.manticore.team"


if __name__ == '__main__':
    rpc_endpoint = input(
        f"enter rpc endpoint or hit enter for default: {RPC_ENDPOINT}\n"
    )
    if rpc_endpoint == '':
        rpc_endpoint = RPC_ENDPOINT

    countries = countries_locator.locate_nodes(rpc_endpoint)
    items_stats.show(countries)