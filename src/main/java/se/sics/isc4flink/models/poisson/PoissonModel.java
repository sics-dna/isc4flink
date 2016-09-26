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

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple4;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.core.Gamma;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.models.Model;

import java.io.Serializable;

/**
 * Created by mneumann on 2016-04-21.
 */
public class PoissonModel extends Model implements Serializable {
    private static final double prior_c = 0.0;
    private History hist;

    public PoissonModel(History hist){
        this.hist = hist;
    }

    static double calculateAnomaly(double value, double windowSize, double modelValue, double modelSize) {
        // For now everything is calculated as poisson.
        if ((value == 0.0 && windowSize == 0.0) || modelSize == 0.0){
            return 0.0;
        } else {
            double result = principal_anomaly_poisson(value, windowSize, modelValue, modelSize);
            return (result > 0.0 ? result > 1.0 ? 0.0 : -Math.log(result) : 700.0);
        }
    }

    // The principal anomaly for a poisson distribution
    public static double principal_anomaly_poisson(double value, double windowLength, double modelCount, double modelSize) {
        double sum = 0.0, lp1, lttu, luut;
        int top = (int) Math.floor(windowLength / modelSize * (modelCount - prior_c));
        int zz = (int) Math.round(value);

        // NOTE: code ported from c++ the array is a trick to do call by reference
        int[] l2 = new int[1];
        // the next line can throw infinity if u is 0.
        luut = Math.log(modelSize / (modelSize + windowLength));
        lttu = Math.log(windowLength / (modelSize + windowLength));
        lp1 = luut * (modelCount + 1.0 - prior_c) - Gamma.lngamma(modelCount + 1.0 - prior_c);
        if (top < value) {
            sum += pa_sum(top, -1, -1, zz, lp1, lttu, luut, modelCount, l2);
            sum += pa_sum(top + 1, zz, 1, zz, lp1, lttu, luut, modelCount, l2);
            sum += pa_sum(zz - 1, l2[0], -1, zz, lp1, lttu, luut, modelCount, l2);
            sum += pa_sum(zz, -1, 1, zz, lp1, lttu, luut, modelCount, l2);
        } else {
            sum += pa_sum(zz - 1, -1, -1, zz, lp1, lttu, luut, modelCount, l2);
            sum += pa_sum(zz, top, 1, zz, lp1, lttu, luut, modelCount, l2);
            sum += pa_sum(top, l2[0], -1, zz, lp1, lttu, luut, modelCount, l2);
            sum += pa_sum(top + 1, -1, 1, zz, lp1, lttu, luut, modelCount, l2);
        }

        return sum;
    }

    private static double pa_sum(int y1, int y2, int delta, int z, double lp1, double lttu, double luut, double s, int[] last) {
        double sum = 0.0, lp2, p3, lam, tmp = 1.0, peak = 0.0;
        int y;
        for (y = y1; y != y2 && tmp > peak * 1e-12; y += delta) {
            lp2 = lttu * y + Gamma.lngamma(s + 1.0 - prior_c + y) - Gamma.lngamma(1.0 + y);
            if (y != z) {
                lam = Math.exp((Gamma.lngamma(y + 1.0) - Gamma.lngamma(z + 1.0)) / (y - z) - lttu);
                p3 = (y < z ? Gamma.incgammaq(s + 1.0 - prior_c + y, lam) : Gamma.incgammap(s + 1.0 - prior_c + y, lam));
            } else
                p3 = 1.0;
            sum += tmp = Math.exp(lp1 + lp2) * p3;
            if (tmp > peak) peak = tmp;
        }
        last[0] = y - delta;
        return sum;
    }

    @Override
    public AnomalyResult calculateAnomaly(Tuple4<Double,Double,Long,Long> v, double threshold) {
        PoissonHValue w = new PoissonHValue();
        w.f0 = v.f0;
        w.f1= v.f1;

        PoissonHValue h = (PoissonHValue) hist.getHistory();
        if (h == null){
            return new AnomalyResult(-1,v.f2,v.f3,threshold,w,h);
        }
        return new AnomalyResult(calculateAnomaly(v.f0,v.f1,h.f0,h.f1),v.f2,v.f3,threshold,w,h);
    }

    @Override
    public void addWindow(Tuple4<Double,Double,Long,Long> v) {
        PoissonHValue val = new PoissonHValue();
        val.f0 = v.f0;
        val.f1 = v.f1;
        hist.addWindow(val);
    }

    @Override
    public TypeInformation getTypeInfo() {
        return TypeInformation.of(new TypeHint<PoissonModel>() {});
    }

}
