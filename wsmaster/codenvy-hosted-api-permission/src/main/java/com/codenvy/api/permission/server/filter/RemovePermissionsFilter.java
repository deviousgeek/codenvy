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
package com.codenvy.api.permission.server.filter;

import com.codenvy.api.permission.server.InstanceParameterValidator;
import com.codenvy.api.permission.server.filter.check.DomainsPermissionsCheckers;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Restricts access to removing permissions of instance by users' setPermissions permission
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/{domain}")
public class RemovePermissionsFilter extends CheMethodInvokerFilter {
    @PathParam("domain")
    private String domain;

    @QueryParam("instance")
    private String instance;

    @QueryParam("user")
    private String user;


    @Inject
    private InstanceParameterValidator instanceValidator;

    @Inject
    private DomainsPermissionsCheckers domainsPermissionsCheckers;

    @Override
    public void filter(GenericResourceMethod genericResourceMethod, Object[] args) throws BadRequestException,
                                                                                          ForbiddenException,
                                                                                          NotFoundException,
                                                                                          ServerException {
        if (genericResourceMethod.getMethod().getName().equals("removePermissions")) {
            instanceValidator.validate(domain, instance);
            if (!domainsPermissionsCheckers.getRemoveChecker(domain).isPermitted(user, domain, instance)) {
                throw new ForbiddenException("User can't edit permissions for this instance");
            }
        }
    }
}
