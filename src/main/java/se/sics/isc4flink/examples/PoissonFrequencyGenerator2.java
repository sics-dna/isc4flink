package se.sics.isc4flink.examples;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import javax.swing.*;
import java.util.Random;

/**
 * Created by mneumann on 2016-11-30.
 */
public class PoissonFrequencyGenerator2 implements SourceFunction<Tuple3<String,Long,Double>>{
    private static double mean = 200d;
    private static double meanFault = 10d;
    private static volatile Random rnd = new Random();
    private volatile boolean isRunning = true;
    private long starttime;
    private static long delay = 100000;

    private volatile boolean anomaly = false;
    private volatile long numK1 = 0;

    private int chanceToRepeat = 60;

    private static FrequencyGeneratorButton button = null;


    public PoissonFrequencyGenerator2(double mean, double meanfault, long delay){
        this.mean = mean;
        this.meanFault = meanfault;
        this.delay = delay;
    }

    public PoissonFrequencyGenerator2(long delay){
        this.delay = delay;
    }

    public PoissonFrequencyGenerator2(){
        this.button = new FrequencyGeneratorButton();

    }

    public PoissonFrequencyGenerator2(double mean, double meanfault){
        this.mean = mean;
        this.meanFault = meanfault;
        this.button = new FrequencyGeneratorButton();
    }

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
    public void run(SourceContext<Tuple3<String,Long, Double>> sourceContext) throws Exception {
        this.starttime= System.currentTimeMillis();
        if(button!= null){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    button.createAndShowGUI();
                }
            });
        }

        while(isRunning){

            if(button!=null){
                anomaly = button.anomaly;
            }

            if(!anomaly){
                int val = getPoissonRandom(mean);
                Thread.sleep(getPoissonRandom(mean));
                sourceContext.collect(new Tuple3<>("key1", numK1, new Double(val)));
                if(rnd.nextInt(chanceToRepeat)== 1){
                    for (int i=0 ; i<100;i++){
                        sourceContext.collect(new Tuple3<>("key1", numK1, new Double(val)));
                    }
                }
            }else{
                int val = getPoissonRandom(meanFault);
                Thread.sleep(getPoissonRandom(meanFault));
                sourceContext.collect(new Tuple3<>("key1", numK1, new Double(val)));
                if(rnd.nextInt(chanceToRepeat)== 1){
                    for (int i=0 ; i<100;i++){
                        sourceContext.collect(new Tuple3<>("key1", numK1, new Double(val)));
                    }
                }
            }
            numK1++;

            if(System.currentTimeMillis()-starttime > delay && !anomaly && button!=null){
                anomaly = true;
            }
        }
    }


    @Override
    public void cancel() {
        isRunning = false;
    }


}
