package com.nearhop.nearhop.utilities;

import android.database.sqlite.SQLiteException;


import com.nearhop.nearhop.AsyncTasks.ScanPortsAsyncTask;
import com.nearhop.nearhop.AsyncTasks.WolAsyncTask;
import com.nearhop.nearhop.db.Database;
import com.nearhop.nearhop.response.HostAsyncResponse;
import com.nearhop.nearhop.response.MainAsyncResponse;

import java.io.IOException;
import java.io.Serializable;

public class Host implements Serializable {

    private String hostname;
    private String ip;
    private String mac;
    private String vendor;
    private static String port = "80";

    public Host(String ip, String mac, Database db) throws IOException {
        this(ip, mac);
        setVendor(db);
    }

    public static String getPort(){
        return port;
    }
    /**
     * Constructs a host with a known IP and MAC.
     *
     * @param ip
     * @param mac
     */
    public Host(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    /**
     * Returns this host's hostname
     *
     * @return
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets this host's hostname to the given value
     *
     * @param hostname Hostname for this host
     * @return
     */
    public Host setHostname(String hostname) {
        this.hostname = hostname;

        return this;
    }

    private Host setVendor(Database db) throws IOException {
        vendor = findMacVendor(mac, db);

        return this;
    }

    /**
     * Gets this host's MAC vendor.
     *
     * @return
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Returns this host's IP address
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns this host's MAC address
     *
     * @return
     */
    public String getMac() {
        return mac;
    }

    public void wakeOnLan() {
        new WolAsyncTask().execute(mac, ip);
    }

    /**
     * Starts a port scan
     *
     * @param ip        IP address
     * @param startPort The port to start scanning at
     * @param stopPort  The port to stop scanning at
     * @param timeout   Socket timeout
     * @param delegate  Delegate to be called when the port scan has finished
     */
    public static void scanPorts(Host ip, int startPort, int stopPort, int timeout, MainAsyncResponse delegate) {
        new ScanPortsAsyncTask(delegate).execute(ip, startPort, stopPort, timeout);
    }

    /**
     * Searches for the MAC vendor based on the provided MAc address.
     *
     * @param mac
     * @param db
     * @return
     * @throws java.io.IOException
     * @throws android.database.sqlite.SQLiteException
     */
    public static String findMacVendor(String mac, Database db) throws IOException, SQLiteException {
        String prefix = mac.substring(0, 8);
        return db.selectVendor(prefix);
    }

}
