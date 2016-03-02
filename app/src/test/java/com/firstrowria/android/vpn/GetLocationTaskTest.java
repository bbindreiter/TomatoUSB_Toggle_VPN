package com.firstrowria.android.vpn;

import com.firstrowria.android.vpn.network.GoogleLocation;
import com.firstrowria.android.vpn.network.WIMIPLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Created by Bernd on 3/1/2016.
 */
public class GetLocationTaskTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void googleLocationRetrieve() throws Exception {
        String location = GoogleLocation.getLocation();

        assertNotNull(location);
        assertNotEquals("", location);
    }

    @Test
    public void wimipLocationRetrieve() throws Exception {
        String location = WIMIPLocation.getLocation();

        assertNotNull(location);
        assertNotEquals("", location);
    }


}