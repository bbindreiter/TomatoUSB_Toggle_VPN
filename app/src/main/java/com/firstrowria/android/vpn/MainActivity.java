package com.firstrowria.android.vpn;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firstrowria.android.vpn.adapter.VPNServerAdapter;
import com.firstrowria.android.vpn.vo.VPNServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int VPN_CONNECT_STATE_UNKNOWN = 0;
    private static final int VPN_CONNECT_STATE_CONNECTED = 1;
    private static final int VPN_CONNECT_STATE_DISCONNECTED = 2;

    private static final String WIFI_SSID = "";
    private static final String TOMATO_AUTH = "";
    private static final String TOMATO_START_VPN = "start";
    private static final String TOMATO_STOP_VPN = "stop";
    private static final Pattern LOAD_PATTERN = Pattern.compile("\\(\\d+%\\)");

    private TextView ssidTextView = null;
    private TextView wifiTextView = null;

    private TextView googleCountryTextView = null;
    private TextView wimiaCountryTextView = null;

    private ListView serverListView = null;
    private MenuItem vpnMenuItem = null;
    private ProgressDialog progressDialog = null;

    private int vpnConnectState = VPN_CONNECT_STATE_UNKNOWN;
    private String tomatoHttpId = "";

    private ConnectivityManager connManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */








        ssidTextView = (TextView)findViewById(R.id.ssidTextView);
        wifiTextView = (TextView)findViewById(R.id.wifiTextView);

        googleCountryTextView = (TextView)findViewById(R.id.googleCountryTextView);
        wimiaCountryTextView = (TextView)findViewById(R.id.wimiaCountryTextView);

        serverListView = (ListView)findViewById(R.id.serverListView);

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    protected void onStart() {
        super.onStart();
        loadInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        vpnMenuItem = menu.getItem(0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload) {
            loadInfo();
            return true;
        }
        else if (id == R.id.action_vpn) {

            if (vpnConnectState == VPN_CONNECT_STATE_CONNECTED) {

                progressDialog =  ProgressDialog.show(this, "Disconnect", "Please wait...", true);
                progressDialog.show();

                (new ConnectVPNServerTask()).execute(TOMATO_STOP_VPN);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadInfo() {

        vpnConnectState = VPN_CONNECT_STATE_UNKNOWN;

        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
        String ssid = mWifi.getExtraInfo().replace("\"", "");

        ssidTextView.setText(ssid);
        googleCountryTextView.setText("");
        wimiaCountryTextView.setText("");

        if (mWifi.getType() == ConnectivityManager.TYPE_WIFI && mWifi.isAvailable() && mWifi.isConnected() && WIFI_SSID.equals(ssid))
        {
            (new GetVPNConnectStatusTask()).execute();
            (new GetVPNServersTask()).execute();

            wifiTextView.setTextColor(Color.BLACK);
        }
        else
        {
            wifiTextView.setTextColor(Color.RED);
        }

        (new GetVPNServersTask()).execute();
        (new GetGoogleLocationTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new GetWIMIALocationTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class GetGoogleLocationTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {

            try {
                Log.d("MainActivity", "Load google location");

                HttpURLConnection connection = (HttpURLConnection) new URL("http://mylocationtest.appspot.com/").openConnection();
                connection.setRequestProperty("Connection", "close");
                connection.setUseCaches(false);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                String country = "";

                while ((inputLine = in.readLine()) != null) {

                    if (inputLine.startsWith("<h3> Country : ") && inputLine.endsWith("</h3>")) {
                        country = inputLine.substring(15, inputLine.length() - 5);
                        Log.d("MainActivity", "Google Country: " + country);
                        break;
                    }
                }

                connection.disconnect();
                return country;
            }
            catch(Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        protected void onPostExecute(String result) {
            //showDialog("Downloaded " + result + " bytes");
            googleCountryTextView.setText(result);
        }
    }

    private class GetWIMIALocationTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {

            try {
                Log.d("MainActivity", "Load wimia location");

                HttpURLConnection connection = (HttpURLConnection) new URL("http://whatismyipaddress.com/").openConnection();
                connection.setRequestProperty("Connection", "close");
                connection.setUseCaches(false);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                String country = "";

                while ((inputLine = in.readLine()) != null) {

                    inputLine = inputLine.trim();

                    if (inputLine.startsWith("<tr><th style=\"font-weight:bold;color:#676769;\">Country:</th><td style=\"font-size:14px;\">") && inputLine.endsWith("</td></tr>")) {
                        country = inputLine.substring(89, inputLine.length() - 10);
                        Log.d("MainActivity", "WIMIA Country: " + country);
                        break;
                    }
                }

                connection.disconnect();
                return country;
            }
            catch(Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        protected void onPostExecute(String result) {
            //showDialog("Downloaded " + result + " bytes");
            wimiaCountryTextView.setText(result);
        }
    }

    private class GetVPNConnectStatusTask extends AsyncTask<Void, Void, Integer> {
        protected Integer doInBackground(Void... params) {

            int state = VPN_CONNECT_STATE_UNKNOWN;

            try {

                HttpURLConnection connection = (HttpURLConnection) new URL("http://192.168.0.1/vpn-client.asp").openConnection();
                connection.setRequestProperty("Authorization", "basic " + Base64.encodeToString(TOMATO_AUTH.getBytes(), Base64.URL_SAFE));

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {

                    if (inputLine.startsWith("vpn1up = parseInt(")) {
                        state = inputLine.equals("vpn1up = parseInt('1');") ? VPN_CONNECT_STATE_CONNECTED : VPN_CONNECT_STATE_DISCONNECTED;
                        Log.d("MainActivity", "tomato vpn status is: " + state + " // " + inputLine);
                        break;
                    }
                    else if (inputLine.contains("'http_id': '")) {

                        int index = inputLine.indexOf("'http_id': '");
                        tomatoHttpId = inputLine.substring(index + 12, inputLine.length() - 2);
                        Log.d("MainActivity", "tomato httpd id is: " + tomatoHttpId);
                    }
                }

                in.close();
                connection.disconnect();
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return state;
        }

        protected void onPostExecute(Integer state) {
            vpnConnectState = state;

            if (vpnConnectState == VPN_CONNECT_STATE_CONNECTED) {
                vpnMenuItem.setEnabled(true);
                vpnMenuItem.setIcon(R.drawable.ic_vpn_key_connected_24dp);
            } else {
                vpnMenuItem.setEnabled(false);
                vpnMenuItem.setIcon(R.drawable.ic_vpn_key_disabled_24dp);
            }
        }
    }

    private class SetVPNServerTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... params) {

            boolean saved = false;

            try {

                HttpURLConnection connection = (HttpURLConnection) new URL("http://192.168.0.1/tomato.cgi").openConnection();
                connection.setRequestProperty("Authorization", "basic " + Base64.encodeToString(TOMATO_AUTH.getBytes(), Base64.URL_SAFE));

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write("_ajax=1&_nextpage=vpn-client.asp&_nextwait=5&_service=&vpn_client_eas=&vpn_client1_bridge=1&vpn_client1_nat=1&vpn_client1_rgw=1&vpn_client1_userauth=1&vpn_client1_useronly=1&vpn_client1_tlsremote=0&vpn_client1_nopull=0&vpn_client1_route=0&vpn_client1_routing_val=&vpn_client1_if=tun&vpn_client1_br=br0&vpn_client1_proto=udp&vpn_client1_addr=" + params[0] + "&vpn_client1_port=1194&vpn_client1_firewall=auto&vpn_client1_crypt=tls&vpn_client1_username=vpn88449241&vpn_client1_password=bkoM23q53G&vpn_client1_hmac=-1&vpn_client1_local=10.8.0.2&vpn_client1_remote=10.8.0.1&vpn_client1_nm=255.255.255.0&vpn_client1_poll=0&vpn_client1_gw=&vpn_client1_adns=3&vpn_client1_cipher=BF-CBC&vpn_client1_comp=-1&vpn_client1_reneg=-1&vpn_client1_retry=30&vpn_client1_cn=&vpn_client1_custom=persist-key%0Apersist-tun%0Atls-client%0Aremote-cert-tls%20server&vpn_client1_static=&vpn_client1_ca=-----BEGIN%20CERTIFICATE-----%0AMIIDljCCAv%2BgAwIBAgIJANMiwLWxktowMA0GCSqGSIb3DQEBBQUAMIGPMQswCQYD%0AVQQGEwJSTzEMMAoGA1UECBMDQlVDMRIwEAYDVQQHEwlCdWNoYXJlc3QxDzANBgNV%0ABAoTBlZQTi5BQzEPMA0GA1UECxMGVlBOLkFDMQ8wDQYDVQQDEwZWUE4uQUMxDzAN%0ABgNVBCkTBlZQTi5BQzEaMBgGCSqGSIb3DQEJARYLaW5mb0B2cG4uYWMwHhcNMTIx%0AMTI2MTI0NDMzWhcNMjIxMTI0MTI0NDMzWjCBjzELMAkGA1UEBhMCUk8xDDAKBgNV%0ABAgTA0JVQzESMBAGA1UEBxMJQnVjaGFyZXN0MQ8wDQYDVQQKEwZWUE4uQUMxDzAN%0ABgNVBAsTBlZQTi5BQzEPMA0GA1UEAxMGVlBOLkFDMQ8wDQYDVQQpEwZWUE4uQUMx%0AGjAYBgkqhkiG9w0BCQEWC2luZm9AdnBuLmFjMIGfMA0GCSqGSIb3DQEBAQUAA4GN%0AADCBiQKBgQDZ6bE44ampTNnDBqsB/J5tS41UHTkxk8sswATe7R32%2Bmn87hwLzfhz%0AuRP4sRPLKTcM%2B7zGgfgyMU2wvF2N1%2B4Vyr%2BBxTRNKZIuGvoUwqFvOU7kGMrSW4Hx%0AR2Z1dr%2BIVhtDGYg3C3zHZcRDOMZZzufG1rxl0rWBF8atkkvwhWwIRQIDAQABo4H3%0AMIH0MB0GA1UdDgQWBBQE14ZZ8yiLb6jsy9j8168FwkhNEzCBxAYDVR0jBIG8MIG5%0AgBQE14ZZ8yiLb6jsy9j8168FwkhNE6GBlaSBkjCBjzELMAkGA1UEBhMCUk8xDDAK%0ABgNVBAgTA0JVQzESMBAGA1UEBxMJQnVjaGFyZXN0MQ8wDQYDVQQKEwZWUE4uQUMx%0ADzANBgNVBAsTBlZQTi5BQzEPMA0GA1UEAxMGVlBOLkFDMQ8wDQYDVQQpEwZWUE4u%0AQUMxGjAYBgkqhkiG9w0BCQEWC2luZm9AdnBuLmFjggkA0yLAtbGS2jAwDAYDVR0T%0ABAUwAwEB/zANBgkqhkiG9w0BAQUFAAOBgQA3jbSdw0ShUj3yV9LlW//MutZpQc67%0AbXexaRrLvbDgFywOE5jdIeYMQzn3T4/Uj17B6qHZjFwWIkiE7Q2a1ShmHwFFq4pS%0ANfDo/CDdcNRbQU7r8T4XorwVYGPmdxRVoZtejOb510lY5AP6P0pSBshC3zjTLpPi%0AqRI4JvSeZc4/ww%3D%3D%0A-----END%20CERTIFICATE-----&vpn_client1_crt=&vpn_client1_key=&vpn_client2_bridge=1&vpn_client2_nat=1&vpn_client2_rgw=0&vpn_client2_userauth=0&vpn_client2_useronly=0&vpn_client2_tlsremote=0&vpn_client2_nopull=0&vpn_client2_route=0&vpn_client2_routing_val=&vpn_client2_if=tun&vpn_client2_br=br0&vpn_client2_proto=udp&vpn_client2_addr=&vpn_client2_port=1194&vpn_client2_firewall=auto&vpn_client2_crypt=tls&vpn_client2_username=&vpn_client2_password=&vpn_client2_hmac=-1&vpn_client2_local=10.8.0.2&vpn_client2_remote=10.8.0.1&vpn_client2_nm=255.255.255.0&vpn_client2_poll=0&vpn_client2_gw=&vpn_client2_adns=0&vpn_client2_cipher=default&vpn_client2_comp=adaptive&vpn_client2_reneg=-1&vpn_client2_retry=30&vpn_client2_cn=&vpn_client2_custom=&vpn_client2_static=&vpn_client2_ca=&vpn_client2_crt=&vpn_client2_key=&_http_id=" + tomatoHttpId);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    saved = inputLine.equals("@msg:Settings saved.");
                    if (saved)
                        Log.d("MainActivity", "tomato vpn server set to: " + params[0]);
                    else
                        Log.d("MainActivity", inputLine);
                }

                in.close();
                connection.disconnect();
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return saved;
        }

        protected void onPostExecute(Boolean saved) {

            if (saved) {
                (new ConnectVPNServerTask()).execute(TOMATO_START_VPN);
            } else {

                if (progressDialog != null)
                    progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "Can't set chosen VPN Server", Toast.LENGTH_LONG).show();
                loadInfo();
            }
        }
    }

    private class ConnectVPNServerTask extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... params) {

            int state = VPN_CONNECT_STATE_UNKNOWN;

            try {

                HttpURLConnection connection = (HttpURLConnection) new URL("http://192.168.0.1/service.cgi").openConnection();
                connection.setRequestProperty("Authorization", "basic " + Base64.encodeToString(TOMATO_AUTH.getBytes(), Base64.URL_SAFE));

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write("_redirect=vpn-client.asp&_sleep=5&_service=vpnclient1-" + params[0] + "&_http_id=" + tomatoHttpId);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                //Log.d("MainActivity", "response code: " + connection.getResponseCode());

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {

                    //Log.d("MainActivity", inputLine);

                    if (inputLine.startsWith("vpn1up = parseInt(")) {
                        state = inputLine.equals("vpn1up = parseInt('1');") ? VPN_CONNECT_STATE_CONNECTED : VPN_CONNECT_STATE_DISCONNECTED;
                        Log.d("MainActivity", "tomato vpn status is: " + state + " // " + inputLine + " // " + params[0]);
                        break;
                    }
                }


                in.close();
                connection.disconnect();


                if (params[0].equals(TOMATO_START_VPN)) {
                   Log.d("MainActivity", "sleep 15 sec after connect");
                   Thread.sleep(15000);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return state;
        }

        protected void onPostExecute(Integer state) {

            vpnConnectState = state;

            if (vpnConnectState == VPN_CONNECT_STATE_DISCONNECTED) {
                vpnMenuItem.setEnabled(false);
                vpnMenuItem.setIcon(R.drawable.ic_vpn_key_disabled_24dp);

            } else {
                vpnMenuItem.setEnabled(true);
                vpnMenuItem.setIcon(R.drawable.ic_vpn_key_connected_24dp);
            }

            if (progressDialog != null)
                progressDialog.dismiss();

            Log.d("MainActivity", "update location");
            (new GetGoogleLocationTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            (new GetWIMIALocationTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class GetVPNServersTask extends AsyncTask<Void, Void, ArrayList<VPNServer>> {
        protected ArrayList<VPNServer> doInBackground(Void... params) {

            ArrayList<VPNServer> servers = new ArrayList<>();

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader((new URL("https://vpn.ac/status")).openStream()));

                String inputLine;
                VPNServer vpnServer = null;
                boolean inTable = false;
                boolean inTr = false;
                int tdCount = 0;

                while ((inputLine = in.readLine()) != null) {

                    inputLine = inputLine.trim();
                    if (inputLine.startsWith("<tbody>")) {
                        inTable = true;
                    }
                    else if (inTable && inputLine.startsWith("<tr")) {
                        inTr = true;
                        tdCount = 0;
                    }
                    else if (inTable && inTr && inputLine.startsWith("<td>") && inputLine.endsWith("</td>")) {
                        String data = inputLine.substring(4, inputLine.length() - 5);

                        if (tdCount == 0) {
                            vpnServer = new VPNServer();
                            vpnServer.host = data;
                        }
                        else if (tdCount == 1) {
                            vpnServer.country = data;
                        }
                        else if (tdCount == 2) {
                            vpnServer.load = data;

                            try {
                                if (!data.contains("very low"))
                                {
                                    Matcher matcher = LOAD_PATTERN.matcher(data);

                                    if (matcher.find())
                                    {
                                        String loadPercentage = matcher.group(0);
                                        vpnServer.loadPercentage = Integer.parseInt(loadPercentage.substring(1, loadPercentage.length() - 2));

                                        if (vpnServer.loadPercentage > 25)
                                            vpnServer.loadPercentage = 25;
                                        else if (vpnServer.loadPercentage == 0)
                                            vpnServer.loadPercentage = 1;
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }


                            servers.add(vpnServer);
                        }

                        tdCount++;
                    }
                    else if (inputLine.startsWith("</tr>")) {
                        inTr = false;
                    }
                    else if (inputLine.startsWith("</tbody>")) {
                        inTable = false;
                    }
                }

                in.close();

            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return servers;
        }

        protected void onPostExecute(final ArrayList<VPNServer> servers) {

            VPNServerAdapter vpnServerAdapter = new VPNServerAdapter(getApplicationContext(), servers);
            serverListView.setAdapter(vpnServerAdapter);
            serverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    if (vpnConnectState == VPN_CONNECT_STATE_CONNECTED) {
                        Toast.makeText(getApplicationContext(), "You're already connected to a VPN. If you want to connect to a different VPN Server you have to disconnect first", Toast.LENGTH_LONG).show();
                    }
                    else if (vpnConnectState == VPN_CONNECT_STATE_DISCONNECTED) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Connect to " + servers.get(position).host + "?").setTitle(servers.get(position).country);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                progressDialog =  ProgressDialog.show(MainActivity.this, "Connect", "Please wait...", true);
                                progressDialog.show();

                                (new SetVPNServerTask()).execute(servers.get(position).host);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    //Log.d("Activity", "item click" + position);
                }
            });


        }
    }
}
