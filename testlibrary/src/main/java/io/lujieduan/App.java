package io.lujieduan;

import io.lujieduan.ssclient.ExternalLogger;
import io.lujieduan.ssclient.LiveDataset;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        LiveDataset test = new LiveDataset(ExternalLogger::log);
        System.out.println(test);
    }
}
