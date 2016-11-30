package se.sics.isc4flink.examples;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

/**
 * Created by mneumann on 2016-11-30.
 */
public class PoissonFrequencyGenerator implements SourceFunction<Tuple3<String,Long,Double>>{
    private double mean = 200d;
    private double meanFault = 10d;
    private static volatile Random rnd = new Random();
    private volatile boolean isRunning = true;
    private long starttime;

    private volatile boolean anomaly = false;
    private volatile boolean anomalyK2 = false;
    private volatile double repeatK2 = -1;
    private volatile long numK1 = 0;
    private volatile long numK2 = 0;

    private SourceFunction.SourceContext<Tuple3<String,Long, Double>> sourceContextback;

    private static int getPoissonRandom(double mean) {

        double L = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do {
            p = p * rnd.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
    }



    @Override
    public void run(SourceFunction.SourceContext<Tuple3<String,Long, Double>> sourceContext) throws Exception {
        this.sourceContextback = sourceContext;
        this.starttime= System.currentTimeMillis();
        while(isRunning){

            if(!anomaly){
                int val = getPoissonRandom(mean);
                Thread.sleep(getPoissonRandom(mean));
                sourceContext.collect(new Tuple3<>("key1", numK1, new Double(val)));
            }else{
                int val = getPoissonRandom(meanFault);
                Thread.sleep(getPoissonRandom(meanFault));
                sourceContext.collect(new Tuple3<>("key1", numK1, new Double(val)));
            }
            numK1++;

            if(System.currentTimeMillis()-starttime > 10000 && !anomaly){
                anomaly = true;
                System.out.println("--------------------anomaly");
            }


                /*
                //generator for K2
                double k2val;
                if(!anomalyK2){
                    k2val = getPoissonRandom(mean);
                    if(rnd.nextInt(20)==1)anomalyK2 = true;
                }else {
                    k2val = getPoissonRandom(meanFault);

                    if (rnd.nextInt(8) == 1) anomalyK2 = false;
                }

                if (repeatK2 == -1){
                    sourceContext.collect(new Tuple3<>("key2", numK2 ,k2val));
                    if (rnd.nextInt(10) == 1) {
                        repeatK2 = k2val;
                    }
                }else{
                    sourceContext.collect(new Tuple3<>("key2", numK2 ,repeatK2));
                    if (rnd.nextInt(5) == 1) {
                        repeatK2 = -1;
                    }
                    numK2++;
                }
                */
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
