package com.nearhop.nearhop.runnable;

import android.util.SparseArray;

import com.nearhop.nearhop.response.HostAsyncResponse;
import com.nearhop.nearhop.response.MainAsyncResponse;
import com.nearhop.nearhop.utilities.Host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;

public class ScanPortsRunnable implements Runnable {
    private Host host;
    private int startPort;
    private int stopPort;
    private int timeout;
    private final WeakReference<MainAsyncResponse> delegate;

    /**
     * Constructor to set the necessary data to perform a port scan
     *
     * @param ip        IP address
     * @param startPort Port to start scanning at
     * @param stopPort  Port to stop scanning at
     * @param timeout   Socket timeout
     * @param delegate  Called when this chunk of ports has finished scanning
     */
    public ScanPortsRunnable(Host ip, int startPort, int stopPort, int timeout, WeakReference<MainAsyncResponse> delegate) {
        this.host = ip;
        this.startPort = startPort;
        this.stopPort = stopPort;
        this.timeout = timeout;
        this.delegate = delegate;
    }

    /**
     * Starts the port scan
     */
    @Override
    public void run() {
        MainAsyncResponse activity = delegate.get();
        for (int i = startPort; i < stopPort; i++) {
            if (activity == null) {
                return;
            }

            Socket socket = new Socket();
            try {
                socket.setReuseAddress(true);
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(host.getIp(), i), timeout);
            } catch (IllegalBlockingModeException | IllegalArgumentException e) {
                activity.processFinish(e);
                continue;
            } catch (IOException e) {
                activity.processFinish(1);
                continue; // Connection failures mean that the port isn't open.
            }

            SparseArray<String> portData = new SparseArray<>();
            String data = null;
            try {
                if (i == 22) {
                    data = parseSSH(new BufferedReader(new InputStreamReader(socket.getInputStream())));
                } else if (i == 80 || i == 443 || i == 8080) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    data = parseHTTP(in, out);
                }
            } catch (IOException e) {
                activity.processFinish(e);
            } finally {
                portData.put(i, data);
                activity.processFinish(host,portData);
                activity.processFinish(1);
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // Something's really wrong if we can't close the socket...
                }
            }
        }
    }

    /**
     * Tries to determine the SSH version used.
     *
     * @param reader Reads SSH version from the connected socket
     * @return SSH banner
     * @throws java.io.IOException
     */
    private String parseSSH(BufferedReader reader) throws IOException {
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    /**
     * Tries to determine what web server is used
     *
     * @param reader Reads headers to determine server type
     * @param writer Sends HTTP request to get a response to parse
     * @return HTTP banner
     * @throws java.io.IOException
     */
    private String parseHTTP(BufferedReader reader, PrintWriter writer) throws IOException {
        writer.println("GET / HTTP/1.1\r\nHost: " + host.getIp() + "\r\n");
        char[] buffer = new char[256];
        reader.read(buffer, 0, buffer.length);
        writer.close();
        reader.close();
        String data = new String(buffer).toLowerCase();

        if (data.contains("apache") || data.contains("httpd")) {
            return "Apache";
        }

        if (data.contains("iis") || data.contains("microsoft")) {
            return "IIS";
        }

        if (data.contains("nginx")) {
            return "NGINX";
        }

        return null;
    }
}
