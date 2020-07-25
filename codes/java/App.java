package btcAnalytics;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.tuple.Fields;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.elasticsearch.storm.EsBolt;



public class App 
{
    public static void main( String[] args ) throws AlreadyAliveException, 
    	InvalidTopologyException, AuthorizationException, InterruptedException
    {
        TopologyBuilder builder = new TopologyBuilder();
        
        // Config écriture sur Elasticsearch à chaque donnée entrant dans EsBolt au lieu d'attendre 1000 
        Map conf = new HashMap();
    	conf.put("es.batch.size.entries", "1");
        
        // Spout 1 : current price
        KafkaSpoutConfig.Builder<String, String> spoutConfigBuilderPrice = KafkaSpoutConfig
        		.builder("localhost:9092", "bitcoin-price");
        KafkaSpoutConfig<String, String> spoutConfigPrice = spoutConfigBuilderPrice.build();
        builder.setSpout("currencyKafkaSpout", new KafkaSpout<String, String>(spoutConfigPrice));
        
        builder.setBolt("parseDeviseBolt", new ParserPriceBolt())
        	.shuffleGrouping("currencyKafkaSpout");
        
        // Sauvegarde du current price dans elasticSearch
        builder.setBolt("savingES-currencyPrice", new EsBolt("current_price/devises", conf),1)
 			.shuffleGrouping("parseDeviseBolt");
        
        
        
        
        // Spout 2 : bitcoins opérations
        KafkaSpoutConfig.Builder<String, String> spoutConfigBuilderOp = KafkaSpoutConfig
        		.builder("localhost:9092", "bitcoin-transax");
        KafkaSpoutConfig<String, String> spoutConfigOp = spoutConfigBuilderOp.build();
        builder.setSpout("operationsKafkaSpout", new KafkaSpout<String, String>(spoutConfigOp));
        
        builder.setBolt("operationsParsingBolt", new ParserOperations())
        	.shuffleGrouping("operationsKafkaSpout");
        
        
        // 2.a ) Traitement des opérations de transactions
        builder.setBolt("stat-transactions", new StatTransactionBolt().withTumblingWindow(BaseWindowedBolt.Duration.minutes(5)))
    		.fieldsGrouping("operationsParsingBolt", new Fields("op"));
        
        builder.setBolt("join-transaction-currency", new JoinerTransctionsDevisesBolt())
    		.shuffleGrouping("stat-transactions")
    		.shuffleGrouping("parseDeviseBolt");
        
        			// Sauvegarde des statistiques de transactions dans elasticSearch
        builder.setBolt("savingES-transx", new EsBolt("bitcoin_transactions/stats", conf),1)
 			.shuffleGrouping("join-transaction-currency");
        
        
        
        
        // 2.b) Traitement des opérations de minages
        builder.setBolt("highest-mineur", new StatMinageBolt().withTumblingWindow(BaseWindowedBolt.Duration.minutes(20)))
        	.fieldsGrouping("operationsParsingBolt", new Fields("op"));
        
        builder.setBolt("join-minage-currency", new JoinerMinageDeviseBolt())
        	.shuffleGrouping("highest-mineur")
        	.shuffleGrouping("parseDeviseBolt");
        
        			// Sauvegarde du statistique des derniers opérations de minage dans elasticSearch
        builder.setBolt("savingES-minage", new EsBolt("bitcoin_minages/stats", conf),1)
 			.shuffleGrouping("join-minage-currency");
        
        
        
        
        StormTopology topology = builder.createTopology();
        
        Config config = new Config();
        config.setMessageTimeoutSecs(60*70);
        config.put("es.batch.size.entries", 1);
        config.put("es.storm.bolt.flush.entries.size", 1);
        config.put("topology.producer.batch.size", 1);
        config.put("topology.transfer.batch.size", 1);
        
        
        String topologyName = "bitcoinsAnalytics";
        if (args.length > 0 && args[0].equals("remote")) {
        	StormSubmitter.submitTopology(topologyName, config, topology);
        } else {
        	LocalCluster cluster = new LocalCluster();
        	cluster.submitTopology(topologyName, config, topology);
        }
        		
    }
    
}
