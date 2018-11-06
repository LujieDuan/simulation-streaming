package io.lujieduan.ssclient;

import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class LiveDataset {

    private LinkedBlockingQueue<Double> dataList;

    private Consumer<String> logger;

    private String jedisServer = "localhost";

    private int jedisPort = 6379;

    private Jedis jedis;

    private Subscriber sub;

    private String channelName = "testchannel";

    private void log(String string, Object... args) {
        if (this.logger != null)
            this.logger.accept(String.format(string, args));
    }

    public LiveDataset(Collection<Double> existing, Consumer<String>  logger) {
        dataList = new LinkedBlockingQueue<>(existing);
        this.logger = logger;


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

    public LiveDataset() {
        this(Collections.emptyList(), null);
    }

    public LiveDataset(Consumer<String>  logger) {
        this(Collections.emptyList(), logger);
    }

    public LiveDataset(Collection<Double> existing) {
        this(existing, null);
    }


    public boolean needBlock() {
        return dataList.peek() == null;
    }

    public double getNext() throws InterruptedException {
        return dataList.take();
    }

    public void stop() {
        sub.unsubscribe();
    }

}
