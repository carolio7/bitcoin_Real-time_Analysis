package btcAnalytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.shade.org.json.simple.parser.ParseException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

//import org.json.JSONObject;


public class ParserPriceBolt extends BaseRichBolt {
	private OutputCollector outputCollector;
	
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;

	}

	@Override
	public void execute(Tuple input) {
		try {
			extract(input);
		} catch (ParseException e) {
			e.printStackTrace();
			outputCollector.fail(input);
		}

	}
	
	
	public void extract(Tuple input) throws ParseException {
		// considerant le temps d'écart entre la requete sur API Coindesk et reception storm négligeable
		//String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		Date now = new Date();
		
		JSONParser jsonParser = new JSONParser();
		JSONObject obj = (JSONObject)jsonParser.parse(input.getStringByField("value"));
		
		JSONObject time = (JSONObject)obj.get("time");
		String updated = (String)time.get("updated");
		
		JSONObject bpi = (JSONObject)obj.get("bpi");
		
		JSONObject euro_json = (JSONObject)bpi.get("EUR");
		Double euro = (Double)euro_json.get("rate_float");
		
		JSONObject usd_json = (JSONObject)bpi.get("USD");
		Double usd = (Double)usd_json.get("rate_float");
		
		JSONObject gbp_json = (JSONObject)bpi.get("GBP");
		Double gbp = (Double)gbp_json.get("rate_float");
		
		
		outputCollector.emit(new Values(now, euro, usd, gbp, updated));
		outputCollector.ack(input);
	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("now", "euro", "usd","gbp", "updated"));

	}

}
