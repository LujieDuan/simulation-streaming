package io.lujieduan.ssclient;

import com.google.common.primitives.Doubles;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


public class Subscriber extends JedisPubSub {

    private Consumer<String>  logger;

    private LinkedBlockingQueue<Double> queue;

    public Subscriber(LinkedBlockingQueue<Double> queue) {
        super();
        this.queue = queue;
    }

    public Subscriber(LinkedBlockingQueue<Double> queue, Consumer<String> logger) {
        this(queue);
        this.logger = logger;
    }

    private void log(String string, Object... args) {
        if (this.logger != null)
            this.logger.accept(String.format(string, args));
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        log("onUnsubscribe");
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        log("onSubscribe");
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    }

    @Override
    public void onMessage(String channel, String message) {
        log("Message received");
        log(message);
        Double c = Doubles.tryParse(message);
        if (c != null)
            queue.add(c);
    }
}
