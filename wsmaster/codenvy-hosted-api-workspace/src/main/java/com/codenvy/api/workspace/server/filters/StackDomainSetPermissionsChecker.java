/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.api.workspace.server.filters;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.permission.server.filter.check.DefaultSetPermissionsChecker;
import com.codenvy.api.permission.server.filter.check.SetPermissionsChecker;
import com.codenvy.api.permission.shared.model.Permissions;
import com.codenvy.api.workspace.server.stack.StackDomain;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Stack domain specific permission checker.
 *
 * @author Anton Korneta
 */
@Singleton
public class StackDomainSetPermissionsChecker implements SetPermissionsChecker {

    private final DefaultSetPermissionsChecker defaultChecker;

    @Inject
    public StackDomainSetPermissionsChecker(DefaultSetPermissionsChecker defaultChecker) {
        this.defaultChecker = defaultChecker;
    }

    @Override
    public boolean isPermitted(Permissions permissions) {
        try {
            EnvironmentContext.getCurrent()
                              .getSubject()
                              .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
            return (permissions.getUserId().equals("*") && permissions.getActions().contains(StackDomain.SEARCH))
                   || defaultChecker.isPermitted(permissions);
        } catch (ForbiddenException ignored) {
        }
        return false;
    }

}
