/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;
import static com.mongodb.util.MyAsserts.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersData extends BaseTest {

    @BeforeClass
    public void prepare() throws IOException {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company1", "11", "1")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company1", "11", "1")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f2", "l2", "company2", "11", "1")
                        .withDate("2013-11-01").build());

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionStartedEvent("user3@gmail.com", "ws2", "ide", "2").withDate("2013-11-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3@gmail.com", "ws2", "ide", "2").withDate("2013-11-01")
                        .withTime("19:02:00").build());

        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "s", "", "")
                        .withDate("2013-11-01").withTime("20:01:00").build());
        events.add(Event.Builder.createProjectDeployedEvent("user4@gmail.com", "ws1", "s", "", "", "")
                        .withDate("2013-11-01").withTime("20:02:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1@gmail.com", "", "", "", "", "", "")
                        .withDate("2013-11-01").withTime("20:03:00").build());
        events.add(Event.Builder.createRunStartedEvent("user4@gmail.com", "ws1", "", "")
                        .withDate("2013-11-01").withTime("20:04:00").build());
        events.add(Event.Builder.createDebugStartedEvent("user4@gmail.com", "ws1", "", "")
                        .withDate("2013-11-01").withTime("20:06:00").build());

        events.add(
                Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type").withDate("2013-11-01")
                     .withTime("19:08:00").build());
        events.add(
                Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type").withDate("2013-11-01")
                     .withTime("19:10:00").build());


        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testusersdata-sessions");
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, "testusersdata");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);

        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testusersdata");
        PigServer.execute(ScriptType.USERS_STATISTICS, params);

        Parameters.STORAGE_TABLE.put(params, MetricType.USERS_PROFILES_LIST.name().toLowerCase());
        PigServer.execute(ScriptType.USERS_UPDATE_PROFILES, params);
    }

    @Test
    public void testSingleProfile() throws Exception {
        Map<String, String> context = Utils.newContext();

        Metric metric = new TestUsersStatisticsList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 4);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            assertEquals(all.size(), 9);

            String user = all.get("_id").getAsString();
            switch (user) {
                case "user1@gmail.com":
                    assertEquals(all.get("projects").getAsString(), "1");
                    assertEquals(all.get("deploys").getAsString(), "0");
                    assertEquals(all.get("builds").getAsString(), "0");
                    assertEquals(all.get("debugs").getAsString(), "0");
                    assertEquals(all.get("runs").getAsString(), "0");
                    assertEquals(all.get("factories").getAsString(), "1");
                    assertEquals(all.get("time").getAsString(), "300");
                    assertEquals(all.get("sessions").getAsString(), "1");
                    break;

                case "user2@gmail.com":
                    assertEquals(all.get("projects").getAsString(), "0");
                    assertEquals(all.get("runs").getAsString(), "1");
                    assertEquals(all.get("deploys").getAsString(), "0");
                    assertEquals(all.get("debugs").getAsString(), "0");
                    assertEquals(all.get("builds").getAsString(), "0");
                    assertEquals(all.get("factories").getAsString(), "0");
                    assertEquals(all.get("time").getAsString(), "0");
                    assertEquals(all.get("sessions").getAsString(), "0");
                    break;

                case "user3@gmail.com":
                    assertEquals(all.get("sessions").getAsString(), "1");
                    assertEquals(all.get("projects").getAsString(), "0");
                    assertEquals(all.get("time").getAsString(), "120");
                    assertEquals(all.get("deploys").getAsString(), "0");
                    assertEquals(all.get("builds").getAsString(), "0");
                    assertEquals(all.get("debugs").getAsString(), "0");
                    assertEquals(all.get("runs").getAsString(), "0");
                    assertEquals(all.get("factories").getAsString(), "0");
                    break;

                case "user4@gmail.com":
                    assertEquals(all.get("projects").getAsString(), "0");
                    assertEquals(all.get("deploys").getAsString(), "1");
                    assertEquals(all.get("builds").getAsString(), "1");
                    assertEquals(all.get("debugs").getAsString(), "1");
                    assertEquals(all.get("runs").getAsString(), "1");
                    assertEquals(all.get("factories").getAsString(), "0");
                    assertEquals(all.get("time").getAsString(), "0");
                    assertEquals(all.get("sessions").getAsString(), "0");
                    break;

                default:
                    fail("unknown user " + user);
                    break;
            }
        }
    }

    @Test
    public void testUsersTimeInWorkspaces() throws Exception {
        Map<String, String> context = Utils.newContext();

        TestUsersTimeInWorkspaces metric = new TestUsersTimeInWorkspaces();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 2);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            String ws = all.get("_id").getAsString();

            switch (ws) {
                case "ws1":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get("sessions").getAsString(), "1");
                    assertEquals(all.get("time").getAsString(), "300");
                    break;

                case "ws2":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get("sessions").getAsString(), "1");
                    assertEquals(all.get("time").getAsString(), "120");
                    break;

                default:
                    fail("unknown ws " + ws);
                    break;

            }
        }
    }

    @Test
    public void testUsersTimeInWorkspacesWithFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user1@gmail.com");

        TestUsersTimeInWorkspaces metric = new TestUsersTimeInWorkspaces();
        ListValueData valueData = (ListValueData)metric.getValue(context);

        assertEquals(valueData.size(), 1);

        List<ValueData> items = valueData.getAll();
        MapValueData entry = (MapValueData)items.get(0);

        assertEquals(entry.getAll().get("sessions").getAsString(), "1");
        assertEquals(entry.getAll().get("time").getAsString(), "300");
        assertEquals(entry.getAll().get("_id").getAsString(), "ws1");

    }

    @Test
    public void testUsersStatisticsByCompany() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER_COMPANY.put(context, "company1");

        TestUsersStatisticsList metric = new TestUsersStatisticsList();
        ListValueData valueData = (ListValueData)metric.getValue(context);

        assertEquals(valueData.size(), 2);
    }

    private class TestUsersTimeInWorkspaces extends UsersTimeInWorkspacesList {

        @Override
        public String getStorageCollectionName() {
            return "testusersdata-sessions";
        }
    }

    private class TestUsersStatisticsList extends UsersStatisticsList {

        @Override
        public String getStorageCollectionName() {
            return "testusersdata";
        }
    }
}
