package com.firstrowria.android.vpn;

import com.firstrowria.android.vpn.network.VPNACServers;
import com.firstrowria.android.vpn.vo.VPNServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Created by Bernd on 3/1/2016.
 */
public class GetVPNServersTaskTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void countVPNServers() throws Exception {

        String versionProperty = System.getProperty("java.version");
        int pos = versionProperty.indexOf('.');
        pos = versionProperty.indexOf('.', pos+1);
        double javaVersion = Double.parseDouble(versionProperty.substring(0, pos));

        //java 1.8 is required for connecting to vpn.ac server otherwise ssl handshake exception
        assumeTrue("Java 1.8 required", javaVersion >= 8);
        assertTrue("Size: " + VPNACServers.retrieve().size(), VPNACServers.retrieve().size() > 0);
    }

    @Test
    public void equalsVPNServer() throws Exception {

        String country = "USA";
        String host = "subdomain.domain.com";
        String load = "30%";
        int loadPercentage = 30;

        VPNServer server1 = new VPNServer();
        server1.country = country;
        server1.host = host;
        server1.loadPercentage = loadPercentage;
        server1.load = load;

        assertEquals(server1.country, country);
        assertEquals(server1.host, host);
        assertEquals(server1.loadPercentage, loadPercentage);
        assertEquals(server1.load, load);

    }

}