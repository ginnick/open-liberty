/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.jsonb.fat;

import static com.ibm.ws.jsonb.fat.FATSuite.PROVIDER_YASSON;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.websphere.simplicity.config.ServerConfiguration;

import componenttest.annotation.MinimumJavaLevel;
import componenttest.annotation.Server;
import componenttest.annotation.TestServlet;
import componenttest.annotation.TestServlets;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.FATServletClient;
import web.jsonbtest.JSONBTestServlet;
import web.jsonbtest.YassonTestServlet;

@RunWith(FATRunner.class)
@MinimumJavaLevel(javaLevel = 1.8)
public class JSONBContainerTest extends FATServletClient {
    private static final String appName = "jsonbapp";
    private static final String SERVLET_PATH = "jsonbapp/JSONBTestServlet";

    @Server("com.ibm.ws.jsonb.container.fat")
    @TestServlets({
                    @TestServlet(servlet = JSONBTestServlet.class, path = appName + "/JSONBTestServlet"),
                    @TestServlet(servlet = YassonTestServlet.class, path = appName + "/YassonTestServlet")
    })
    public static LibertyServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        WebArchive app = ShrinkWrap.create(WebArchive.class, appName + ".war")
                        .addPackage("web.jsonbtest");
        ShrinkHelper.exportAppToServer(server, app);

        server.addInstalledAppForValidation(appName);
        server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stopServer();
    }

    // Test a user feature with a service component that injects JsonbProvider (from the bell)
    // as a declarative service. Validate the expected provider is used, and that it can succesfully
    // marshall/unmarshall to/from classes from the bundle.
    @Test
    public void testUserFeature() throws Exception {
        String found;
        server.resetLogMarks();
        assertNotNull(found = server.waitForStringInLogUsingMark("TEST1: JsonbProvider obtained from declarative services"));
        assertTrue(found, found.contains(PROVIDER_YASSON));
        assertNotNull(found = server.waitForStringInLogUsingMark("TEST2"));
        assertTrue(found, found.contains("success"));
        assertTrue(found, found.contains("\"Rochester\""));
        assertTrue(found, found.contains("\"Minnesota\""));
        assertTrue(found, found.contains("55901"));
        assertTrue(found, found.contains("410"));
    }

    @Test
    // Verify that the jsonb-1.0 and jsonbContainer-1.0 features can be used together to to specify Yasson
    public void testJsonAndYasson() throws Exception {
        // Add the jsonb-1.0 feature to server.xml
        ServerConfiguration config = server.getServerConfiguration();
        config.getFeatureManager().getFeatures().add("jsonb-1.0");
        server.updateServerConfiguration(config);
        server.waitForConfigUpdateInLogUsingMark(Collections.singleton(appName));

        // Run a test to verify that jsonb is still usable
        runTest(server, SERVLET_PATH, "testJsonbDeserializer&JsonbProvider=" + PROVIDER_YASSON);
    }
}
