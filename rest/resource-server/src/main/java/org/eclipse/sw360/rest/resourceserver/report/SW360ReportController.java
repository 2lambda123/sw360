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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.thrift.PaginationData;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonObject;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static org.eclipse.sw360.datahandler.common.SW360Constants.CONTENT_TYPE_OPENXML_SPREADSHEET;
import static org.eclipse.sw360.datahandler.common.SW360Utils.getBUFromOrganisation;

@BasePathAwareController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))

public class SW360ReportController implements RepresentationModelProcessor<RepositoryLinksResource>{

    public static final String REPORTS_URL = "/reports";

    @NonNull
    private final RestControllerHelper restControllerHelper;
    
    @NonNull
    private final SW360ReportService sw360ReportService;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(SW360ReportController.class).slash("api/projects/" + REPORTS_URL).withRel("reports"));
        return resource;
    }
    
    private List<String> mimeTypeList = Arrays.asList("xls","xlsx");
    
    private static String BACKEND_URL;
    public static final String PROPERTIES_FILE_PATH = "/sw360.properties";
    static {
        Properties props = CommonUtils.loadProperties(ThriftClients.class, PROPERTIES_FILE_PATH);
        BACKEND_URL = props.getProperty("backend.url", "http://127.0.0.1:8080");
    }

    @RequestMapping(value = REPORTS_URL + "/myprojectreports", method = RequestMethod.GET)
    public void getProjectReport(@RequestParam(value = "withlinkedreleases", required = false, defaultValue = "false") boolean withLinkedReleases,
            @RequestParam(value = "mimetype", required = false, defaultValue = "xlsx") String mimeType,
            @RequestParam(value = "mailrequest", required = false, defaultValue="false") String mailRequest, HttpServletRequest request,
            HttpServletResponse response) throws TException{

        final User sw360User = restControllerHelper.getSw360UserFromAuthentication();
        ThriftClients thriftClients = new ThriftClients();
        ProjectService.Iface client = thriftClients.makeProjectClient();

        try {
            if(validateMimeType(mimeType)) {
                if(mailRequest.equalsIgnoreCase("true")) {
                    
                    String projectPath = sw360ReportService.getUploadedProjectPath(sw360User, client, withLinkedReleases);
                    
                    BACKEND_URL = BACKEND_URL + "/resource/api/reports/download?user="+sw360User.getEmail()+"&extendedByReleases="+withLinkedReleases+"&token=";
                    
                    URL url = new URL(BACKEND_URL+projectPath);
                    
                    if(!CommonUtils.isNullEmptyOrWhitespace(projectPath)) {
                        client.sendExportSpreadsheetSuccessMail(url.toString(), sw360User.getEmail());
                    }
                    JsonObject responseJson = new JsonObject();
                    responseJson.addProperty("response", "E-mail sent succesfully to the end user.");
                    responseJson.addProperty("url", url.toString());
                    responseJson.toString();
                    response.getWriter().write(responseJson.toString());
                }else {
                    downloadExcelReport(withLinkedReleases, mimeType, response, sw360User, client);
                }
            }else {
                throw new TException("Error : Mimetype either should be : xls/xlsx");
            }
        }catch (TException t) {
            throw new TException(t.getMessage());
        }catch (Exception e) {
            throw new TException(e.getMessage());
        }
    }

    private void downloadExcelReport(boolean withLinkedReleases, String mimeType, HttpServletResponse response,
            User user, ProjectService.Iface client) throws TException, SW360Exception, IOException {
        ByteBuffer buffer = sw360ReportService.getProjectBuffer(user, client,withLinkedReleases);
        response.setContentType(getContentType(mimeType));
        String filename = String.format("projects-%s.xlsx", SW360Utils.getCreatedOn());
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
        copyDataStreamToResponse(response, buffer);
    }

    private void copyDataStreamToResponse(HttpServletResponse response, ByteBuffer buffer) throws IOException {
        FileCopyUtils.copy(buffer.array(), response.getOutputStream());
    }

    private String getContentType(String mimeType) {
        String contentType=null;
        switch (mimeType) {
        case "xlsx":
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            break;
        case "docx":
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            break;
        default:
            break;
        }
        return contentType;
    }

    private boolean validateMimeType(String mimeType) {
        if(mimeTypeList.contains(mimeType)) {
            return true;
        }
        return false;
    }
    
    @RequestMapping(value = REPORTS_URL + "/download", method = RequestMethod.GET)
    public void downloadExcel(HttpServletRequest request,HttpServletResponse response) throws TException{
        String userEmail = request.getParameter("user");
        String token = request.getParameter("token");
        String extendedByReleases = request.getParameter("extendedByReleases");
        ThriftClients thriftClients = new ThriftClients();
        ProjectService.Iface client = thriftClients.makeProjectClient();
        User user=restControllerHelper.getUserByEmail(userEmail);

        try {
            ByteBuffer buffer =  sw360ReportService.getReportStreamFromURl(client,user, "projects",Boolean.valueOf(extendedByReleases), token);
            String filename = String.format("projects-%s.xlsx", SW360Utils.getCreatedOn());
            response.setContentType(getContentType("xlsx"));
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
            copyDataStreamToResponse(response, buffer);
        } catch (IOException | TException e) {
            throw new TException(e.getMessage());
        }
    }
    
}
