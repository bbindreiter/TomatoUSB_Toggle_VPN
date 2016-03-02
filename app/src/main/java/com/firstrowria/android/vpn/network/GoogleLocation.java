package com.firstrowria.android.vpn.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bernd on 3/1/2016.
 */

public class GoogleLocation {

    public static String getLocation() {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://mylocationtest.appspot.com/").openConnection();
            connection.setRequestProperty("Connection", "close");
            connection.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String country = "";

            while ((inputLine = in.readLine()) != null) {

                if (inputLine.startsWith("<h3> Country : ") && inputLine.endsWith("</h3>")) {
                    country = inputLine.substring(15, inputLine.length() - 5);
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
