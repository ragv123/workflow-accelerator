package com.adobe.aem.accelerator.program.core.services;

import org.apache.sling.api.resource.ResourceResolver;

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
	 * @param srcPath          the src path
	 * @param rolloutManager   the rollout manager
	 * @param liveRelManager   the live rel manager
	 * @param language         the language
	 */
	public void createLiveCopy(ResourceResolver resourceResolver, String srcPath, RolloutManager rolloutManager,
			LiveRelationshipManager liveRelManager, String language);

}
