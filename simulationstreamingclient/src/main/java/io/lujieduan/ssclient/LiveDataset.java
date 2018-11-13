package io.lujieduan.ssclient;

import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class LiveDataset {

    private LinkedBlockingQueue<Double> dataList;

    private Consumer<String> logger;

    private String jedisServer = "localhost";

    private int jedisPort = 6379;

    private Jedis jedis;

    private Subscriber sub;

    private String channelName;

    private LinkedList<Double> dataArray;

    private Double timeUnit = 1.0;

    private void log(String string, Object... args) {
        if (this.logger != null)
            this.logger.accept(String.format(string, args));
    }

    public LiveDataset(String channel, double timeUnit, Collection<Double> existing, Consumer<String> logger) {
        dataList = new LinkedBlockingQueue<>(existing);
        this.logger = logger;
        this.channelName = channel;
        this.timeUnit = timeUnit;
        this.dataArray = new LinkedList<Double>();


        new Thread(() -> {
            try {
                log("Connecting");
                jedis = new Jedis(jedisServer, jedisPort);
                log("subscribing");
                sub = new Subscriber(dataList, logger);
                jedis.subscribe(sub, channelName);
                log("subscribe returned, closing down");
                jedis.quit();
            } catch (Exception e) {
                log(">>> OH NOES Sub - " + e.getMessage());
                e.printStackTrace();
            }
        }, "subscriberThread").start();
    }

    public LiveDataset(String channel) {
        this(channel, 1.0, Collections.emptyList(), null);
    }

    public LiveDataset(String channel, double timeUnit) {
        this(channel, timeUnit, Collections.emptyList(), null);
    }

    public LiveDataset(String channel, Consumer<String>  logger) {
        this(channel, 1.0, Collections.emptyList(), logger);
    }

    public LiveDataset(String channel, double timeUnit, Consumer<String>  logger) {
        this(channel, timeUnit, Collections.emptyList(), logger);
    }

    public LiveDataset(String channel, Collection<Double> existing) {
        this(channel, 1.0, existing, null);
    }

    public LiveDataset(String channel, double timeUnit, Collection<Double> existing) {
        this(channel, timeUnit, existing, null);
    }


    public boolean needBlock() {
        return dataList.peek() == null;
    }

    public double getNext() throws InterruptedException {
        double result = dataList.take();
        dataArray.add(result);
        return result;
    }

    public double getByTime(double timeIndex) throws InterruptedException {
        int arrayIndex = (int) Math.ceil(timeIndex / timeUnit);
        while (arrayIndex >= dataArray.size()) {
            this.getNext();
        }
        return dataArray.get(arrayIndex);
    }

    public void stop() {
        sub.unsubscribe();
    }

}
