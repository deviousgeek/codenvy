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
package com.codenvy.api.permission.server.filter.check;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Represents a set of domain-specific permissions checkers.
 *
 * @author Anton Korneta
 */
@Singleton
public class DomainsPermissionsCheckers {

    private final Map<String, SetPermissionsChecker>    domain2setPermissionsChecker;
    private final DefaultSetPermissionsChecker          defaultSetPermissionsChecker;
    private final Map<String, RemovePermissionsChecker> domain2removePermissionsChecker;
    private final DefaultRemovePermissionsChecker       defaultRemovePermissionsChecker;

    @Inject
    public DomainsPermissionsCheckers(@Named("set.permissions.checkers")
                                      Map<String, SetPermissionsChecker> domain2setPermissionsChecker,
                                      DefaultSetPermissionsChecker defaultPermissionsChecker,
                                      @Named("remove.permissions.checkers")
                                      Map<String, RemovePermissionsChecker> domain2removePermissionsChecker,
                                      DefaultRemovePermissionsChecker defaultRemovePermissionsChecker) {
        this.domain2setPermissionsChecker = domain2setPermissionsChecker;
        this.defaultSetPermissionsChecker = defaultPermissionsChecker;
        this.domain2removePermissionsChecker = domain2removePermissionsChecker;
        this.defaultRemovePermissionsChecker = defaultRemovePermissionsChecker;
    }

    public SetPermissionsChecker getSetChecker(String domain) {
        if (domain2setPermissionsChecker.containsKey(domain)) {
            return domain2setPermissionsChecker.get(domain);
        }
        return defaultSetPermissionsChecker;
    }

    public RemovePermissionsChecker getRemoveChecker(String domain) {
        if (domain2removePermissionsChecker.containsKey(domain)) {
            return domain2removePermissionsChecker.get(domain);
        }
        return defaultRemovePermissionsChecker;
    }

}
