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
package com.codenvy.api.user.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.user.server.DtoConverter;
import org.eclipse.che.api.user.server.UserLinksInjector;
import org.eclipse.che.api.user.server.UserManager;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Provides REST API for admin user management.
 *
 * @author Anatoliy Bazko
 * @author Anton Korneta
 */
@Api(value = "/admin/user", description = "Admin user manager")
@Path("/admin/user")
public class AdminUserService extends Service {

    private final UserManager       userManager;
    private final UserLinksInjector linksInjector;

    @Inject
    public AdminUserService(UserManager userManager, UserLinksInjector linksInjector) {
        this.userManager = userManager;
        this.linksInjector = linksInjector;
    }

    /**
     * Get all users.
     *
     * @param maxItems
     *         the maximum number of users to return
     * @param skipCount
     *         the number of users to skip
     * @return list of users
     * @throws BadRequestException
     *         when {@code maxItems} or {@code skipCount} are incorrect
     * @throws ServerException
     *         when some error occurred while retrieving users
     */
    @GET
    @GenerateLink(rel = "get all users")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get all users", notes = "Get all users in the system")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 400, message = "Bad Request"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response getAll(@ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
                           @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
            throws ServerException, BadRequestException {
        try {
            final Page<? extends User> usersPage = userManager.getAll(maxItems, skipCount);
            return Response.ok()
                           .entity(usersPage.getItems(user -> linksInjector.injectLinks(DtoConverter.asDto(user), getServiceContext())))
                           .header("Link", createLinkHeader(usersPage))
                           .build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GET
    @Path("/find")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets all users whose e-mail/name contains(case insensitive) given part")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains searching result"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response find(@ApiParam("User e-mail fragment")
                            @QueryParam("emailPart")
                            String emailPart,
                            @ApiParam("User name fragment")
                            @QueryParam("namePart")
                            String namePart,
                            @ApiParam(value = "Max items")
                            @QueryParam("maxItems")
                            @DefaultValue("30")
                            int maxItems,
                            @ApiParam(value = "Skip count")
                            @QueryParam("skipCount")
                            @DefaultValue("0")
                            int skipCount) throws ServerException, BadRequestException {
        if (emailPart == null && namePart == null) {
            throw new BadRequestException("Missed user's e-mail/name part");
        }
        if (emailPart != null && namePart != null) {
            throw new BadRequestException("Expected either user's e-mail or name part, while both values received");
        }
        Page<? extends User> usersPage = namePart == null ? userManager.getByEmailPart(emailPart, maxItems, skipCount)
                                                          : userManager.getByNamePart(namePart, maxItems, skipCount);
        return Response.ok()
                       .entity(usersPage.getItems(user -> linksInjector.injectLinks(DtoConverter.asDto(user),
                                                                                    getServiceContext())))
                       .header("Link", createLinkHeader(usersPage))
                       .build();
    }

}
