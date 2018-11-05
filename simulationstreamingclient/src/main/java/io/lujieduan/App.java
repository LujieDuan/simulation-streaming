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
        LiveDataset l = new LiveDataset(ExternalLogger::log);
        System.out.println(l.needBlock());


        try {
			Thread.currentThread().sleep(100000);
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		}

		l.stop();
    }
}
