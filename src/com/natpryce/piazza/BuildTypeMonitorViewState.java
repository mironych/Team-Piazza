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

import java.text.DateFormat;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.ShortStatistics;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsModification;

import java.util.*;
import jetbrains.buildServer.serverSide.SBuild;

public class BuildTypeMonitorViewState {

    private final SBuildType buildType;
    private final List<String> commitMessages;
    private Build lastFinishedBuild;
    private final Build latestBuild;
    private final TestStatisticsViewState tests;
    private final InvestigationViewState investigationInfo;
    private final Set<PiazzaUser> committers;
    private boolean showOnFailureOnly;
    private final ResponsibilityEntry responsibilityEntry;
    private final UserGroup users;

    public BuildTypeMonitorViewState(SBuildType buildType, UserGroup userPictures, boolean showOnFailureOnly) {
        this.showOnFailureOnly = showOnFailureOnly;
        this.buildType = buildType;
        this.lastFinishedBuild = buildType.getLastChangesFinished();
        this.latestBuild = buildType.getLastChangesStartedBuild();
        this.commitMessages = commitMessages();
        this.users = userPictures;
        this.committers = userPictures.usersInvolvedInCommit(
                committersForBuild(),
                commitMessagesForBuild());
        this.tests = testStatistics();
        this.investigationInfo = createInvestigationState();
        this.responsibilityEntry = buildType.getResponsibilityInfo();
    }

    private Set<String> committersForBuild() {
        List<? extends VcsModification> changesSinceLastSuccessfulBuild = changesInBuild(latestBuild);

        HashSet<String> commiters = new HashSet<>();
        for (VcsModification vcsModification : changesSinceLastSuccessfulBuild) {
            String userName = vcsModification.getUserName();
            if (userName != null) {
                // if (userUnfiltered()) {
                commiters.add(userName.trim());
                //}
            }
        }
        return commiters;
    }

    private ArrayList<String> commitMessages() {
        List<? extends VcsModification> changesSinceLastSuccessfulBuild = changesInBuild(latestBuild);

        ArrayList<String> messages = new ArrayList<>();
        for (VcsModification vcsModification : changesSinceLastSuccessfulBuild) {
            //if (userUnfiltered()) {
            StringBuilder builder = new StringBuilder();
            builder.append("<b>Author:</b> " + vcsModification.getUserName());
            builder.append(" <b>Version:</b> " + vcsModification.getVersion());
            Date date = vcsModification.getVcsDate();
            builder.append(" <b>Date:</b> " + DateFormat.getInstance().format(date));
            builder.append(" <b>Changes:</b> " + vcsModification.getChangeCount());
            builder.append(" <b>Description:</b> " + vcsModification.getDescription().trim());
            messages.add(builder.toString());
            //}
        }
        return messages;
    }

    private ArrayList<String> commitMessagesForBuild() {
        List<? extends VcsModification> changesSinceLastSuccessfulBuild = changesInBuild(latestBuild);

        ArrayList<String> messages = new ArrayList<>();
        for (VcsModification vcsModification : changesSinceLastSuccessfulBuild) {
            //if (userUnfiltered()) {
            messages.add(vcsModification.getDescription().trim());
            //}
        }
        return messages;
    }

    private boolean userUnfiltered() {
        return !showOnFailureOnly || (status() == BuildStatus.FAILURE);
    }

    private TestStatisticsViewState testStatistics() {
        ShortStatistics stats = ((SBuild) latestBuild).getShortStatistics();
        return new TestStatisticsViewState(
                stats.getPassedTestCount(), stats.getFailedTestCount(), stats.getIgnoredTestCount());
    }

    private InvestigationViewState createInvestigationState() {
        ResponsibilityEntry responsibilityInfo = this.buildType.getResponsibilityInfo();
        if (responsibilityInfo.getState() != ResponsibilityEntry.State.NONE) {
            return new InvestigationViewState(responsibilityInfo.getState(), responsibilityInfo.getResponsibleUser().getDescriptiveName(), responsibilityInfo.getComment());
        } else {
            return new InvestigationViewState();
        }
    }

    @SuppressWarnings("unchecked")
    private List<? extends VcsModification> changesInBuild(Build latestBuild) {
        return latestBuild.getChanges(SelectPrevBuildPolicy.SINCE_LAST_SUCCESSFULLY_FINISHED_BUILD, true);
    }

