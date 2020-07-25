package btcAnalytics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import scala.collection.generic.BitOperations.Int;

public class JoinerTransctionsDevisesBolt extends BaseRichBolt {
	
	private OutputCollector outputCollector;
	public static double equivalentUnBTCenEuro = 0.0;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;

	}
	
	
	@Override
	public void execute(Tuple input) {
		// Si le tuple reçu vient du bolt parser de devise, on met à jour prix du bitcoin en euro
		if ("parseDeviseBolt".equals(input.getSourceComponent())) {
			equivalentUnBTCenEuro = input.getDoubleByField("euro");
			
		} else if ("stat-transactions".equals(input.getSourceComponent())) {
			try {
				process(input);
				outputCollector.ack(input);
			} catch (IOException e) {
				e.printStackTrace();
				outputCollector.fail(input);
			}
		}
		
	}
	
	
	public void process(Tuple input) throws IOException {
		
		Long fromLong = input.getLongByField("from");
		// convertir les timestamps en date pour faciliter l'affichage dans Kibana
		Date fromDate = new Date(fromLong * 1000L);  // le fois 1000L est pour convertir en millisecond et de type Long		
		
		Long untilLong = input.getLongByField("until");
		// convertir les timestamps en date pour faciliter l'affichage dans Kibana
		Date untilDate = new Date(untilLong * 1000L);  // le fois 1000L est pour convertir en millisecond et de type Long		
		
		//On garde quand même l'interval de temps utilisé pour l'étude
		Long timeIntervalInSecondUsed = untilLong - fromLong;
		
		Double transactionMaxBitcoin = input.getDoubleByField("transactionMax");
		Double transactionMaxEuro = transactionMaxBitcoin * equivalentUnBTCenEuro;
		
		Double montantTotalTransactionBitcoin = input.getDoubleByField("montantTotalTransx");
		Double montantTotalTransactionEuro = montantTotalTransactionBitcoin * equivalentUnBTCenEuro;
		
		int nbTransactions = input.getIntegerByField("nbTransactions");
		
		
		outputCollector.emit(new Values(fromDate, untilDate, timeIntervalInSecondUsed, nbTransactions, 
				transactionMaxBitcoin, transactionMaxEuro,
				montantTotalTransactionBitcoin, montantTotalTransactionEuro));
		
		outputCollector.ack(input);
		/*
		System.out.printf(
				"====== SaveResultsTransactions: Pour temps t entre [%d ; %d] secondes, "
				+ "on a eu %d transactions avec un max de %f bitcoins <=> %f euro "
				+ "-total: %f <=> %f euro\n", 
				fromDate, untilDate, nbTransactions, 
				transactionMaxBitcoin, transactionMaxEuro, 
				montantTotalTransactionBitcoin, montantTotalTransactionEuro
				);
		*/
		
	}
	

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("fromDate", "untilDate", "timeIntervalInSecondUsed", "nbTransactions", 
				"transactionMaxBitcoin", "transactionMaxEuro", 
				"montantTotalTransactionBitcoin", "montantTotalTransactionEuro"));

	}

}
