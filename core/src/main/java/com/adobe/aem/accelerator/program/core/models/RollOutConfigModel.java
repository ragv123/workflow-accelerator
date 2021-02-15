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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.beans.TitlePathBean;
import com.adobe.aem.accelerator.program.core.beans.RolloutPageInfo;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.msm.api.LiveCopy;
import com.day.cq.wcm.msm.api.LiveRelationship;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutConfig;
import com.day.cq.wcm.msm.api.RolloutConfigManager;
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
	private String languageTemplatePath;

	@ValueMapValue
	private String languageRoot;

	@ValueMapValue
	private String countryTemplatePath;

	private Map<String, List<RolloutPageInfo>> pageinfoMap = new HashMap<String, List<RolloutPageInfo>>();

	private Set<TitlePathBean> countriesSet = new HashSet<TitlePathBean>();

	private Set<TitlePathBean> languageSet = new HashSet<TitlePathBean>();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void init() {
		pageManager = resolver.adaptTo(PageManager.class);
	}

	public Set<TitlePathBean> getCountriesSet() {
		return getBeanSet(siteRootPage, countriesSet, countryTemplatePath);
	}

	public Set<TitlePathBean> getLanguageSet() {
		return getBeanSet(languageRoot, languageSet, languageTemplatePath);
	}

	public List<TitlePathBean> getRolloutConfigs() {
		RolloutConfigManager rcm = resolver.adaptTo(RolloutConfigManager.class);
		List<TitlePathBean> rolloutConfigs = new ArrayList<TitlePathBean>();
		// Map<String, String> vm = new HashMap<String, String>();
		try {
			for (String key : rcm.getRolloutConfigs()) {
				RolloutConfig rc = rcm.getRolloutConfig(key);
				if (rc != null) {
					// vm.put("value", rc.getPath());
					// vm.put("text", rc.getTitle());
					rolloutConfigs.add(new TitlePathBean(rc.getPath(), rc.getTitle()));
				}
			}
		} catch (WCMException e) {
			logger.warn("Unable to get rollout configurations");
		}
		return rolloutConfigs;
	}

	public String getCountryTemplatePath() {
		return countryTemplatePath;
	}

	public String getSiteRootPath() {
		return siteRootPage;
	}

	private Set<TitlePathBean> getBeanSet(String rootPagePath, Set<TitlePathBean> beanSet, String templateType) {
		if (StringUtils.isNotBlank(rootPagePath)) {
			Page rootPage = pageManager.getPage(rootPagePath);
			if (null != rootPage) {
				Iterator<Page> pageItr = rootPage.listChildren();
				while (pageItr.hasNext()) {
					Page page = pageItr.next();
					if (StringUtils.contains(templateType, page.getTemplate().getPath())) {
						TitlePathBean bean = new TitlePathBean(page.getPath(),
								page.getTitle() != null ? page.getTitle() : page.getName());
						beanSet.add(bean);
					}
				}
			}
		}
		return beanSet;
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
