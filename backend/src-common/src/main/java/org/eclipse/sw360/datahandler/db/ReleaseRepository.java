/*
 * Copyright Siemens AG, 2013-2018. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.datahandler.db;

import org.eclipse.sw360.components.summary.ReleaseSummary;
import org.eclipse.sw360.components.summary.SummaryType;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.cloudantclient.DatabaseConnectorCloudant;
import org.eclipse.sw360.datahandler.couchdb.SummaryAwareRepository;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;

import java.util.*;
import com.cloudant.client.api.model.DesignDocument.MapReduce;
import com.cloudant.client.api.views.Key;
import com.cloudant.client.api.views.UnpaginatedRequestBuilder;
import com.cloudant.client.api.views.ViewRequestBuilder;

import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * CRUD access for the Release class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author stefan.jaeger@evosoft.com
 */
public class ReleaseRepository extends SummaryAwareRepository<Release> {

    private static final String ALL = "function(doc) { if (doc.type == 'release') emit(null, doc._id) }";
    private static final String BYNAME = "function(doc) { if(doc.type == 'release') { emit(doc.name, doc._id) } }";
    private static final String BYCREATEDON = "function(doc) { if(doc.type == 'release') { emit(doc.createdOn, doc._id) } }";
    private static final String SUBSCRIBERS = "function(doc) {" +
            " if (doc.type == 'release'){" +
            "    for(var i in doc.subscribers) {" +
            "      emit(doc.subscribers[i], doc._id);" +
            "    }" +
            "  }" +
            "}";
    private static final String USEDINRELEASERELATION = "function(doc) {" +
            " if(doc.type == 'release') {" +
            "   for(var id in doc.releaseIdToRelationship) {" +
            "     emit(id, doc._id);" +
            "   }" +
            " }" +
            "}";
    private static final String RELEASEBYVENDORID = "function(doc) {" +
            " if (doc.type == 'release'){" +
            "     emit(doc.vendorId, doc._id);" +
            "  }" +
            "}";
    private static final String RELEASESBYCOMPONENTID = "function(doc) {" +
            " if (doc.type == 'release'){" +
            "      emit(doc.componentId, doc._id);" +
            "  }" +
            "}";
    
    private static final String RELEASEIDSBYVENDORID = "function(doc) {" +
            " if (doc.type == 'release'){" +
            "      emit(doc.vendorId, doc._id);" +
            "  }" +
            "}";

    private static final String RELEASEIDSBYLICENSEID = "function(doc) {" +
            "  if (doc.type == 'release'){" +
            "    for(var i in doc.mainLicenseIds) {" +
            "      emit(doc.mainLicenseIds[i], doc._id);" +
            "    }" +
            "  }" +
              "}";
    private static final String BYEXTERNALIDS = "function(doc) {" +
            "  if (doc.type == 'release') {" +
            "    for (var externalId in doc.externalIds) {" +
            "      try {" +
            "            var values = JSON.parse(doc.externalIds[externalId]);" +
            "            if(!isNaN(values)) {" +
            "               emit( [externalId, doc.externalIds[externalId]], doc._id);" +
            "               continue;" +
            "            }" +
            "            for (var idx in values) {" +
            "              emit( [externalId, values[idx]], doc._id);" +
            "            }" +
            "      } catch(error) {" +
            "          emit( [externalId, doc.externalIds[externalId]], doc._id);" +
            "      }" +
            "    }" +
            "  }" +
            "}";
    
