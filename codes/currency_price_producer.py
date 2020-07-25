#! /usr/bin/env python

import json
import time
import urllib.request

from kafka import KafkaProducer

# url de récupération des data prices
url = "http://api.coindesk.com/v1/bpi/currentprice.json"

producer = KafkaProducer(bootstrap_servers="localhost:9092")

while True:
    response = urllib.request.urlopen(url)
    currency = json.loads(response.read().decode())

    updated = currency['time']['updated']
    bpi = currency['bpi']
    # topic : bitcoin-price
    producer.send("bitcoin-price",json.dumps(currency).encode()) #,key=str(station["number"]).encode())
    print("Le {} :  $:{}  , €:{}  ,  £:{}".\
    	format(updated, bpi['USD']['rate'],
    		bpi['EUR']['rate'],
    		bpi['GBP']['rate']))
    # Attente 1 minute et Mettre à jour les prix
    time.sleep(60)
