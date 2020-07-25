package btcAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.storm.shade.org.json.simple.JSONArray;
import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

public class StatTransactionBolt extends BaseWindowedBolt {
	
	private OutputCollector outputCollector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;
	}
	
	
	@Override
	public void execute(TupleWindow inputWindow) {
		ArrayList<Double> transactions = new ArrayList<Double>(); // pour stocker les montants des transactions
		ArrayList<Long> plage_temps = new ArrayList<Long>(); // pour avoir l'intervalle de temps utilisée pour la statistique
		
		Integer tupleCount = 0; // compteur du nombre de transactions 
		for (Tuple input : inputWindow.get()) {
			
			//Parser et filtrer les transactions
			if (input.getStringByField("op").equals("utx")) {
				
				JSONObject x = (JSONObject)input.getValueByField("x");
				
				Long tempStamp = (Long) x.get("time");
				
				//String hash = (String)x.get("hash");
				
				JSONArray out = (JSONArray)x.get("out");
				double montantTransax = 0.0;
				for (int i=0; i < out.size(); i++) {
					JSONObject un_out = (JSONObject)out.get(i);
					double valeur = ((long) un_out.get("value")) / 100000000.0 ;
					montantTransax += valeur;	
				}
				
				transactions.add(montantTransax);
				plage_temps.add(tempStamp);
				
				outputCollector.ack(input);
				tupleCount +=1;
				
			}
		}
		
		System.out.printf("====== Transaction: Received %d tuples\n", tupleCount);
		
		// Détermination du max et somme des transactions
		Double transactionMax = Collections.max(transactions);
		Double montantTotalTransx = calculSomme(transactions);
		
		// Intervalle de temps utilisée pour la statistique
		Long from = plage_temps.get(0);
		Long until = Collections.max(plage_temps);
		//String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		
		
		outputCollector.emit(new Values(from, until, tupleCount, 
				transactionMax, montantTotalTransx));
		
	}
	
	
	public Double calculSomme (ArrayList<Double> liste) {
		double sum = 0.0;
		for(Double d : liste)
		    sum += d;
		return sum;
	}
	
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("from", "until", "nbTransactions", 
				"transactionMax", "montantTotalTransx"));
	}

}
