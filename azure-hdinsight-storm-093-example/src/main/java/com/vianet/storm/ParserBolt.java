package com.vianet.storm;


import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserBolt extends BaseBasicBolt {

    Logger logger = LoggerFactory.getLogger(ParserBolt.class);

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("deviceId", "deviceValue"));
    }

    //Process tuples
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String value = tuple.getString(0);
        String[] arr = value.split("}");
        for (String ehm : arr)
        {
            collector.emit(new Values("kevindevice", ehm));
        }
    }

}