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


import org.apache.flink.api.java.tuple.Tuple2;
import se.sics.isc4flink.history.HistoryValue;

/**
 * Created by mneumann on 2016-04-21.
 */
public class PoissonHValue extends Tuple2<Double,Double> implements HistoryValue {

    public PoissonHValue(){
        super(0d, 0d);
    }

    @Override
    public void add(HistoryValue v) {
        this.f0 += ((PoissonHValue)v).f0;
        this.f1 += ((PoissonHValue)v).f1;
    }

    @Override
    public HistoryValue getEmpty() {
        return new PoissonHValue();
    }
}
