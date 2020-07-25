package btcAnalytics;

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

public class ParserOperations extends BaseRichBolt {
	
	private OutputCollector outputCollector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;

	}
	
	@Override
	public void execute(Tuple input) {
		try {
			process(input);
		} catch (ParseException e) {
			e.printStackTrace();
			outputCollector.fail(input);
		}
		
	}
	
	
	public void process(Tuple input) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject)jsonParser.parse(input.getStringByField("value"));
		String operation  = (String)jsonObj.get("op");
		
		if (operation.equals("block") || operation.equals("utx")) {
			
			JSONObject x = (JSONObject)jsonObj.get("x");
			outputCollector.emit(new Values(operation, x));
		}
		
		outputCollector.ack(input);
	}
	

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("op","x"));

	}

}
