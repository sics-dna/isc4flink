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

import se.sics.isc4flink.history.HistoryValue;

public class AnomalyResult<H extends HistoryValue> {
    private double score;
    private double cutoff;
    private long sTime;
    private long eTime;
    private H window;
    private H history;


    public AnomalyResult(double score, long sTime, long eTime, double cutoff, H window, H history){
        this.score = score;
        this.sTime = sTime;
        this.eTime = eTime;
        this.cutoff = cutoff;
        this.window = window;
        this.history = history;
    }

    public double getScore(){ return score; }
    public boolean isAnomaly(){ return score > cutoff; }
    public H getWindow(){ return window; }
    public H getHistory(){ return history; }

    public long getStartTime(){return sTime;}
    public long getEndTime(){return eTime;}

    @Override
    public String toString() {
        //return "";
        return (Double.toString(score) +" "+ cutoff + " "+ Long.toString(getStartTime())+ " "+ Long.toString(getEndTime())+ " "+window + " "+history);
    }
}
