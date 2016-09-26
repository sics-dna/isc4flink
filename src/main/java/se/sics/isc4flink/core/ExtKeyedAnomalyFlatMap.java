package se.sics.isc4flink.core;

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

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import se.sics.isc4flink.models.Model;


public class ExtKeyedAnomalyFlatMap<K,M extends Model, T> extends RichFlatMapFunction<Tuple3<K,Tuple4<Double,Double,Long,Long>, T>, Tuple3<K,AnomalyResult,T>>{
    private transient ValueState<M> microModel;
    private final double threshold;
    private boolean updateIfAnomaly;
    private M initModel;

    public ExtKeyedAnomalyFlatMap(double threshold, M model, boolean updateIfAnomaly) {
        this.threshold = threshold;
        this.updateIfAnomaly = updateIfAnomaly;
        this.initModel = model;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<M> descriptor =
                new ValueStateDescriptor<>(
                        "MicroModel",
                        initModel.getTypeInfo(),
                        initModel
                        );
        microModel = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void flatMap(Tuple3<K,Tuple4<Double,Double,Long,Long>, T> sample, Collector<Tuple3<K,AnomalyResult, T>> collector) throws Exception {
        M model = microModel.value();
        AnomalyResult res  = model.calculateAnomaly(sample.f1, threshold);

        if ( res.getScore() <= threshold || updateIfAnomaly){
            model.addWindow(sample.f1);
        }
        microModel.update(model);
        collector.collect(new Tuple3<>(sample.f0,res,sample.f2));
    }
}
