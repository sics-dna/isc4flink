package se.sics.isc4flink.examples.handson.task1;


import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import se.sics.isc4flink.examples.PoissonFrequencyGenerator1;


public class PoissonFeqExample {

	public static void main(String[] args) throws Exception {

		// set up the execution environment
		final StreamExecutionEnvironment env
				= StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

		// generate stream
		DataStream<Tuple3<String, Long,Double>> inStream
				= env.addSource(new PoissonFrequencyGenerator1());

		// fill in the code here






		// print the result or feed it into the result sink
		//result.addSink(new AnomalyResultSink());



		env.execute("Poisson frequency example keyed");
	}
}
