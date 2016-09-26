package se.sics.isc4flink.models.normal;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple4;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.core.Gamma;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.models.Model;

import java.io.Serializable;

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

public class NormalModel extends Model implements Serializable {
    private final static double M_PI = 3.14159265358979323846;
    private final static int MAXITER = 10000;

    private History hist;

    public NormalModel(History hist){
        this.hist = hist;
    }

    public static double calculateAnomaly(double x, double c, double mn, double sc)
    {
        double dist = (x-mn)*(x-mn);
        return (dist==0.0 ? 0.0 : sc <= 0.0 ? 700.0 :
                -logIntStudent(c, Math.sqrt(dist/sc))-Math.log(2.0));
    }

    private static double logIntStudent(double c, double z)
    {
        if (z<0.0) z=-z;
        if (c*z*z >= 10.0)
            return Gamma.lngamma(c) - Gamma.lngamma(c+0.5) -0.5*Math.log(4.0*M_PI) - Math.log(1.0+z*z)*(c-0.5) +
                    Math.log(hyperGeometricBruteForce(c-0.5, 0.5, c+0.5, 1.0/(1.0+z*z)));
        else
            return Math.log(0.5 - Math.exp(Gamma.lngamma(c) - Gamma.lngamma(c-0.5) -Math.log(1.0+z*z)*c) / Math.sqrt(M_PI) * z *
                    hyperGeometricBruteForce(c, 1.0, 1.5, z*z/(1.0+z*z)));
    }

    private static double hyperGeometricBruteForce(double a, double b, double c, double z)
    {
        int n;
        double res, tmp, k;

        k = tmp = res = 1.0;
        for (n=1; n<MAXITER; n++) {
            k *= a*b/c * z/n;
            res += k;
            if (tmp == res) break;
            tmp = res;
            a += 1.0;
            b += 1.0;
            c += 1.0;
        }
        return res;
    }

    @Override
    public AnomalyResult calculateAnomaly(Tuple4<Double,Double,Long,Long> v, double threshold) {
        NormalHValue w =new NormalHValue();
        w.f0 = 1d;
        w.f1 = v.f1;
        w.f2 = v.f0 * v.f1;
        w.f3 = v.f0 * v.f0 * v.f1 ;

        NormalHValue h = (NormalHValue) hist.getHistory();
        if (h == null) {
            return new AnomalyResult(-1,v.f2,v.f3,threshold,w,h);
        }
        double mean = h.f2/h.f1;
        double scale = h.f3-h.f2*h.f2/h.f1;
        double cc = h.f0 * 0.5;
        return new AnomalyResult(calculateAnomaly(v.f0, cc, mean  , (h.f1+v.f1)/(h.f1*v.f1)*scale),v.f2,v.f3,threshold,w,h);
    }

    @Override
    public void addWindow(Tuple4<Double,Double,Long,Long> v) {
        NormalHValue hValue =new NormalHValue();
        hValue.f0 = 1d;
        hValue.f1 = v.f1;
        hValue.f2 = v.f0 * v.f1;
        hValue.f3 = v.f0 * v.f0 * v.f1 ;
        hist.addWindow(hValue);
    }

    @Override
    public TypeInformation getTypeInfo(){ return TypeInformation.of(new TypeHint<NormalModel>() {});}
}