    private static final String BY_LOWERCASE_RELEASE_CPE_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'release' && doc.cpeid != null) {" +
                    "    emit(doc.cpeid.toLowerCase(), doc._id);" +
                    "  } " +
                    "}";

    private static final String RELEASES_BY_SVM_ID_RELEASE_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'release' && doc.externalIds) {" +
                    "    var svmId = doc.externalIds['" + SW360Constants.SVM_COMPONENT_ID + "'];" +
                    "    if (svmId != null) {" +
                    "      emit(svmId, doc._id);" +
                    "    }" +
                    "  } " +
                    "}";

    private static final String BY_LOWERCASE_RELEASE_NAME_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'release' && doc.name != null) {" +
                    "    emit(doc.name.toLowerCase(), doc._id);" +
                    "  } " +
                    "}";

    private static final String BY_LOWERCASE_RELEASE_VERSION_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'release' && doc.version != null) {" +
                    "    emit(doc.version.toLowerCase(), doc._id);" +
                    "  } " +
                    "}";

    public ReleaseRepository(DatabaseConnectorCloudant db, VendorRepository vendorRepository) {
        super(Release.class, db, new ReleaseSummary(vendorRepository));
        Map<String, MapReduce> views = new HashMap<String, MapReduce>();
        views.put("all", createMapReduce(ALL, null));
        views.put("byname", createMapReduce(BYNAME, null));
        views.put("byCreatedOn", createMapReduce(BYCREATEDON, null));
        views.put("subscribers", createMapReduce(SUBSCRIBERS, null));
        views.put("usedInReleaseRelation", createMapReduce(USEDINRELEASERELATION, null));
        views.put("releaseByVendorId", createMapReduce(RELEASEBYVENDORID, null));
        views.put("releasesByComponentId", createMapReduce(RELEASESBYCOMPONENTID, null));
        views.put("releaseIdsByLicenseId", createMapReduce(RELEASEIDSBYLICENSEID, null));
        views.put("byExternalIds", createMapReduce(BYEXTERNALIDS, null));
        views.put("releaseByCpeId", createMapReduce(BY_LOWERCASE_RELEASE_CPE_VIEW, null));
        views.put("releaseBySvmId", createMapReduce(RELEASES_BY_SVM_ID_RELEASE_VIEW, null));
        views.put("releaseByName", createMapReduce(BY_LOWERCASE_RELEASE_NAME_VIEW, null));
        views.put("releaseByVersion", createMapReduce(BY_LOWERCASE_RELEASE_VERSION_VIEW, null));
        views.put("releaseIdsByVendorId", createMapReduce(RELEASEIDSBYVENDORID, null));
        initStandardDesignDocument(views, db);
    }

    public List<Release> searchByNamePrefix(String name) {
        return makeSummary(SummaryType.SHORT, queryForIdsByPrefix("byname", name));
    }

    public List<Release> searchByNameAndVersion(String name, String version){
        List<Release> releasesMatchingName =  new ArrayList<Release>(getFullDocsById(queryForIdsAsValue("byname", name)));
        List<Release> releasesMatchingNameAndVersion = releasesMatchingName.stream()
                .filter(r -> isNullOrEmpty(version) ? isNullOrEmpty(r.getVersion()) : version.equals(r.getVersion()))
                .collect(Collectors.toList());
        return releasesMatchingNameAndVersion;
    }

    public List<Release> getReleaseSummary() {
        return makeSummary(SummaryType.SUMMARY, getAllIds());
    }

    public List<Release> getRecentReleases() {
        ViewRequestBuilder query = getConnector().createQuery(Release.class, "byCreatedOn");
        // Get the 5 last documents
        UnpaginatedRequestBuilder reqBuilder = query.newRequest(Key.Type.STRING, Object.class).limit(5).descending(true).includeDocs(false);
        return makeSummary(SummaryType.SHORT, queryForIds(reqBuilder));
    }

    public List<Release> getSubscribedReleases(String email) {
        Set<String> ids = queryForIds("subscribers", email);
        return makeSummary(SummaryType.SHORT, ids);
    }

    public List<Release> getReleasesFromVendorId(String id, User user) {
        Set<String> releaseIds = queryForIdsAsValue("releaseByVendorId", id);
        return makeSummaryWithPermissionsFromFullDocs(SummaryType.SUMMARY,
                new ArrayList<Release>(getFullDocsById(releaseIds)), user);
    }

    public List<Release> getReleasesFromComponentId(String id) {
         Set<String> releaseIds = queryForIdsAsValue("releasesByComponentId", id);
         return new ArrayList<Release>(getFullDocsById(releaseIds));
    }

    public List<Release> getReleasesFromComponentId(String id, User user) {
        Set<String> releaseIds = queryForIdsAsValue("releasesByComponentId", id);
        return makeSummaryWithPermissionsFromFullDocs(SummaryType.SUMMARY, 
                new ArrayList<Release>(getFullDocsById(releaseIds)), user);
    }

    public List<Release> getReleasesIgnoringNotFound(Collection<String> ids) {
        return getConnector().get(Release.class, ids, true);
    }

    public List<Release> getReleasesFromVendorIds(Set<String> ids) {
        Set<String> releaseIds = queryForIdsAsValue("releaseByVendorId", ids);
        return makeSummaryFromFullDocs(SummaryType.SHORT, new ArrayList<Release>(getFullDocsById(releaseIds)));
    }

    public Set<String> getReleaseIdsFromVendorIds(Set<String> ids) {
    	ViewRequestBuilder query = getConnector().createQuery(Release.class, "releaseIdsByVendorId");
    	String[] arrayOfString = new String[ids.size()];
        int index = 0;
        for (String str : ids)
            arrayOfString[index++] = str;
        UnpaginatedRequestBuilder reqBuild = query.newRequest(Key.Type.STRING, Object.class).keys(arrayOfString);
        return queryForIds(reqBuild);
    }

    public Set<Release> getReleasesByVendorId(String vendorId) {
        Set<String> releaseIds = queryForIdsAsValue("releaseByVendorId", vendorId);
        return getFullDocsById(releaseIds);
    }

    public List<Release> searchReleasesByUsingLicenseId(String licenseId) {
        Set<String> releaseIds = queryForIdsAsValue("releaseIdsByLicenseId", licenseId);
        return new ArrayList<Release>(getFullDocsById(releaseIds));
    }

    public Set<String> getReleaseIdsBySvmId(String svmId) {
        return queryForIdsAsValue("releaseBySvmId", svmId != null ? svmId.toLowerCase() : svmId);
    }

    public Set<String> getReleaseByLowercaseCpe(String cpeid) {
        return queryForIdsAsValue("releaseByCpeId", cpeid != null ? cpeid.toLowerCase() : cpeid);
    }

    public Set<String> getReleaseByLowercaseNamePrefix(String namePrefix) {
        return queryForIdsByPrefix("releaseByName", namePrefix != null ? namePrefix.toLowerCase() : namePrefix);
    }

    public Set<String> getReleaseByLowercaseVersionPrefix(String versionPrefix) {
        return queryForIdsByPrefix("releaseByVersion", versionPrefix != null ? versionPrefix.toLowerCase() : versionPrefix);
    }

    public Set<Release> searchByExternalIds(Map<String, Set<String>> externalIds) {
        RepositoryUtils repositoryUtils = new RepositoryUtils();
        Set<String> searchIds = repositoryUtils.searchByExternalIds(this, "byExternalIds", externalIds);
        return new HashSet<>(get(searchIds));
    }

    public List<Release> getReferencingReleases(String releaseId) {
        return queryView("usedInReleaseRelation", releaseId);
    }
}
