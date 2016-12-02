package se.sics.isc4flink.examples;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.models.normal.NormalHValue;


/**
 * Created by mneumann on 2016-11-28.
 */
public class AnomalyResultSink implements SinkFunction<Tuple2<String,AnomalyResult>> {

    private static long startTime = -1;

    @Override
    public void invoke(Tuple2<String,AnomalyResult> anomalyResult) throws Exception {
        if (startTime == -1) startTime = anomalyResult.f1.getStartTime();

        if(anomalyResult.f1.isAnomaly()){
            System.err.println(formatOut(anomalyResult));
        }else{
            System.out.println(formatOut(anomalyResult));
        }
    }

    private static String formatOut(Tuple2<String,AnomalyResult> anomalyResult){
        StringBuilder res  = new StringBuilder();
        res.append(anomalyResult.f0);
        res.append("\t");
        res.append(anomalyResult.f1.getStartTime()-startTime);
        res.append("\t");
        res.append(anomalyResult.f1.getEndTime()-startTime);
        res.append("\t");
        res.append(anomalyResult.f1.getScore());

        return res.toString();
    }
}
