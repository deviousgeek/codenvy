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
package com.codenvy.api.workspace.server.stack;

import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.core.db.DBInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.codenvy.api.workspace.server.stack.StackDomain.SEARCH;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * Loads predefined stacks and allows you to search for them to any user.
 *
 * @author Anton Korneta
 */
@Singleton
public class OnPremisesStackLoader extends StackLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesStackLoader.class);

    private final JpaStackPermissionsDao permissionsDao;

    private final boolean isOverwrite;

    @Inject
    @SuppressWarnings("unused")
    public OnPremisesStackLoader(@Named("che.stacks.storage") String stacksPath,
                                 @Named("che.stacks.images") String stackIconFolder,
                                 @Named("codenvy.default.stacks.overwrite") Boolean isOverwrite,
                                 StackDao stackDao,
                                 DBInitializer dbInitializer,
                                 JpaStackPermissionsDao permissionsDao) {
        super(stacksPath, stackIconFolder, stackDao, dbInitializer);
        this.isOverwrite = isOverwrite;
        this.permissionsDao = permissionsDao;
    }

    /**
     * Load predefined stacks with their icons to the {@link StackDao}.
     */
    @PostConstruct
    public void start() {
        if (isOverwrite) {
            super.start();
        }
    }

    protected void loadStack(StackImpl stack) {
        setIconData(stack, stackIconFolderPath);
        try {
            try {
                stackDao.update(stack);
            } catch (NotFoundException ignored) {
                stackDao.create(stack);
            }
            permissionsDao.store(new StackPermissionsImpl("*", stack.getId(), singletonList(SEARCH)));
        } catch (ServerException | ConflictException ex) {
            LOG.warn(format("Failed to load stack with id '%s' ", stack.getId()), ex.getMessage());
        }
    }

}
