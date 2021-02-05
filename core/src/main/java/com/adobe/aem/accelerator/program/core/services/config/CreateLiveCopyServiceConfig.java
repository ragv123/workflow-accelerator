package com.adobe.aem.accelerator.program.core.services.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Interface CreateLiveCopyServiceConfig.
 */
@ObjectClassDefinition(name = "Create Live Copy Config", description = "Create Live Copy Config")
public @interface CreateLiveCopyServiceConfig {

	/**
	 * Gets the english live copy base paths.
	 *
	 * @return the english live copy base paths
	 */
	@AttributeDefinition(name = "English Live Copy Base Paths", description = "English Live Copy Base Paths")
	String[] getEnglishLiveCopyBasePaths();

	/**
	 * Gets the french live copy base paths.
	 *
	 * @return the french live copy base paths
	 */
	@AttributeDefinition(name = "French Live Copy Base Paths", description = "French Live Copy Base Paths")
	String[] getFrenchLiveCopyBasePaths();

	/**
	 * Gets the rollout configs.
	 *
	 * @return the rollout configs
	 */
	@AttributeDefinition(name = "Rollout Configs", description = "Rollout Configs")
	String[] getRolloutConfigs();

}
