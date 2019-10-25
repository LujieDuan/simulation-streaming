package io.lujieduan;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App 
{
    private static JedisPool jedisPool;

    public static void main( String[] args )
    {
        System.out.println( "Redis Logger Started." );

        String[] channels = {"Royal_University_Hospital_patient",
                            "Saskatoon_City_Hospital_patient",
                            "St_Pauls_Hospital_patient"};

        String jedisServer = "localhost";
        int jedisPort  = 6379;
        if (args.length >= 1)
            jedisServer = args[0];
        if (args.length >= 2)
            jedisPort = Integer.parseInt(args[1]);

        jedisPool = new JedisPool(jedisServer, jedisPort);

        for (String channelName:
             channels) {
            new Thread(() -> {
                try {
                    System.out.println(String.format("Connecting to %s", channelName));
                    Jedis jedis = jedisPool.getResource();
                    System.out.println(String.format("Subscribing to %s", channelName));
                    Subscriber sub = new Subscriber(System.out::println);
                    jedis.subscribe(sub, channelName);
                    System.out.println(String.format("Unsubscribing to %s", channelName));
                    jedis.quit();
                } catch (Exception e) {
                    System.out.println(">>> ERROR - " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
            
        }

    }
}
