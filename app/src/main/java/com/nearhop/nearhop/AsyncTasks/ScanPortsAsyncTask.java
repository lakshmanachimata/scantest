package com.nearhop.nearhop.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.nearhop.nearhop.response.HostAsyncResponse;
import com.nearhop.nearhop.response.MainAsyncResponse;
import com.nearhop.nearhop.runnable.ScanPortsRunnable;
import com.nearhop.nearhop.utilities.Host;
import com.nearhop.nearhop.utilities.UserPreference;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScanPortsAsyncTask extends AsyncTask<Object, Void, Void> {
    private final WeakReference<MainAsyncResponse> delegate;

    /**
     * Constructor to set the delegate
     *
     * @param delegate Called when a port scan has finished
     */
    public ScanPortsAsyncTask(MainAsyncResponse delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    /**
     * Chunks the ports selected for scanning and starts the process
     * Chunked ports are scanned in parallel
     *
     * @param params IP address, start port, and stop port
     */
    @Override
    protected Void doInBackground(Object... params) {
        Host host = (Host) params[0];
        int startPort = (int) params[1];
        int stopPort = (int) params[2];
        int timeout = (int) params[3];
        MainAsyncResponse activity = delegate.get();
        if (activity != null) {
            final int NUM_THREADS = UserPreference.getPortScanThreads((Context) activity);

            try {
                InetAddress address = InetAddress.getByName(host.getIp());
            } catch (UnknownHostException e) {
                activity.processFinish(false);
                activity.processFinish(e);

                return null;
            }

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(NUM_THREADS);
            Random rand = new Random();

            int chunk = (int) Math.ceil((double) (stopPort - startPort) / NUM_THREADS);
            int previousStart = startPort;
            int previousStop = startPort + chunk;

            for (int i = 0; i < NUM_THREADS; i++) {
                if (previousStop >= stopPort) {
                    executor.execute(new ScanPortsRunnable(host, previousStart, stopPort, timeout, delegate));
                    break;
                }

                int schedule = rand.nextInt((int) ((((stopPort - startPort) / NUM_THREADS) / 1.5)) + 1) + 1;
                executor.schedule(new ScanPortsRunnable(host, previousStart, previousStop, timeout, delegate), i % schedule, TimeUnit.SECONDS);

                previousStart = previousStop + 1;
                previousStop = previousStop + chunk;
            }

            executor.shutdown();

            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
                executor.shutdownNow();
            } catch (InterruptedException e) {
                activity.processFinish(e);
            }

            activity.processFinish(true);
        }

        return null;
    }
}
