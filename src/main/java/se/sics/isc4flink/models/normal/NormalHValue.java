package se.sics.isc4flink.models.normal;
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
import org.apache.flink.api.java.tuple.Tuple4;
import se.sics.isc4flink.history.HistoryValue;

public class NormalHValue extends Tuple4<Double,Double,Double,Double> implements HistoryValue {
    public NormalHValue(){
        super(0d, 0d,0d,0d);
    }

    @Override
    public void add(HistoryValue v) {
        this.f0 +=((NormalHValue)v).f0;
        this.f1 +=((NormalHValue)v).f1;
        this.f2 +=((NormalHValue)v).f2;
        this.f3 +=((NormalHValue)v).f3;
    }

    @Override
    public HistoryValue getEmpty() {
        return new NormalHValue();
    }

}
