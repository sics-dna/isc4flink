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
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import se.sics.isc4flink.core.PayloadFold;


public class ExtCountSumFold<V,K,RV> implements FoldFunction<V, Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>>, ResultTypeQueryable<Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>> {
        private PayloadFold<V,RV> plf;
        private KeySelector<V,K> kSelect;
        private KeySelector<V,Double> vSelect;

        private transient TypeInformation<Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>> resultType;

        public ExtCountSumFold(KeySelector<V, K> key, KeySelector<V,Double> value, PayloadFold<V, RV> valueFold, TypeInformation<Tuple3<K, Tuple4<Double, Double,Long,Long>, RV>> resultType){
            this.plf = valueFold;
            this.kSelect = key;
            this.vSelect = value;
            this.resultType = resultType;
        }

        @Override
        public TypeInformation<Tuple3<K,Tuple4<Double,Double,Long,Long>, RV>> getProducedType() {
            return resultType;
        }

        @Override
        public Tuple3<K, Tuple4<Double,Double,Long,Long>, RV> fold(Tuple3<K, Tuple4<Double,Double,Long,Long>, RV> out, V value) throws Exception {

            if (out.f0 == null){
                out.f0=kSelect.getKey(value);
            }

            RV plo = plf.fold(value, out.f2);
            out.f2 = plo;

            out.f1.f0 +=1;
            out.f1.f1 += vSelect.getKey(value);

            return out;
        }
}

