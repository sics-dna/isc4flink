package se.sics.isc4flink.examples;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.history.HistoryTrailing;
import se.sics.isc4flink.models.normal.NormalFreqAnomaly;
import se.sics.isc4flink.models.poisson.PoissonFreqAnomaly;
import se.sics.isc4flink.models.poisson.PoissonValueAnomaly;

public class KeyedPoissonExample {
    public static void main(String[] args) throws Exception {

        // set up the execution environment
        final StreamExecutionEnvironment env
                = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

        // generate stream
        DataStream<Tuple3<String, Long,Double>> inStream
                = env.addSource(new PoissonFrequencyGenerator());

        // Choose and a History defining what the latest window will be compared to. In this case each new window will be compared to the aggregation of the last two windows.
        History hist
                = new HistoryTrailing(2);

        // Choose a distribution the value is supposed to follow and initialize it with a history.
        PoissonFreqAnomaly<String,Tuple3<String,Long,Double>> anomalyDetector
                = new PoissonFreqAnomaly<>(hist);

        // feed the stream into the model and get back a stream of AnomalyResults. For details see the different internal classes defined below.
        DataStream<Tuple2<String,AnomalyResult>> result
                = anomalyDetector.getAnomalySteam(inStream,new KExtract(),Time.seconds(5));

        // print the result
        //result.print();
        result.addSink(new AnomalyResultSink());


        env.execute("Simple Exponential Example Keyed");
    }

    // Simple extractor function that pulls the key out of the input pojo
    private static class KExtract implements KeySelector<Tuple3<String,Long,Double>,String>{
        @Override
        public String getKey(Tuple3<String, Long, Double> t) throws Exception {
            return t.f0;
        }
    }
}
