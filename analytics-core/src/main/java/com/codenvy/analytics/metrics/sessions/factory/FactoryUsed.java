/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed({"any"})
@RequiredFilter(MetricFilter.FACTORY)
@OmitFilters({MetricFilter.USER, MetricFilter.WS})
public class FactoryUsed extends AbstractCount {

    public FactoryUsed() {
        super(MetricType.FACTORY_USED, MetricType.FACTORIES_ACCEPTED_LIST, FACTORY);
    }

    @Override
    public String getDescription() {
        return "The number of factory usage";
    }
}