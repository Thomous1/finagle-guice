package org.example.threadPool;


import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Farmers {

    private static final int CORE_POOL_SIZE = 100;
    private static final int MAXIMUM_POOL_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 600;
    private static final int CAPACITY = 1000;


    private static final Logger LOGGER = Logger.getLogger(Farmers.class.getName());
    private ThreadPoolExecutor threadPoolExecutor;
    private LinkedList<Farmer> farmers = Lists.newLinkedList();
    private CountDownLatch countDownLatch;
    private boolean isWorking = false;

    public Farmers() {
        threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,new LinkedBlockingQueue(CAPACITY), Executors.defaultThreadFactory());
    }
    public void startCarryBricks() {
        if (isWorking || farmers.size() == 0) {
            return;
        }
        isWorking = true;
        countDownLatch = new CountDownLatch(farmers.size());
        for (Farmer farmer: farmers) {
            final Farmer finalFarmer = farmer;
            threadPoolExecutor.execute(()->{
                execute(finalFarmer, countDownLatch);
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(countDownLatch.getCount());
    }

    private static void execute(Farmer farmer, CountDownLatch latch) {
       try {
           System.out.println(Thread.currentThread().getName());
           farmer.carryBricks();
       }catch (Exception e){
           LOGGER.info("carry bricks error!");
           e.printStackTrace();
       }finally {
           if (latch != null) {
               latch.countDown();
           }
       }

    }

    public Farmers addFarmer(Farmer farmer) {
        if (isWorking) {
            return this;
        }
        this.farmers.add(farmer);
        return this;
    }
}
