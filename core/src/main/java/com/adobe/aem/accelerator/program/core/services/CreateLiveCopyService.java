package com.adobe.aem.accelerator.program.core.services;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.aem.accelerator.program.core.beans.RolloutCountryBean;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutManager;

/**
 * The Interface CreateLiveCopyService.
 */
public interface CreateLiveCopyService {

	/**
	 * Creates the live copy.
	 *
	 * @param resourceResolver the resource resolver
	 * @param pagePaths        the page paths
	 * @param countriesList    the countries list
	 * @param rolloutManager   the rollout manager
	 * @param liveRelManager   the live rel manager
	 * @param isDeep           the is deep
	 * @throws WCMException the WCM exception
	 */
	public void createLiveCopy(ResourceResolver resourceResolver, List<String> pagePaths, List<String> countriesList,
			RolloutManager rolloutManager, LiveRelationshipManager liveRelManager, boolean isDeep) throws WCMException;

	/**
	 * Rollout countries.
	 *
	 * @param resourceResolver   the resource resolver
	 * @param rolloutCountryBean the rollout country bean
	 * @param rolloutManager     the rollout manager
	 * @param liveRelManager     the live rel manager
	 * @param siteRootPath       the site root path
	 * @param templatePath       the template path
	 * @throws WCMException the WCM exception
	 */
	public void rolloutCountries(ResourceResolver resourceResolver, List<RolloutCountryBean> rolloutCountryBean,
			RolloutManager rolloutManager, LiveRelationshipManager liveRelManager, String siteRootPath,
			String templatePath) throws WCMException;

}
