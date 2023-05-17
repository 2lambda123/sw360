/*
 * Copyright Siemens AG, 2017-2019.
 * Copyright Bosch Software Innovations GmbH, 2017-2018.
 * Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.rest.resourceserver.report;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.PaginationData;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService.Iface;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SW360ReportService {

    public ByteBuffer getProjectBuffer(User user, ProjectService.Iface client, boolean extendedByReleases)
            throws TException, SW360Exception {
        List<Project> listOfProjects = getProjectList(client, user);
        ByteBuffer buffer = client.getReportDataStream(listOfProjects, user, "projects", extendedByReleases);
        return buffer;
    }

    private List<Project> getProjectList(ProjectService.Iface client, User user) throws TException {
        int total = client.getMyAccessibleProjectCounts(user);
        PaginationData pageData = new PaginationData();
        pageData.setAscending(true);
        Map<PaginationData, List<Project>> pageDtToProjects;
        Set<Project> projects = new HashSet<>();
        int displayStart = 0;
        int rowsPerPage = 500;
        while (0 < total) {
            pageData.setDisplayStart(displayStart);
            pageData.setRowsPerPage(rowsPerPage);
            displayStart = displayStart + rowsPerPage;
            pageDtToProjects = client.getAccessibleProjectsSummaryWithPagination(user, pageData);
            projects.addAll(pageDtToProjects.entrySet().iterator().next().getValue());
            total = total - rowsPerPage;
        }
        return new ArrayList<Project>(projects);
    }

    public String getUploadedProjectPath(User user, ProjectService.Iface client, boolean extendedByReleases) throws TException{
        List<Project> listOfProjects = getProjectList(client, user);
        return client.getReportInEmail(listOfProjects, user, "projects", extendedByReleases);
    }

    public ByteBuffer getReportStreamFromURl(ProjectService.Iface client,User user, String exporterObject,boolean extendedByReleases, String token) 
            throws TException, SW360Exception{
        return client.downloadExcel(user,exporterObject,extendedByReleases, token);
    }

    public ByteBuffer getComponentReportStreamFromURl(ComponentService.Iface client,User user, String exporterObject,boolean extendedByReleases, String token) 
            throws TException, SW360Exception{
        return client.downloadExcel(user, extendedByReleases, token);
    }

    public String getUploadedComponentPath(User sw360User, Iface client, boolean withLinkedReleases) throws TException{
        List<Component> listOfComponents = getComponentList(client, sw360User);
        return client.getReportInEmail(listOfComponents, sw360User, withLinkedReleases);
    }

    private List<Component> getComponentList(Iface client, User sw360User) throws TException {
        int total = client.getTotalComponentsCount(sw360User);
        PaginationData pageData = new PaginationData();
        pageData.setAscending(true);
        Map<PaginationData, List<Component>> pageDtToComponenents;
        Set<Component> components = new HashSet<>();
        int displayStart = 0;
        int rowsPerPage = 500;
        while (0 < total) {
            pageData.setDisplayStart(displayStart);
            pageData.setRowsPerPage(rowsPerPage);
            displayStart = displayStart + rowsPerPage;
            pageDtToComponenents = client.getRecentComponentsSummaryWithPagination(sw360User, pageData);
            components.addAll(pageDtToComponenents.entrySet().iterator().next().getValue());
            total = total - rowsPerPage;
        }
        return new ArrayList<Component>(components);
    }

    public ByteBuffer getComponentBuffer(User sw360User, Iface client, boolean withLinkedReleases) throws SW360Exception, TException{
        List<Component> listOfComponents = getComponentList(client, sw360User);
        ByteBuffer buffer = client.getReportDataStream(listOfComponents, sw360User, withLinkedReleases);
        return buffer;
    }
}
