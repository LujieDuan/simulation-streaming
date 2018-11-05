package io.lujieduan.ssclient;

import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class LiveDataset {

    private BlockingDeque<Double> dataList;

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
        dataList = new LinkedBlockingDeque<>(existing);
        this.logger = logger;


        new Thread(() -> {
            try {
                log("Connecting");
                jedis = new Jedis(jedisServer, jedisPort);
                log("subscribing");
                sub = new Subscriber(logger);
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

    public double getNext() {
        return dataList.poll();
    }

    public void stop() {
        sub.unsubscribe();
    }

}
