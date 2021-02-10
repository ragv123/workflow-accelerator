package com.adobe.aem.accelerator.program.core.services;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;

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

}
