import requests
import geoip2.database


def get_location_country(ip):
    try:
        with geoip2.database.Reader('maxmind/GeoLite2-Country.mmdb') as reader:
            response = reader.country(ip)
            return response.country.name
    except:
        print(f"Failed to process {ip}, skipping it")
        return "Not Found"



def get_nodes_ips(rpc_endpoint):
    peers = requests.get(rpc_endpoint + "/net_info").json()['result']['peers']
    result = set()
    for peer in peers:
        result.add(peer['remote_ip'])
    return result


def locate_nodes(rpc_endpoint):
    ips = get_nodes_ips(rpc_endpoint)
    countires = []
    for ip in ips:
        countires.append(get_location_country(ip))
    return countires


if __name__ == '__main__':
    print("enter your rpc endpoint")