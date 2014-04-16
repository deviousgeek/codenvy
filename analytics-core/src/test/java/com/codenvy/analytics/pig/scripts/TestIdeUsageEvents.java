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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ide_usage.AbstractIdeUsage;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestIdeUsageEvents extends BaseTest {

    private static final String COLLECTION = MetricType.IDE_USAGES.toString().toLowerCase();

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(
                Event.Builder.createIDEUsageEvent("user1", "ws1", "action1", "src1", "project1", "type1", "p1=v1,p2=v2")
                             .withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user2", null, null, null, null, null, null)
                                .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createIDEUsageEvent("user3", null, AbstractIdeUsage.FILE_DELETE, null, null, null, null)
                             .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createIDEUsageEvent("user4", null, AbstractIdeUsage.FILE_DELETE, null, null, null, null)
                             .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createIDEUsageEvent("user5", null, AbstractIdeUsage.FILE_DELETE, null, null, null, null)
                             .withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);

        pigServer.execute(ScriptType.IDE_USAGE_EVENTS, builder.build());
    }

    @Test
    public void scriptShouldStoreNotNullParameters() throws Exception {
        DBObject filter = new BasicDBObject("user", "user1");

        DBCollection collection = mongoDb.getCollection(COLLECTION);
        DBCursor cursor = collection.find(filter);

        assertEquals(1, cursor.size());

        DBObject dbObject = cursor.next();
        assertEquals(11, dbObject.keySet().size());
        assertEquals("user1", dbObject.get("user"));
        assertEquals("ws1", dbObject.get("ws"));
        assertEquals("action1", dbObject.get("action"));
        assertEquals("src1", dbObject.get("source"));
        assertEquals("project1", dbObject.get("project"));
        assertEquals("type1", dbObject.get("project_type"));
        assertEquals("v1", dbObject.get("p1"));
        assertEquals("v2", dbObject.get("p2"));
        assertNotNull(dbObject.get("_id"));
        assertNotNull(dbObject.get("date"));
        assertNotNull(dbObject.get("ide"));
    }

    @Test
    public void scriptShouldNotStoreNullParameters() throws Exception {
        DBObject filter = new BasicDBObject("user", "user2");

        DBCollection collection = mongoDb.getCollection(COLLECTION);
        DBCursor cursor = collection.find(filter);

        assertEquals(1, cursor.size());

        DBObject dbObject = cursor.next();
        assertEquals(5, dbObject.keySet().size());
        assertEquals("user2", dbObject.get("user"));
        assertEquals("default", dbObject.get("ws"));
        assertNotNull(dbObject.get("_id"));
        assertNotNull(dbObject.get("date"));
        assertNotNull(dbObject.get("ide"));
    }

    @Test
    public void testSingleActions() throws Exception {
        Metric metric = new TestedIdeUsages(AbstractIdeUsage.FILE_DELETE, new String[]{AbstractIdeUsage.FILE_DELETE});
        Assert.assertEquals(LongValueData.valueOf(3), metric.getValue(Context.EMPTY));
    }


    // -----------------------> Tested metrics

    private class TestedIdeUsages extends AbstractIdeUsage {
        protected TestedIdeUsages(String metricName, String[] actions) {
            super(metricName, actions);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
