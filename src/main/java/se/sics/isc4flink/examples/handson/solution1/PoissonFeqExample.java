package se.sics.isc4flink.examples.handson.solution1;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.examples.AnomalyResultSink;
import se.sics.isc4flink.examples.PoissonFrequencyGenerator1;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.history.HistoryTrailing;
import se.sics.isc4flink.models.poisson.PoissonFreqAnomaly;

public class PoissonFeqExample {

	public static void main(String[] args) throws Exception {

		// set up the execution environment
		final StreamExecutionEnvironment env
				= StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

		// generate stream
		DataStream<Tuple3<String, Long,Double>> inStream
				= env.addSource(new PoissonFrequencyGenerator1());

		// Choose and a History defining what the latest window will be compared to. In this case each new window will be compared to the aggregation of the last two windows.
		History hist
				= new HistoryTrailing(4);

		// Choose a distribution the value is supposed to follow and initialize it with a history.
		PoissonFreqAnomaly<String,Tuple3<String,Long,Double>> anomalyDetector
				= new PoissonFreqAnomaly<>(hist);

		// feed the stream into the model and get back a stream of AnomalyResults. For details see the different internal classes defined below.
		DataStream<Tuple2<String,AnomalyResult>> result
				= anomalyDetector.getAnomalySteam(inStream,new KExtract(), Time.seconds(5));

		// print the result
		//result.print();
		result.addSink(new AnomalyResultSink());


		env.execute("Simple Exponential Example Keyed");
	}

	// Simple extractor function that pulls the key out of the input pojo
	private static class KExtract implements KeySelector<Tuple3<String,Long,Double>,String> {
		@Override
		public String getKey(Tuple3<String, Long, Double> t) throws Exception {
			return t.f0;
		}
	}
}
