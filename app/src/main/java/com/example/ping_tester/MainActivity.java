package com.example.ping_tester;
import android.os.AsyncTask;

import java.net.InetAddress;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 1;

    private EditText ipAddressInput;
    private Button checkConnectivityButton;
    private Button pingButton;
    private Button tracerouteButton;
    private Button wifiChannelButton;
    private Button dnsLookupButton;
    private Button portScannerButton;  // New button for port scanning
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ipAddressInput = findViewById(R.id.ipAddressInput);
        checkConnectivityButton = findViewById(R.id.checkConnectivityButton);
        pingButton = findViewById(R.id.pingButton);
        tracerouteButton = findViewById(R.id.tracerouteButton);
        wifiChannelButton = findViewById(R.id.wifiChannelButton);
        dnsLookupButton = findViewById(R.id.dnsLookupButton);
        portScannerButton = findViewById(R.id.portScannerButton);  // Initialize the new button
        resultText = findViewById(R.id.resultText);

        checkConnectivityButton.setOnClickListener(view -> new CheckConnectivityTask().execute(getIpAddress()));
        pingButton.setOnClickListener(view -> new PingTask().execute(getIpAddress()));
        tracerouteButton.setOnClickListener(view -> new TracerouteTask().execute(getIpAddress()));
        wifiChannelButton.setOnClickListener(view -> new WifiChannelTask(MainActivity.this).execute());
        dnsLookupButton.setOnClickListener(view -> new DnsLookupTask().execute(getIpAddress()));
        portScannerButton.setOnClickListener(view -> new PortScannerTask().execute(getIpAddress()));
    }

    private String getIpAddress() {
        return ipAddressInput.getText().toString().trim();
    }


    private class CheckConnectivityTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String ipAddress = params[0];
            boolean isReachable = isHostReachable(ipAddress);
            return "Connectivity Check Result: " + (isReachable ? "Reachable" : "Not Reachable");
        }

        // Helper method to check if a host is reachable
        private boolean isHostReachable(String host) {
            try {
                // Use InetAddress to check if the host is reachable
                InetAddress address = InetAddress.getByName(host);
                return address.isReachable(5000); // 5000 milliseconds timeout
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            resultText.setText(result);
        }
    }


    private class PingTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String ipAddress = params[0];
            return sendPingRequest(ipAddress);
        }

        // Helper method to send a ping request
        private String sendPingRequest(String host) {
            try {
                InetAddress address = InetAddress.getByName(host);

                // Use isReachable method with a timeout to simulate a ping
                if (address.isReachable(5000)) { // 5000 milliseconds timeout
                    return "Ping Result: Success";
                } else {
                    return "Ping Result: Timeout";
                }
            } catch (IOException e) {
                return "Ping Result: Error - " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            resultText.setText(result);
        }
    }


    private class TracerouteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String ipAddress = params[0];
            return performTraceroute(ipAddress);
        }

        // Helper method to perform traceroute
        private String performTraceroute(String host) {
            StringBuilder result = new StringBuilder();

            try {
                InetAddress address = InetAddress.getByName(host);

                for (int ttl = 1; ttl <= 30; ttl++) { // Set the maximum TTL value as needed
                    try {
                        long startTime = System.currentTimeMillis();

                        // Create a socket with the specified TTL value
                        Socket socket = new Socket();
                        socket.setSoTimeout(5000); // Timeout in milliseconds
                        socket.connect(new InetSocketAddress(address, 33434), ttl);

                        long endTime = System.currentTimeMillis();

                        // Calculate and append the round-trip time
                        long roundTripTime = endTime - startTime;
                        result.append("TTL=").append(ttl).append(" RTT=").append(roundTripTime).append("ms\n");

                        socket.close();
                    } catch (IOException e) {
                        // If an exception occurs (e.g., timeout), append an error message
                        result.append("TTL=").append(ttl).append(" Error: ").append(e.getMessage()).append("\n");
                    }
                }
            } catch (IOException e) {
                result.append("Traceroute Error: ").append(e.getMessage());
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {


            resultText.setText(result);
        }
    }


    public class WifiChannelTask extends AsyncTask<Void, Void, String> {

        private Context context;

        public WifiChannelTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return getWifiChannelStrength();
        }

        // Helper method to get WiFi channel strength
        private String getWifiChannelStrength() {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int frequency = wifiInfo.getFrequency();

                // Convert frequency to channel
                int channel = convertFrequencyToChannel(frequency);

                return "WiFi Channel: " + channel + " Strength: " + wifiInfo.getRssi() + " dBm";
            } else {
                return "WiFi not enabled";
            }
        }

        // Helper method to convert WiFi frequency to channel
        private int convertFrequencyToChannel(int frequency) {
            if (frequency >= 2412 && frequency <= 2484) {
                return (frequency - 2412) / 5 + 1;
            } else if (frequency >= 5170 && frequency <= 5825) {
                return (frequency - 5170) / 5 + 34;
            } else {
                return -1; // Unsupported frequency
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Update UI or handle the result as needed
            // For example, you can display the result in a TextView


            resultText.setText(result);
        }
    }


    private class DnsLookupTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ipAddress = params[0];
            try {
                InetAddress address = InetAddress.getByName(ipAddress);
                return "DNS Lookup Result: " + address.getHostName();
            } catch (IOException e) {
                return "DNS Lookup Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            resultText.setText(result);
        }
    }

    private class PortScannerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ipAddress = params[0];
            StringBuilder result = new StringBuilder();

            // Specific ports to scan
            int[] targetPorts = {21,22, 23, 25, 53, 80, 110, 139, 143, 443, 445, 1143, 3306, 3389 };

            for (int port : targetPorts) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ipAddress, port), 1000); // Timeout in milliseconds
                    socket.close();

                    result.append("Port ").append(port).append(": Open\n");

                    // Log the result for visibility in logcat
                    Log.d("PortScannerTask", "Port " + port + ": Open");
                } catch (IOException e) {
                    // Port is closed
                    Log.d("PortScannerTask", "Port " + port + ": Closed");
                    return "Error during port scanning: " + e.getMessage();

                }
            }

            return result.toString();
        }

    @Override
        protected void onPostExecute(String result) {

        resultText.setText(result);
        }
    }

}
