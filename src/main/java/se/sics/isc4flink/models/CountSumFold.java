package se.sics.isc4flink.models;

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

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;

public class CountSumFold<V,K> implements FoldFunction<V, Tuple2<K,Tuple4<Double,Double,Long,Long>>>, ResultTypeQueryable<Tuple2<K,Tuple4<Double,Double,Long,Long>>> {
        private KeySelector<V,K> kSelect;
        private KeySelector<V,Double> vSelect;

        private transient TypeInformation<Tuple2<K,Tuple4<Double,Double,Long,Long>>> resultType;

        public CountSumFold(KeySelector<V, K> key, KeySelector<V,Double> value, TypeInformation<Tuple2<K, Tuple4<Double, Double,Long,Long>>> resultType){
            this.kSelect = key;
            this.vSelect = value;
            this.resultType = resultType;
        }

        @Override
        public TypeInformation<Tuple2<K,Tuple4<Double,Double,Long,Long>>> getProducedType() {
            return resultType;
        }

        @Override
        public Tuple2<K, Tuple4<Double,Double,Long,Long>> fold(Tuple2<K, Tuple4<Double,Double,Long,Long>> out, V value) throws Exception {

            if (out.f0 == null){
                out.f0=kSelect.getKey(value);
            }

            out.f1.f0 +=1;
            out.f1.f1 += vSelect.getKey(value);

            return out;
        }
}

