package com.nearhop.nearhop.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.databinding.DataBindingUtil;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nearhop.nearhop.AsyncTasks.ScanHostsAsyncTask;
import com.nearhop.nearhop.R;
import com.nearhop.nearhop.databinding.ActivityMainBinding;
import com.nearhop.nearhop.db.Database;
import com.nearhop.nearhop.response.MainAsyncResponse;
import com.nearhop.nearhop.utilities.Errors;
import com.nearhop.nearhop.utilities.Host;
import com.nearhop.nearhop.utilities.NHWireless;
import com.nearhop.nearhop.utilities.UserPreference;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends NHActivity implements MainAsyncResponse {
//    MainActivityBinding binding;
    ActivityMainBinding binding;
    private final static int TIMER_INTERVAL = 1500;
    private Database db;
    private NHWireless wifi;
    private ProgressDialog scanProgressDialog;
    private Handler signalHandler = new Handler();
    private  Handler scanHandler;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter = new IntentFilter();
    private List<Host> hosts = Collections.synchronizedList(new ArrayList<Host>());

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.scandevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });
        db = Database.getInstance(getApplicationContext());
        scanHandler = new Handler(Looper.getMainLooper());

        wifi = new NHWireless(getApplicationContext());

    }

    void scanForDevices(){

        try {
            if (!wifi.isEnabled()) {
                Errors.showError(getApplicationContext(), getResources().getString(R.string.wifiDisabled));
                return;
            }

            if (!wifi.isConnectedWifi()) {
                Errors.showError(getApplicationContext(), getResources().getString(R.string.notConnectedWifi));
                return;
            }
        } catch (NHWireless.NoWifiManagerException | NHWireless.NoConnectivityManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.failedWifiManager));
            return;
        }

        int numSubnetHosts;
        try {
            numSubnetHosts = wifi.getNumberOfHostsInWifiSubnet();
        } catch (NHWireless.NoWifiManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.failedSubnetHosts));
            return;
        }


        hosts.clear();

        scanProgressDialog = new ProgressDialog(MainActivity.this, R.style.DialogTheme);
        scanProgressDialog.setCancelable(false);
        scanProgressDialog.setTitle(getResources().getString(R.string.hostScan));
        scanProgressDialog.setMessage(String.format(getResources().getString(R.string.subnetHosts), numSubnetHosts));
        scanProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        scanProgressDialog.setProgress(0);
        scanProgressDialog.setMax(numSubnetHosts);
        scanProgressDialog.show();

        try {
            Integer ip = wifi.getInternalWifiIpAddress(Integer.class);
            new ScanHostsAsyncTask(MainActivity.this, db).execute(ip, wifi.getInternalWifiSubnet(), UserPreference.getHostSocketTimeout(getApplicationContext()));
        } catch (UnknownHostException | NHWireless.NoWifiManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.notConnectedWifi));
        }
    }


    /**
     * Sets up the device's MAC address and vendor
     */
    public void setupMac() {
        try {
            if (!wifi.isEnabled()) {
                return;
            }
            String mac = wifi.getMacAddress();
        } catch (UnknownHostException | SocketException | NHWireless.NoWifiManagerException e) {
        } catch (IOException | SQLiteException | UnsupportedOperationException e) {
        }
    }



    /**
     * Sets up and registers receivers
     */
    private void setupReceivers() {
        receiver = new BroadcastReceiver() {

            /**
             * Detect if a network connection has been lost or established
             * @param context
             * @param intent
             */
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) {
                    return;
                }

                getNetworkInfo(info);
            }

        };

        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    /**
     * Gets network information about the device and updates various UI elements
     */
    private void getNetworkInfo(NetworkInfo info) {
        setupMac();
        getExternalIp();

        try {
            boolean enabled = wifi.isEnabled();
            if (!info.isConnected() || !enabled) {
                signalHandler.removeCallbacksAndMessages(null);
            }

            if (!enabled) {

                return;
            }
        } catch (NHWireless.NoWifiManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.failedWifiManager));
        }

        if (!info.isConnected()) {
            return;
        }

        signalHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int signal;
                int speed;
                try {
                    speed = wifi.getLinkSpeed();
                } catch (NHWireless.NoWifiManagerException e) {
                    Errors.showError(getApplicationContext(), getResources().getString(R.string.failedLinkSpeed));
                    return;
                }
                try {
                    signal = wifi.getSignalStrength();
                } catch (NHWireless.NoWifiManagerException e) {
                    Errors.showError(getApplicationContext(), getResources().getString(R.string.failedSignal));
                    return;
                }

                signalHandler.postDelayed(this, TIMER_INTERVAL);
            }
        }, 0);

        getInternalIp();
        getExternalIp();

        String wifiSsid;
        String wifiBssid;
        try {
            wifiSsid = wifi.getSSID();
        } catch (NHWireless.NoWifiManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.failedSsid));
            return;
        }
        try {
            wifiBssid = wifi.getBSSID();
        } catch (NHWireless.NoWifiManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.failedBssid));
            return;
        }

    }

    /**
     * Wrapper method for getting the internal wireless IP address.
     * This gets the netmask, counts the bits set (subnet size),
     * then prints it along side the IP.
     */
    private void getInternalIp() {
        try {
            int netmask = wifi.getInternalWifiSubnet();
            String internalIpWithSubnet = wifi.getInternalWifiIpAddress(String.class) + "/" + Integer.toString(netmask);
        } catch (UnknownHostException | NHWireless.NoWifiManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.notConnectedLan));
        }
    }

    /**
     * Wrapper for getting the external IP address
     * We can control whether or not to do this based on the user's preference
     * If the user doesn't want this then hide the appropriate views
     */
    private void getExternalIp() {
        wifi.getExternalIpAddress(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (scanProgressDialog != null) {
            scanProgressDialog.dismiss();
        }
        scanProgressDialog = null;
    }
    @Override
    protected void onStart() {
        // call the superclass method first
        super.onStart();
        registerReceiver(receiver, intentFilter);

    }
    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        signalHandler.removeCallbacksAndMessages(null);

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    /**
     * Delegate to update the host list and dismiss the progress dialog
     * Gets called when host discovery has finished
     *
     * @param h The host to add to the list of discovered hosts
     * @param i Number of hosts
     */
    @Override
    public void processFinish(final Host h, final AtomicInteger i) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                hosts.add(h);
            }
        });
    }

    /**
     * Delegate to update the progress of the host discovery scan
     *
     * @param output The amount of progress to increment by
     */
    @Override
    public void processFinish(int output) {
        if (scanProgressDialog != null && scanProgressDialog.isShowing()) {
            scanProgressDialog.incrementProgressBy(output);
        }
    }

    /**
     * Delegate to handle setting the external IP in the UI
     *
     * @param output External IP
     */
    @Override
    public void processFinish(String output) {
        String myString = output;
    }

    /**
     * Delegate to dismiss the progress dialog
     *
     * @param output
     */
    @Override
    public void processFinish(final boolean output) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                if (output && scanProgressDialog != null && scanProgressDialog.isShowing()) {
                    scanProgressDialog.dismiss();
                }
            }
        });
    }

    /**
     * Delegate to handle bubbled up errors
     *
     * @param output The exception we want to handle
     * @param <T>    Exception
     */
    @Override
    public <T extends Throwable> void processFinish(final T output) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                Errors.showError(getApplicationContext(), output.getLocalizedMessage());
            }
        });
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
