# isc4flink
The ISC Anomaly Detection and Classification Framework implemented for [Apache Flink](https://flink.apache.org/). It provides the building blocks to create a distributed fault tolerant streaming anomaly detection pipeline in flink using "Bayesian Statistical Anomaly". Other ISC functionality will be ported if requested.

## Bayesian Statistical Anomaly in a Nutshell
At the core of this approach lies a function that compares an observed distribution of a certain type with a distribution of the same type defined by previously seen values. Supported distributions are lognormal, normal, poisson and exponential.
The library takes a stream of events and splits it into time windows. For each such window the number of events and the distribution defining property is stored. (e.g. for Poisson the average value) Each window is compared to a set of windows seen before (history). The result is a score representing the probability that the window and the history are the same distribution.

**Strengths:**

1. Compact model: The only data stored are the defining values for the distribution making it independent from the number of events. This allows to keep a large number of models in memory.
2. No training data needed
3. Little start up cost: as soon as the history is filled the system can return results.
4. High precision: The number of events in a window is taken into account for this approach. Single large values will not lead to high anomaly values. However precision is highly dependent on how closely the data follows the predefined distribution.

**Limitations:**

1. The date must follow the chosen distribution
2. Low recall: Anomalies can "hide" in the window aggregation. If there are extremely high values and extremely low values in the same window they might even out to a normal value which cannot be detected with this approach.
3. Little explanation: The output of the system is the probability that the window comes from the same distribution as previously seen data under the assumption both follow a predefined distribution. The output does not contain information about single events that might have lead to this outcome.

## When to use this
In order to use this approach the data needs to fulfill the following requirements:

1. The approach can be used for float values or for inter arrival time of events
2. **The values observed need to be normal, lognormal, exponential or poisson distributed**
3. The shape of the distribution can change but not the type. For example you cannot switch from normal to exponential at runtime.
4. If data with timestamps is used there needs to be a limit on how much out of order the events can be.

## How to use it
You need to find out what distribution the values follow before you can setup a pipeline. Then create a Flink streaming job or modify **KeyedExponentialExample.java**. First configure the Flink environment and set fault tolerance and timestamps if needed. Connect a source to get the input stream.

Choose and configure a history. 
There are currently two types of History available. HistoryTrailing contains the last n windows and takes n as variable in the constructor. Space complexity of this model grows liniar with n.
If the data is periodic and the period is known HistoryPeriodic can be used. It has 4 parameter, the length of the period p in number of windows, positive shift, negative shift and history length n. History length defines how many periods should be stored. Space complexity of this history is n*p. Each window will be compared to the window at the same stage of the last n periods plus the windows before and after depending on the value for positive and negative shift.

Choose a base class corresponding to the distribution you want eg ExponentialValueAnomaly and hand it the History. The getAnomalyStream method will create a stream of Tuple3 containing a key an anomaly object and additional user defined payload that can be used to store information about the window.

## About
This work is based on research done by Anders Holst: 
- Holst, Anders, et al. "Statistical anomaly detection for train fleets." AI Magazine 34.1 (2012): 33.
- Holst, Anders, and Jan Ekman. "Incremental Stream Clustering for Anomaly Detection and Classification." SCAI. 2011.
