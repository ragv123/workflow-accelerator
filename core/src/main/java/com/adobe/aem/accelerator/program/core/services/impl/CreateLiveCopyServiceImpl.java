package com.adobe.aem.accelerator.program.core.services.impl;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.services.CreateLiveCopyService;
import com.adobe.aem.accelerator.program.core.services.config.CreateLiveCopyServiceConfig;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutManager;;

/**
 * The Class CreateLiveCopyServiceImpl.
 */
@Component(immediate = true, service = CreateLiveCopyService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = CreateLiveCopyServiceConfig.class)
public class CreateLiveCopyServiceImpl implements CreateLiveCopyService {

	/** The config. */
	private CreateLiveCopyServiceConfig config;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateLiveCopyServiceImpl.class);

	/**
	 * activate method.
	 *
	 * @param config site Improve Script URL configuration
	 */
	@Activate
	public void activate(final CreateLiveCopyServiceConfig config) {
		this.config = config;
	}

	/**
	 * This method accepts the required parameters to create a live copy and calls
	 * the WCM Command Servlet to create live copy.
	 *
	 * @param resolver       the resolver
	 * @param payload        the payload
	 * @param rolloutManager the rollout manager
	 * @param liveRelManager the live rel manager
	 * @param language       the language
	 */
	@Override
	public void createLiveCopy(ResourceResolver resolver, String payload, RolloutManager rolloutManager,
			LiveRelationshipManager liveRelManager, String language) {
		
	}

}
