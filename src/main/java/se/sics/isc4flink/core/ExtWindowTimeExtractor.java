package se.sics.isc4flink.core;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

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

public class ExtWindowTimeExtractor<K,RV> implements WindowFunction<Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>,Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>,K,TimeWindow> ,ResultTypeQueryable<Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>> {
    private transient TypeInformation<Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>> resultType;

    public ExtWindowTimeExtractor(TypeInformation<Tuple3<K, Tuple4<Double, Double,Long,Long>, RV>> resultType){
        this.resultType = resultType;
    }

    @Override
    public void apply(K key, TimeWindow timeWindow, Iterable<Tuple3<K, Tuple4<Double, Double,Long,Long>, RV>> iterable, Collector<Tuple3<K, Tuple4<Double, Double, Long, Long>, RV>> collector) throws Exception {
        Tuple3<K,Tuple4<Double,Double,Long,Long>, RV> out = iterable.iterator().next();
        out.f1.f2 = timeWindow.getStart();
        out.f1.f3 = timeWindow.getEnd();
        collector.collect(out);
    }

    @Override
    public TypeInformation<Tuple3<K, Tuple4<Double, Double,Long,Long>, RV>> getProducedType() {
        return resultType;
    }
}
