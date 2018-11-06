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
			Thread.currentThread().sleep(20000);
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		}

        try {
            System.out.println(l.getNext());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        l.stop();
    }
}
