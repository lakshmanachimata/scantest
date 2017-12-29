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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nearhop.nearhop.AsyncTasks.ScanHostsAsyncTask;
import com.nearhop.nearhop.R;
import com.nearhop.nearhop.adapaters.DevicesAdapter;
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
    private ArrayList<Host> hosts = new ArrayList<Host>();
    DevicesAdapter devicesAdapter;
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


    public void scanPort(Host host){
        if(scanProgressDialog != null && scanProgressDialog.isShowing())
            scanProgressDialog.dismiss();

        try {
            if (!wifi.isConnectedWifi()) {
                Errors.showError(getApplicationContext(), getResources().getString(R.string.notConnectedLan));
                return;
            }
        } catch (NHWireless.NoConnectivityManagerException e) {
            Errors.showError(getApplicationContext(), getResources().getString(R.string.notConnectedLan));
            return;
        }

        int startPort = Integer.parseInt(Host.getPort());
        int stopPort = Integer.parseInt(Host.getPort()) + 1;
        scanProgressDialog = new ProgressDialog(MainActivity.this, R.style.DialogTheme);
        scanProgressDialog.setCancelable(false);
        scanProgressDialog.setTitle("Scanning Port " + startPort);
        scanProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        scanProgressDialog.setProgress(0);
        scanProgressDialog.setMax(1);
        scanProgressDialog.show();

        Host.scanPorts(host, startPort, stopPort, UserPreference.getLanSocketTimeout(getApplicationContext()), MainActivity.this);
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
     * @param numHosts Number of hosts
     */
    @Override
    public void processFinish(final Host h, final AtomicInteger numHosts) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                //if(numHosts.get() == hosts.size()) {
                    scanPort(h);
                //}
            }
        });
    }

    public void updateDeviceTable(Host host){
        hosts.add(host);

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devicesAdapter = new DevicesAdapter(getApplicationContext(), hosts);
                binding.deviceslist.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                binding.deviceslist.setHasFixedSize(true);
                binding.deviceslist.setAdapter(devicesAdapter);
                devicesAdapter.notifyDataSetChanged();
                binding.deviceslist.setVisibility(View.VISIBLE);
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
     * Delegate to handle open ports
     *
     * @param output Contains the port number and associated banner (if any)
     */
    @Override
    public void processFinish(Host host,SparseArray<String> output) {
        int scannedPort = output.keyAt(0);
        String item = String.valueOf(scannedPort);

        String name = db.selectPortDescription(String.valueOf(scannedPort));
        name = (name.isEmpty()) ? "unknown" : name;
        item = formatOpenPort(output, scannedPort, name, item);
        addOpenPort(item);
        updateDeviceTable(host);
    }

    /**
     * Formats a found open port with its name, description, and associated visualization
     *
     * @param entry       Structure holding information about the found open port with its description
     * @param scannedPort The port number
     * @param portName    Friendly name for the port
     * @param item        Contains the transformed output for the open port
     * @return If all associated data is found a port along with its description, underlying service, and visualization is constructed
     */
    private String formatOpenPort(SparseArray<String> entry, int scannedPort, String portName, String item) {
        String data = item + " - " + portName;
        if (entry.get(scannedPort) != null) {
            data += " (" + entry.get(scannedPort) + ")";
        }

        //If the port is in any way related to HTTP then present a nice globe icon next to it via unicode
        if (scannedPort == 80 || scannedPort == 443 || scannedPort == 8080) {
            data += " \uD83C\uDF0E";
        }

        return data;
    }

    /**
     * Adds an open port that was found on a host to the list
     *
     * @param port Port number and description
     */
    private void addOpenPort(final String port) {
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
