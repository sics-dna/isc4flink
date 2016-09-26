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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mneumann on 2016-04-22.
 */
public class GammaTest {

    @Test
    public void testLngamma() throws Exception {
        assertEquals(Double.POSITIVE_INFINITY,Gamma.lngamma(0d),0d);
        assertEquals(0d,Gamma.lngamma(1d),0d);
        assertEquals(5905.22042320936d,Gamma.lngamma(1000d),0.0001d);
    }

    @Test
    public void testIncgammaq() throws Exception {
        assertEquals(0d,Gamma.incgammaq(-1000d,-100d),0d);
        assertEquals(0d,Gamma.incgammaq(-66d,0d),0d);
        assertEquals(0d,Gamma.lngamma(1d),0d);
    }

    @Test
    public void testIncgammap() throws Exception {
        assertEquals(0d,Gamma.incgammaq(-1d,-1d),0d);
    }
}