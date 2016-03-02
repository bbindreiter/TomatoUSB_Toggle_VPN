package com.firstrowria.android.vpn.network;

import com.firstrowria.android.vpn.vo.VPNServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Bernd on 3/1/2016.
 */

public class VPNACServers {

    private static final Pattern LOAD_PATTERN = Pattern.compile("\\(\\d+%\\)");

    public static ArrayList<VPNServer> retrieve() {

        ArrayList<VPNServer> servers = new ArrayList<>();

        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection)(new URL("https://vpn.ac/status").openConnection());
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String inputLine;
            VPNServer vpnServer = null;
            boolean inTable = false;
            boolean inTr = false;
            int tdCount = 0;

            while ((inputLine = in.readLine()) != null) {

                inputLine = inputLine.trim();
                if (inputLine.startsWith("<tbody>")) {
                    inTable = true;
                } else if (inTable && inputLine.startsWith("<tr")) {
                    inTr = true;
                    tdCount = 0;
                } else if (inTable && inTr && inputLine.startsWith("<td>") && inputLine.endsWith("</td>")) {
                    String data = inputLine.substring(4, inputLine.length() - 5);

                    if (tdCount == 0) {
                        vpnServer = new VPNServer();
                        vpnServer.host = data;
                    } else if (tdCount == 1) {
                        vpnServer.country = data;
                    } else if (tdCount == 2) {
                        vpnServer.load = data;

                        try {
                            if (!data.contains("very low")) {
                                Matcher matcher = LOAD_PATTERN.matcher(data);

                                if (matcher.find()) {
                                    String loadPercentage = matcher.group(0);
                                    vpnServer.loadPercentage = Integer.parseInt(loadPercentage.substring(1, loadPercentage.length() - 2));

                                    if (vpnServer.loadPercentage > 25)
                                        vpnServer.loadPercentage = 25;
                                    else if (vpnServer.loadPercentage == 0)
                                        vpnServer.loadPercentage = 1;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        servers.add(vpnServer);
                    }

                    tdCount++;
                } else if (inputLine.startsWith("</tr>")) {
                    inTr = false;
                } else if (inputLine.startsWith("</tbody>")) {
                    inTable = false;
                }
            }

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return servers;
    }
}
