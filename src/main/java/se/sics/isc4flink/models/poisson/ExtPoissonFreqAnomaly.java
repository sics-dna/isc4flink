package se.sics.isc4flink.models.poisson;
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

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.windowing.time.Time;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.core.ExtKeyedAnomalyFlatMap;
import se.sics.isc4flink.core.PayloadFold;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.models.ExtCountWindFold;
import se.sics.isc4flink.core.ExtWindowTimeExtractor;

public class ExtPoissonFreqAnomaly<K,V,RV> {

    private ExtKeyedAnomalyFlatMap<K,PoissonModel,RV> afm;

    public ExtPoissonFreqAnomaly(boolean addIfAnomaly, double anomalyLevel, History hist){
        this.afm = new ExtKeyedAnomalyFlatMap<>(14d,new PoissonModel(hist), addIfAnomaly);
    }

    public ExtPoissonFreqAnomaly(History hist){
        this(false,14d,hist);
    }

    public DataStream<Tuple3<K, AnomalyResult, RV>> getAnomalySteam(DataStream<V> ds, KeySelector<V, K> keySelector , PayloadFold<V, RV> valueFold, Time window) {

        KeyedStream<V, K> keyedInput = ds
                .keyBy(keySelector);

        TypeInformation<Object> foldResultType = TypeExtractor.getUnaryOperatorReturnType(valueFold,
                PayloadFold.class,
                false,
                false,
                ds.getType(),
                "PayloadFold",
                false);
 TypeInformation<Tuple3<K,Tuple4<Double,Double,Long,Long>,RV>> resultType = (TypeInformation) new TupleTypeInfo<>(Tuple3.class,
                new TypeInformation[] {keyedInput.getKeyType(), new TupleTypeInfo(Tuple4.class,
                        BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.LONG_TYPE_INFO,BasicTypeInfo.LONG_TYPE_INFO), foldResultType});

        Tuple3<K,Tuple4<Double,Double,Long,Long>, RV> init= new Tuple3<>(null,new Tuple4<>(0d,0d,0l,0l), valueFold.getInit());
        KeyedStream<Tuple3<K,Tuple4<Double,Double,Long,Long>,RV>, Tuple> kPreStream = keyedInput
                .timeWindow(window)
                .apply(init, new ExtCountWindFold<>(keySelector,valueFold, window, resultType),new ExtWindowTimeExtractor(resultType))
                .keyBy(0);

        return kPreStream.flatMap(afm);
    }
}