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

public class Gamma {

    /**
     * Prevents instantiation.
     */
    private Gamma() {
    }

    // caching previous results to save computation
    static double lastx = -1.0;
    static double lastgl = 0.0;

    public static double lngamma(double x) {
        if (x == lastx) {
            return lastgl;
        }
        lastx = x;
        double tmp = 2.5066282746310005 * (1.000000000190015 + 76.18009172947146 / (x + 1.0) - 86.50532032941677 / (x + 2.0)
                + 24.01409824083091 / (x + 3.0) - 1.231739572450155 / (x + 4.0)
                + 0.001208650973866179 / (x + 5.0) - 0.000005395239384953 / (x + 6.0));
        return (lastgl = (x + 0.5) * Math.log(x + 5.5) - x - 5.5 + Math.log(tmp / x));
    }

    // Returns the incomplete gamma function Q(a, x) = 1-P(a, x).
    public static double incgammaq(double x, double y) {
        if (y < 0.0 || x <= 0.0)
            return 0.0;
        if (y < x + 1.0)
            return 1.0 - incgammap_s(x, y);
        else
            return incgammaq_cf(x, y);
    }

    // Returns the incomplete gamma function P(x, y).
    public static double incgammap(double x, double y) {
        if (y < 0.0 || x <= 0.0) {
            return 0.0;
        }
        if (y < x + 1.0) {
            return incgammap_s(x, y);
        } else {
            return 1.0 - incgammaq_cf(x, y);
        }
    }

    // The incomplete gamma function P(x, y) evaluated by its series representation.
    private static double incgammap_s(double x, double y) {
        int i;
        double sum, tmp, xx;
        if (y <= 0.0) {
            return 0.0;
        } else {
            tmp = sum = 1.0 / x;
            for (i = 0, xx = x; Math.abs(tmp) > 1e-15 * Math.abs(sum) && i < 100; i++) {
                xx += 1.0;
                tmp *= y / xx;
                sum += tmp;
            }
            return sum * Math.exp(-y + x * Math.log(y) - lngamma(x));
        }
    }

    // The incomplete gamma function Q(x, y) evaluated by its continued fraction representation.
    private static double incgammaq_cf(double x, double y) {
        double b = y + 1.0 - x;
        double c = 0.0;
        double d = 1.0 / b;
        double h = d;
        double a;
        for (int i = 1; Math.abs(d - c) > 1e-15 * Math.abs(c) && i <= 100; i++) {
            a = -i * (i - x);
            b += 2.0;
            d = 1.0 / (a * d + b);
            c = 1.0 / (a * c + b);
            h *= d / c;
        }
        return Math.exp(-y + x * Math.log(y) - lngamma(x)) * h;
    }

}
