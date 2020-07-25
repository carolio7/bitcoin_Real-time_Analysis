package btcAnalytics;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

public class StatMinageBolt extends BaseWindowedBolt {
	
	private OutputCollector outputCollector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;
	}
	
	
	@Override
	public void execute(TupleWindow inputWindow) {
		HashMap<String, ArrayList<Double>> dernieresMinages = new HashMap<String, ArrayList<Double>>();
		ArrayList<Long> plage_temps = new ArrayList<Long>(); // pour avoir l'intervalle de temps utilisée pour la statistique
		
		Integer tupleCount = 0;
		double montantTotalMineeDansInterval = 0.0;
		for (Tuple input : inputWindow.get()) {
			
			if (input.getStringByField("op").equals("block")) {
				
				JSONObject x = (JSONObject)input.getValueByField("x");
				
				Long tempStamp = (Long) x.get("time");
				
				String hash = (String)x.get("hash");
				
				double btcSent = ((long) x.get("totalBTCSent")) / 100000000.0 ;
				
				JSONObject foundBy = (JSONObject)x.get("foundBy");
				
				String miner = (String)foundBy.get("desciption");
				// Si le nom du mineur est vide,
				//on utilisera le hash de l'opération pour récuperer son nom dans un autre API
				// si on n'arrive toujours pas à l'avoir, on le met "anonymous"
				if (isNullOrEmpty(miner)) {
					try {
						String minerIdByOtherAPI = getMinerName(hash);
						if (!isNullOrEmpty(minerIdByOtherAPI)) {
							miner = minerIdByOtherAPI;
						} else {
							miner = "anonymous";
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				
				
				
				dernieresMinages.putIfAbsent(miner, new ArrayList<Double>());
				dernieresMinages.get(miner).add(btcSent);
				
				plage_temps.add(tempStamp);
				
				outputCollector.ack(input);
				tupleCount +=1;
				montantTotalMineeDansInterval += btcSent;
				
			}
			
		}
		System.out.printf("====== Minages: Received %d blocks\n", tupleCount);
		
		// Détermination du max des transactions
		String grandMineur = new String();
		Double btcGrandMineur = 0.0;
		for (Entry<String, ArrayList<Double>> minage: dernieresMinages.entrySet()) {
			Double totalMineeParleMineur = 0.0;
			for (Double btcMined: minage.getValue()) {
				totalMineeParleMineur += btcMined;
			}
			if (totalMineeParleMineur > btcGrandMineur) {
				grandMineur = minage.getKey();
				btcGrandMineur = totalMineeParleMineur;
			}
		}
		
		
		//Intervalle de temps utilisé pour cette analyse
		Long from = plage_temps.get(0);
		Long until = Collections.max(plage_temps);
		//String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		
		outputCollector.emit(new Values(from, until, tupleCount,
				grandMineur, btcGrandMineur, montantTotalMineeDansInterval));
		
	}
	
	
	
	public boolean isNullOrEmpty(String str) {
		/*
		 * Fonction qui dit si un String est vide
		 * Retourne un booléen
		 */
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }
	
	
	
	public String getMinerName(String hash)throws Exception{
		/*
		 Fonction pour récuperer le nom du mineur à l'API
		 btc.com
		 	Cette fonctin prend  le hash comme paramètre: String
		 	et retourne ne nom du mineur en String
		 */
		String blockString = new Scanner(new URL(
				"https://chain.api.btc.com/v3/block/" + hash
				).openStream(), "UTF-8")
				.useDelimiter("\\A").next();
		
		JSONParser jsonParser = new JSONParser();
		JSONObject obj = (JSONObject)jsonParser.parse(blockString);
		JSONObject data = (JSONObject)obj.get("data");
		JSONObject extras = (JSONObject)data.get("extras");
		String miner_name = (String)extras.get("pool_name");
		
		return miner_name;
	}
	
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("from", "until", "nbMinages", 
				"grandMineur", "btcDoneByMiner", "btcTotalForStudy"));
	}

}
