package se.sics.isc4flink.examples.handson.solution2;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.examples.AnomalyResultSink;
import se.sics.isc4flink.examples.PoissonFrequencyGenerator2;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.history.HistoryTrailing;
import se.sics.isc4flink.models.poisson.PoissonFreqAnomaly;

public class PoissonFeqExampleClean {

	public static void main(String[] args) throws Exception {

		// set up the execution environment
		final StreamExecutionEnvironment env
				= StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

		// generate stream
		DataStream<Tuple3<String, Long,Double>> inStream
				= env.addSource(new PoissonFrequencyGenerator2());

		// use flatmap to remove duplictes from the stream
		DataStream<Tuple3<String, Long,Double>> cleanStream
				=inStream.keyBy(0).flatMap(new DuplicateFilter());

		// Choose and a History defining what the latest window will be compared to. In this case each new window will be compared to the aggregation of the last two windows.
		History hist
				= new HistoryTrailing(4);

		// Choose a distribution the value is supposed to follow and initialize it with a history.
		PoissonFreqAnomaly<String,Tuple3<String,Long,Double>> anomalyDetector
				= new PoissonFreqAnomaly<>(hist);

		// feed the stream into the model and get back a stream of AnomalyResults. For details see the different internal classes defined below.
		DataStream<Tuple2<String,AnomalyResult>> result
				= anomalyDetector.getAnomalySteam(cleanStream,new KExtract(), Time.seconds(5));

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

	public static class DuplicateFilter extends RichFlatMapFunction<Tuple3<String, Long,Double>,Tuple3<String, Long,Double>> {
		private transient ValueState<Long> largestSequenceSeen;

		@Override
		public void open(Configuration config) {
			ValueStateDescriptor<Long> descriptor =
					new ValueStateDescriptor<Long>(
							"largest sequence", // the state name
							TypeInformation.of(new TypeHint<Long>() {}), // type information
							0L); // default value of the state, if nothing was set
			largestSequenceSeen = getRuntimeContext().getState(descriptor);
		}

		@Override
		public void flatMap(Tuple3<String, Long, Double> in, Collector<Tuple3<String, Long, Double>> collector) throws Exception {
			if(in.f1 > largestSequenceSeen.value()){
				largestSequenceSeen.update(in.f1);
				collector.collect(in);
			}
		}
	}
}
