package io.airlift.http.client;

import io.airlift.http.client.jetty.JettyHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ThroughputTest
{
    private static URI[] url;

    static {
        try {
            //*
            url = new URI[] {
                    new URI("http://localhost:8080/"),
            };
            /*/
            url = new URI[] {
                    new URI("http://hadoop7676.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7568.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7658.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7622.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7604.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7514.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7586.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7550.prn2.facebook.com:7777/v1/service"),
                    new URI("http://hadoop7640.prn2.facebook.com:7777/v1/service"),
            };
            //*/
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static final long TEN_SECOND = 5_000_000_000L;
    private static final int N_SECOND = 2;
    private static final long ONE_SECOND = 1_000_000_000L * N_SECOND;
    private static final HttpClient httpClient = new JettyHttpClient();

    public static void main(String[] args) throws Exception
    {
        //int threadCount = Integer.parseInt(args[0]);
        for (int threadCount = 1; threadCount <= 1024; threadCount *= 2) {
            int[] result = new int[16];
            for (int i = 0; i < result.length; i++) {
                int countPerSecond = parallelCount(threadCount) / N_SECOND;
                result[i] = countPerSecond;
            }
            System.out.format(
                    "%d %d %s%n",
                    threadCount,
                    Arrays.stream(result).max().getAsInt(),
                    Arrays.toString(result));
        }
    }

    private static int parallelCount(int threadCount) throws InterruptedException {
        final AtomicInteger sum = new AtomicInteger(0);
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    sum.addAndGet(count());
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        return sum.get();
    }

    private static int count()
    {
        try {
            long start;
            start = System.nanoTime();
            while (System.nanoTime() - start < TEN_SECOND) {
                get();
            }
            start = System.nanoTime();
            int count = 0;
            while (System.nanoTime() - start < ONE_SECOND) {
                get();
                count++;
            }
            start = System.nanoTime();
            while (System.nanoTime() - start < TEN_SECOND) {
                get();
            }
            return count;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new Error(ex);
        }
    }

    private static void get() throws Exception
    {
        //URI uri = url[ThreadLocalRandom.current().nextInt(url.length)];
        URI uri = url[0];
        Request request = Request.Builder.prepareGet().setUri(uri).build();
        httpClient.execute(request, new ResponseHandler<Object, RuntimeException>()
        {
            @Override
            public Object handleException(Request request, Exception e)
                    throws RuntimeException
            {
                e.printStackTrace();
                return null;
            }

            @Override
            public Object handle(Request request, Response response)
                    throws RuntimeException
            {
                return null;
            }
        });
    }
}