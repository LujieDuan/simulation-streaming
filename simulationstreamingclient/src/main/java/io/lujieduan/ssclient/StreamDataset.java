package io.lujieduan.ssclient;

import com.google.common.primitives.Doubles;
import io.lettuce.core.Consumer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class StreamDataset {

    private LinkedBlockingQueue<Double> dataList;

    private java.util.function.Consumer<String> logger;

    private String redisServer = "localhost";

    private int redisPort = 6379;

    RedisClient redisClient;

    StatefulRedisConnection<String, String> connection;

    RedisCommands<String, String> syncCommands;

    private String channelName;

    private LinkedList<Double> dataArray;

    private Double timeUnit = 1.0;

    private boolean listen = false;

    private void log(String string, Object... args) {
        if (this.logger != null)
            this.logger.accept(String.format(string, args));
    }

    public StreamDataset(String channel, double timeUnit, Collection<Double> existing, java.util.function.Consumer<String> logger) {
        dataList = new LinkedBlockingQueue<>(existing);
        redisClient = RedisClient.create("redis://" + redisServer + ":" + redisPort); // change to reflect your environment
        connection = redisClient.connect();
        syncCommands = connection.sync();
        this.logger = logger;
        this.channelName = channel;
        this.timeUnit = timeUnit;
        this.dataArray = new LinkedList<Double>();
        UUID uuid = UUID.randomUUID();
        String application_id = uuid.toString();

        new Thread(() -> {
            try {
                log("Connecting");
                syncCommands.xgroupCreate( XReadArgs.StreamOffset.from(channelName, "0-0"), application_id);
                this.listen = true;
                log("subscribing");
                while(true) {
                    if (!this.listen)
                        break;
                    List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                            Consumer.from(application_id, "consumer_1"),
                            XReadArgs.StreamOffset.lastConsumed(channelName)
                    );

                    if (!messages.isEmpty()) {
                        for (StreamMessage<String, String> message : messages) {
                            log("Message received");
                            log(message.getBody().get("value"));
                            Double c = Doubles.tryParse(message.getBody().get("value"));
                            if (c != null)
                                this.dataList.add(c);
                            // Confirm that the message has been processed using XACK
                            syncCommands.xack(channelName, application_id, message.getId());
                        }
                    }
                }
                log("subscribe returned, closing down");
            } catch (Exception e) {
                log("[ERROR]" + e.getMessage());
                e.printStackTrace();
            }
        }, "subscriberThread").start();
    }

    public StreamDataset(String channel) {
        this(channel, 1.0, Collections.emptyList(), null);
    }

    public StreamDataset(String channel, double timeUnit) {
        this(channel, timeUnit, Collections.emptyList(), null);
    }

    public StreamDataset(String channel, java.util.function.Consumer<String>  logger) {
        this(channel, 1.0, Collections.emptyList(), logger);
    }

    public StreamDataset(String channel, double timeUnit, java.util.function.Consumer<String>  logger) {
        this(channel, timeUnit, Collections.emptyList(), logger);
    }

    public StreamDataset(String channel, Collection<Double> existing) {
        this(channel, 1.0, existing, null);
    }

    public StreamDataset(String channel, double timeUnit, Collection<Double> existing) {
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
        this.listen = false;
    }

}
