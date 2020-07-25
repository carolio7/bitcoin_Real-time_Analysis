package btcAnalytics;

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

public class JoinerMinageDeviseBolt extends BaseRichBolt {
	
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
			
		} else if ("highest-mineur".equals(input.getSourceComponent())) {
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
		
		int nbMinage = input.getIntegerByField("nbMinages");
		
		String grandMineur = input.getStringByField("grandMineur");
		Double btcDoneByMiner = input.getDoubleByField("btcDoneByMiner");
		Double doneByMinerEuro = btcDoneByMiner * equivalentUnBTCenEuro;
		
		Double btcTotalForStudy = input.getDoubleByField("btcTotalForStudy");
		
		
		outputCollector.emit(new Values(fromDate, untilDate, timeIntervalInSecondUsed, nbMinage, 
				grandMineur, btcDoneByMiner, doneByMinerEuro, btcTotalForStudy));
		outputCollector.ack(input);
		
	}
	

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("fromDate", "untilDate", "timeIntervalInSecondUsed","nbMinage",
				"grandMineur", "btcDoneByMiner", "doneByMinerEuro", "btcTotalForStudy"));
	}

}
