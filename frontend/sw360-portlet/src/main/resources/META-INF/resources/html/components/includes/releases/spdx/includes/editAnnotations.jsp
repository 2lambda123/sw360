<%--
    ~ Copyright TOSHIBA CORPORATION, 2021. Part of the SW360 Portal Project.
    ~ Copyright Toshiba Software Development (Vietnam) Co., Ltd., 2021. Part of the SW360 Portal Project.

    ~ This program and the accompanying materials are made
    ~ available under the terms of the Eclipse Public License 2.0
    ~ which is available at https://www.eclipse.org/legal/epl-2.0/

    ~ SPDX-License-Identifier: EPL-2.0
--%>

<table class="table spdx-table spdx-full" id="editAnnotations">
    <thead>
        <tr>
            <th colspan="3">12. Annotations</th>
        </tr>
    </thead>
    <tbody class="section section-annotation">
        <tr>
            <td>
                <div style="display: flex; flex-direction: row; margin-bottom: 0.75rem; padding-left: 1rem;">
                    <label for="selectAnnotationSource" style="text-decoration: underline;" class="sub-title">Select Source</label>
                    <select id="selectAnnotationSource" type="text" class="form-control spdx-select always-enable" style="margin-right: 4rem;">
                        <option>SPDX Document</option>
                        <option>Package</option>
                    </select>
                </div>
                <div style="display: flex; flex-direction: column; padding-left: 1rem;">
                    <div style="display: flex; flex-direction: row; margin-bottom: 0.75rem;">
                        <label for="selectAnnotation" style="text-decoration: underline;" class="sub-title">Select Annotation</label>
                        <select id="selectAnnotation" type="text" class="form-control spdx-select"></select>
                        <svg class="disabled lexicon-icon spdx-delete-icon-main" name="delete-annotation" data-row-id="" viewBox="0 0 512 512">
                            <title><liferay-ui:message key="delete" /></title>
                            <use href="/o/org.eclipse.sw360.liferay-theme/images/clay/icons.svg#trash"/>
                        </svg>
                    </div>
                    <button class="spdx-add-button-main" name="add-annotation">Add new Annotation</button>
                </div>
            </td>
        </tr>
        <tr>
            <td style="display: flex">
                <div class="form-group" style="flex: 3">
                    <label class="mandatory" for="annotator">12.1 Annotator</label>
                    <div style="display: flex">
                        <select id="annotatorType" style="flex: 2; margin-right: 1rem;" type="text" class="form-control" placeholder="Enter Type">
                            <option value="Organization" selected="">Organization</option>
                            <option value="Person">Person</option>
                            <option value="Tool">Tool</option>
                        </select>
                        <input style="flex: 6; margin-right: 1rem;" id="annotatorValue" type="text" class="form-control"
                            placeholder="Enter Value" >
                    </div>
                </div>
                <div class="form-group" style="flex: 1">
                    <label class="mandatory" for="annotationCreatedDate">12.2 Annotation Date </label>
                    <div style="display: flex">
                        <div>
                            <input id="annotationCreatedDate" style="width: 12rem; text-align: center;" type="date"
                                class="form-control needs-validation" rule="required" placeholder="creation.date.yyyy.mm.dd" >
                            <div id="annotationCreatedDate-error-messages">
                                <div class="invalid-feedback" rule="required">
                                    <liferay-ui:message key="invalid.format" />
                                </div>
                            </div>
                        </div>
                        <div>
                            <input id="annotationCreatedTime" style="width: 12rem; text-align: center; margin-left: 10px;"
                                type="time" step="1" class="form-control needs-validation" rule="required" placeholder="creation.time.hh.mm.ss" >
                            <div id="annotationCreatedTime-error-messages">
                                <div class="invalid-feedback" rule="required">
                                    <liferay-ui:message key="invalid.format" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
        <tr>
            <td style="display: flex">
                <div class="form-group" style="flex: 1">
                    <label class="mandatory" for="annotationType">12.3 Annotation Type</label>
                    <input id="annotationType" class="form-control" type="text" placeholder="Enter Annotation Type" >
                </div>
                <div class="form-group" style="flex: 1">
                    <label class="mandatory" for="spdxIdRef">12.4 SPDX Identifier Reference</label>
                    <input id="spdxIdRef" class="form-control" type="text"
                        placeholder="Enter SPDX Identifier Reference" >
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <div class="form-group">
                    <label class="mandatory" for="annotationComment">12.5 Annotation Comment</label>
                    <textarea class="form-control" id="annotationComment" rows="5"
                        placeholder="Enter License Comment"></textarea>
                </div>
            </td>
        </tr>
    </tbody>
</table>