    public String getFullName() {
        return Text.toTitleCase(buildType.getFullName());
    }

    public String getName() {
        return Text.toTitleCase(buildType.getName());
    }

    public String getBuildNumber() {
        return latestBuild.getBuildNumber();
    }

    public boolean isBuilding() {
        return !latestBuild.isFinished();
    }

    public String getActivity() {
        if (isBuilding()) {
            return ((SRunningBuild) latestBuild).getShortStatistics().getCurrentStage();
        } else {
            return status().toString();
        }
    }

    public int getCompletedPercent() {
        if (isBuilding()) {
            return ((SRunningBuild) latestBuild).getCompletedPercent();
        } else {
            return 100;
        }
    }

    public String getCombinedStatusClasses() {
        String status = status().toStringReflectingCurrentlyBuilding(isBuilding());
        if (buildType.isPaused()) {
            return status + " Paused";
        }
        return status;
    }

    public TestStatisticsViewState getTests() {
        return tests;
    }

    public InvestigationViewState getInvestigationInfo() {
        return investigationInfo;
    }

    public long getDurationSeconds() {
        Date start = latestBuild.getStartDate();
        Date finished = latestBuild.getFinishDate();
        Date end = (finished != null) ? finished : now();

        return (end.getTime() - start.getTime()) / 1000L;
    }

    private Date now() {
        return new Date();
    }

    public String getStatus() {
        return status().toString();
    }

    public BuildStatus status() {
        if (latestBuild == null) {
            return BuildStatus.UNKNOWN;
        } else if (latestBuild.getBuildStatus().isFailed()) {
            return BuildStatus.FAILURE;
        }
        if (lastFinishedBuild == null) {
            return BuildStatus.UNKNOWN;
        } else if (lastFinishedBuild.getBuildStatus().isFailed()) {
            return BuildStatus.FAILURE;
        } else {
            return BuildStatus.SUCCESS;
        }
    }

    public String getRunningBuildStatus() {
        return runningBuildStatus().toString();
    }

    public BuildStatus runningBuildStatus() {
        if (latestBuild == null) {
            return BuildStatus.UNKNOWN;
        } else if (latestBuild.getBuildStatus().isFailed()) {
            return BuildStatus.FAILURE;
        } else {
            return BuildStatus.SUCCESS;
        }
    }

    public List<String> getCommitMessages() {
        return commitMessages;
    }

    public Set<PiazzaUser> getCommitters() {
        return committers;
    }

    public List<PiazzaUser> getUsers() {
        return users.GetUsers();
    }

    public String getInvestigationStatusClass() {
        if (status() == BuildStatus.SUCCESS) {
            return "NotInvestigated";
        }

        switch (responsibilityEntry.getState()) {
            case NONE:
                return "NotInvestigated";
            case FIXED:
                return "Fixed";
            case GIVEN_UP:
                return "GivenUp";
            case TAKEN:
                return "UnderInvestigation";
        }

        return "";
    }

    private Set<String> committersForBuild(Build latestBuild) {
        List<? extends VcsModification> changesSinceLastSuccessfulBuild = changesInBuild(latestBuild);

        HashSet<String> committersForBuild = new HashSet<>();
        for (VcsModification vcsModification : changesSinceLastSuccessfulBuild) {
            String userName = vcsModification.getUserName();
            if (userName != null) {
                committersForBuild.add(userName.trim());
            }
        }
        return committersForBuild;
    }

    private ArrayList<String> commitMessagesForBuild(Build latestBuild) {
        List<? extends VcsModification> changesSinceLastSuccessfulBuild = changesInBuild(latestBuild);

        ArrayList<String> commitMessagesForbuild = new ArrayList<>();
        for (VcsModification vcsModification : changesSinceLastSuccessfulBuild) {
            commitMessagesForbuild.add(vcsModification.getDescription().trim());
        }

        return commitMessagesForbuild;
    }

    public Build getLatestBuild() {
        return latestBuild;
    }

    public boolean isBeingInvestigated() {
        return responsibilityEntry.getState().isActive()
                || responsibilityEntry.getState().isFixed()
                || responsibilityEntry.getState().isGivenUp();
    }

    public PiazzaUser getInvestigator() {
        return null;
    }

    public String getInvestigationComment() {
        if (isBeingInvestigated()) {
            return responsibilityEntry.getComment();
        }
        return "";
    }

    public boolean isQueued() {
        return buildType.isInQueue();
    }
}
