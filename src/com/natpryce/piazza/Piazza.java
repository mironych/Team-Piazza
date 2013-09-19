/*
 * Copyright (c) 2011 Nat Pryce, Timo Meinen.
 *
 * This file is part of Team Piazza.
 *
 * Team Piazza is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Team Piazza is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.natpryce.piazza;

import com.natpryce.piazza.pluginConfiguration.PiazzaConfiguration;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class Piazza {

    public static final String PLUGIN_NAME = Piazza.class.getSimpleName().toLowerCase();
    public static final String PATH = "/" + PLUGIN_NAME + ".html";
    private final PluginDescriptor pluginDescriptor;
    private final PiazzaUserAdapter piazzaUserAdapter;
    private SecurityContext securityContext;
    private final PiazzaConfiguration piazzaConfiguration;
    private final SUser guestUser;

    public Piazza(SBuildServer server, ProjectManager projectManager, ProjectSettingsManager projectSettingsManager, WebControllerManager webControllerManager,
            PluginDescriptor pluginDescriptor, UserModel userManager, SecurityContext securityContext, PiazzaConfiguration piazzaConfiguration) {
        this.pluginDescriptor = pluginDescriptor;
        this.securityContext = securityContext;
        guestUser = userManager.getGuestUser();

        this.piazzaConfiguration = piazzaConfiguration;
        this.piazzaUserAdapter = new PiazzaUserAdapter(server, userManager);

        webControllerManager.registerController(PATH, new BuildMonitorController(server, projectManager, projectSettingsManager, this));
        webControllerManager.getPlaceById(PlaceId.ALL_PAGES_FOOTER).addExtension(new PiazzaLinkPageExtension(this));
    }

    public String resourcePath(String resourceName) {
        return this.pluginDescriptor.getPluginResourcesPath(resourceName);
    }

    public String version() {
        return this.pluginDescriptor.getPluginVersion();
    }

    public UserGroup userGroup() {
        return this.piazzaUserAdapter.getUserGroup();
    }

    public boolean isShowOnFailureOnly() {
        return piazzaConfiguration.isShowOnFailureOnly();
    }

    public PiazzaConfiguration getConfiguration() {
        return this.piazzaConfiguration;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public SUser getGuestUser() {
        return guestUser;
    }
}
