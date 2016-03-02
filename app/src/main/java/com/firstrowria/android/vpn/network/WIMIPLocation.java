package com.firstrowria.android.vpn.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bernd on 3/1/2016.
 */

public class WIMIPLocation {

    public static String getLocation() {

        try {


            HttpURLConnection connection = (HttpURLConnection) new URL("http://whatismyipaddress.com/").openConnection();
            connection.setRequestProperty("Connection", "close");
            //necessary otherwise server will respond with no script access allowed
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
            connection.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String country = "";

            while ((inputLine = in.readLine()) != null) {

                inputLine = inputLine.trim();

                if (inputLine.startsWith("<tr><th style=\"font-weight:bold;color:#676769;\">Country:</th><td style=\"font-size:14px;\">") && inputLine.endsWith("</td></tr>")) {
                    country = inputLine.substring(89, inputLine.length() - 10);
                    break;
                }
            }

            connection.disconnect();
            return country;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
