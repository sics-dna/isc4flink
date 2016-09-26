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

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

/**
 * Created by mneumann on 2016-05-09.
 */
public class ExponentialGenerator implements SourceFunction<Tuple2<String,Double>> {

        private double lambda1 = 10d;
        private volatile Random rnd = new Random();
        private volatile boolean isRunning = true;
        private volatile boolean anomaly = false;

        public double getNext(double lambda) {
            return  Math.log(1-rnd.nextDouble())/(-lambda);
        }

        @Override
        public void run(SourceContext<Tuple2<String, Double>> sourceContext) throws Exception {
            while(isRunning){
                Thread.sleep(1000);
                if(!anomaly){
                    sourceContext.collect(new Tuple2<>("key1", getNext(lambda1)));
                    //sourceContext.collect(new Tuple2<>("key2", getNext(lambda1)));
                    if(rnd.nextInt(40)<1)anomaly = true;
                }else{
                    sourceContext.collect(new Tuple2<>("key1", getNext(lambda1)*20));
                    //sourceContext.collect(new Tuple2<>("key2", getNext(lambda1)*20));
                    if(rnd.nextInt(10)<1)anomaly = false;
                }
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }

}
