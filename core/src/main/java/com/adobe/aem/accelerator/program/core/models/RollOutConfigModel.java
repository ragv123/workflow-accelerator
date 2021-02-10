/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.aem.accelerator.program.core.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.jcr.RangeIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.aem.accelerator.program.core.beans.CountryBean;
import com.adobe.aem.accelerator.program.core.beans.RolloutPageInfo;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.msm.api.LiveCopy;
import com.day.cq.wcm.msm.api.LiveRelationship;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.google.gson.Gson;

@Model(adaptables = Resource.class, resourceType = {
		RollOutConfigModel.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RollOutConfigModel {

	public static final String RESOURCE_TYPE = "accelerator-program/components/content/rolloutConfigs";

	@OSGiService
	private LiveRelationshipManager relationMgr;

	@SlingObject
	private ResourceResolver resolver;

	private PageManager pageManager;

	@ValueMapValue
	private String rolloutRootPage;

	@ValueMapValue
	private String siteRootPage;

	@ValueMapValue
	private String countryResourceType;

	private Map<String, List<RolloutPageInfo>> pageinfoMap = new HashMap<String, List<RolloutPageInfo>>();

	private Set<CountryBean> countriesSet = new HashSet<CountryBean>();

	@PostConstruct
	public void init() {
		pageManager = resolver.adaptTo(PageManager.class);
	}

	public String getPages() {
		return new Gson().toJson(getModifiedOrCreatedPages());
	}

	public Set<CountryBean> getCountriesSet() {
		if (StringUtils.isNotBlank(siteRootPage)) {
			Page websiteRootPage = pageManager.getPage(siteRootPage);
			if (null != websiteRootPage) {
				Iterator<Page> pageItr = websiteRootPage.listChildren();
				while (pageItr.hasNext()) {
					Page countryPage = pageItr.next();
					if (countryPage.getContentResource() != null && StringUtils.contains(countryResourceType,
							countryPage.getContentResource().getResourceType())) {
						CountryBean bean = new CountryBean(countryPage.getPath(),
								countryPage.getTitle() != null ? countryPage.getTitle() : countryPage.getName());
						countriesSet.add(bean);
					}
				}
			}
		}
		return countriesSet;
	}

	public Map<String, List<RolloutPageInfo>> getModifiedOrCreatedPages() {
		List<RolloutPageInfo> createdPageList = new ArrayList<>();
		List<RolloutPageInfo> modifiedPageList = new ArrayList<>();

		if (relationMgr == null) {
			return pageinfoMap;
		}

		String filter = "";
		if (rolloutRootPage == null) {
			return pageinfoMap;
		}
		try {

			Page requestedPage = pageManager.getPage(rolloutRootPage);
			if (requestedPage == null) {
				return pageinfoMap;
			}

			Iterator<Page> iter = requestedPage.listChildren(null, true);
			while (iter.hasNext()) {
				writePage(createdPageList, modifiedPageList, iter.next(), rolloutRootPage, filter, relationMgr,
						resolver);
			}

			pageinfoMap.put("createdPage", createdPageList);
			pageinfoMap.put("modifiedPage", modifiedPageList);
			return pageinfoMap;
		} catch (Exception e) {
			return pageinfoMap;
		}
	}

	private LiveRelationship findParentRelation(Resource root, String relPath, LiveRelationshipManager relManager)
			throws WCMException {

		int lastIndex = relPath.lastIndexOf("/") + 1; // move to the right to find it again
		LiveRelationship found = null;
		while (found == null && lastIndex > 1) {
			String parentRelPath = relPath.substring(0, lastIndex);
			Resource parent = root.getChild(parentRelPath);
			if (parent == null) {
				parent = new NonExistingResource(root.getResourceResolver(), appendPath(root.getPath(), parentRelPath));
			}
			found = relManager.getLiveRelationship(parent, true);
			lastIndex = relPath.lastIndexOf("/", lastIndex - 1);
		}
		if (found == null) {
			found = relManager.getLiveRelationship(root, true);
		}
		return found;
	}

	private Resource getResource(String path, ResourceResolver resolver) {
		Resource resource = resolver.getResource(path);
		if (resource == null) {
			resource = new NonExistingResource(resolver, path);
		}
		return resource;
	}

	private String appendPath(String root, String relative) {
		if (StringUtils.isEmpty(relative)) {
			return root;
		}
		if (root.endsWith("/")) {
			return String.format("%s%s", root, relative);
		} else {
			return String.format("%s/%s", root, relative);
		}
	}

	private List<String> getTargetRoots(Resource rootSource, LiveRelationshipManager relationshipManager)
			throws WCMException {
		ArrayList<String> paths = new ArrayList<String>();
		RangeIterator all = relationshipManager.getLiveRelationships(rootSource, null, null);
		while (all.hasNext()) {
			paths.add(((LiveRelationship) all.next()).getTargetPath());
		}
		return paths;
	}

	private void writePageRelationship(List<RolloutPageInfo> createdPageList, List<RolloutPageInfo> modifiedPageList,
			Page page, List<String> lrRoots, String rootPath, String relPath, String nameFilter,
			LiveRelationshipManager relationMgr, ResourceResolver resolver) throws WCMException {

		for (String lrRootPath : lrRoots) {
			if (lrRootPath.indexOf(nameFilter) != -1) {
				String targetPath = appendPath(lrRootPath, relPath);
				String sourcePath = appendPath(rootPath, relPath);
				Resource targetResource = getResource(targetPath, resolver);
				LiveRelationship childRelation = relationMgr.getLiveRelationship(targetResource, true);
				if (childRelation != null && childRelation.getSourcePath().equals(sourcePath)) {
					if (isModifiedOrCreated(childRelation, createdPageList, modifiedPageList, page)) {
						break;
					}

				} else if (childRelation == null) {
					RangeIterator t = relationMgr.getLiveRelationships(resolver.getResource(sourcePath), targetPath,
							null);
					if (t.hasNext()) {
						LiveRelationship relation = ((LiveRelationship) t.next());
						if (isModifiedOrCreated(relation, createdPageList, modifiedPageList, page)) {
							break;
						}
					}
				} else {
					LiveCopy liveCopy = findParentRelation(getResource(lrRootPath, resolver), relPath, relationMgr)
							.getLiveCopy();
					if (liveCopy.isDeep()) {
						if (isModifiedOrCreated(childRelation, createdPageList, modifiedPageList, page)) {
							break;
						}
					}
				}
			}
		}
	}

	private boolean isModifiedOrCreated(LiveRelationship childRelation, List<RolloutPageInfo> createdPageList,
			List<RolloutPageInfo> modifiedPageList, Page page) {
		boolean isModifiedOrCreated = false;
		Boolean isSourceModified = childRelation.getStatus().getAdvancedStatus("msm:isSourceModified");
		Date lastRolledOut = childRelation.getStatus().getLastRolledOut();
		if (isSourceModified && null != lastRolledOut) {
			// add to modifiedList
			RolloutPageInfo info = new RolloutPageInfo(page.getPath(), page.getName(),
					page.getTitle() != null ? page.getTitle() : page.getName());
			isModifiedOrCreated = modifiedPageList.add(info);

		} else if (null == lastRolledOut) {
			// add to created list
			RolloutPageInfo info = new RolloutPageInfo(page.getPath(), page.getName(),
					page.getTitle() != null ? page.getTitle() : page.getName());
			isModifiedOrCreated = createdPageList.add(info);
		}
		return isModifiedOrCreated;
	}

	private void writePage(List<RolloutPageInfo> createdPageList, List<RolloutPageInfo> modifiedPageList, Page page,
			String rootPath, String filter, LiveRelationshipManager relationMgr, ResourceResolver resolver)
			throws WCMException {
		String relPath;
		if (page.getPath().startsWith(rootPath + "/")) {
			relPath = page.getPath().substring(rootPath.length() + 1);
		} else {
			relPath = "";
		}
		writePageRelationship(createdPageList, modifiedPageList, page,
				getTargetRoots(resolver.getResource(rootPath), relationMgr), rootPath, relPath, filter, relationMgr,
				resolver);
	}

}
