package com.vianet.storm;


import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
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