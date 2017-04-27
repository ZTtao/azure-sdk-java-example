package com.vianet.storm;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;

import java.io.FileReader;
import java.util.Properties;

import org.apache.storm.eventhubs.spout.EventHubSpout;
import org.apache.storm.eventhubs.spout.EventHubSpoutConfig;

public class EventHubReader {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        if (args.length > 1) {
            properties.load(new FileReader(args[1]));
        } else {
            properties.load(EventHubReader.class.getClassLoader().getResourceAsStream("EventHubs.properties"));
        }

        String topologyName = "reader";
        if (args != null && args.length > 0) {
            topologyName = args[0];
        }
        String policyName = properties.getProperty("eventhubs.readerpolicyname");
        String policyKey = properties.getProperty("eventhubs.readerpolicykey");
        String namespaceName = properties.getProperty("eventhubs.namespace");
        String entityPath = properties.getProperty("eventhubs.entitypath");
        int partitionCount = Integer.parseInt(properties.getProperty("eventhubs.partitions.count"));
//        String zkConnectionString = properties.getProperty("eventhubs.zkConnectionString");
//        int interval = Integer.parseInt(properties.getProperty("eventhubs.checkpoint.interval"));

        EventHubSpoutConfig spoutConfig = new EventHubSpoutConfig(policyName, policyKey, namespaceName, entityPath, partitionCount);
        spoutConfig.setTargetAddress("servicebus.chinacloudapi.cn");
        //Used to build the topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("eventhubspout", new EventHubSpout(spoutConfig), spoutConfig.getPartitionCount())
                .setNumTasks(spoutConfig.getPartitionCount());

        SyncPolicy syncPolicy = new CountSyncPolicy(10);
        FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(10.0f, Units.KB);
        RecordFormat recordFormat = new DelimitedRecordFormat().withFieldDelimiter(",");
        FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath("/devicedata/" + topologyName);
        HdfsBolt wasbBolt = new HdfsBolt()
                .withFsUrl("wasb:///")
                .withRecordFormat(recordFormat)
                .withFileNameFormat(fileNameFormat)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);

        builder.setBolt("parserbolt", new ParserBolt(), spoutConfig.getPartitionCount())
                .shuffleGrouping("eventhubspout")
                .setNumTasks(spoutConfig.getPartitionCount());
        builder.setBolt("wasbbolt", wasbBolt, 10)
                .shuffleGrouping("parserbolt")
                .setNumTasks(spoutConfig.getPartitionCount());

        Config conf = new Config();
        conf.setDebug(true);

        conf.setNumWorkers(spoutConfig.getPartitionCount());
        StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
    }
}