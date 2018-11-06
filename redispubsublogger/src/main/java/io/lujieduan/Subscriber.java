package io.lujieduan;

import com.google.common.primitives.Doubles;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


public class Subscriber extends JedisPubSub {

    private Consumer<String>  logger;

    public Subscriber(Consumer<String> logger) {
        super();
        this.logger = logger;
    }

    private void log(String string, Object... args) {
        if (this.logger != null)
            this.logger.accept(String.format(string, args));
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
            log("Unsubscribe [Channel] %s", channel);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        log("Subscribe [Channel] %s", channel);
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
        log("Message Received [Channel] %s [Message]: %s", channel, message);
    }
}
