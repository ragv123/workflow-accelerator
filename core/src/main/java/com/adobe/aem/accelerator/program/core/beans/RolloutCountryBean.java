package com.adobe.aem.accelerator.program.core.beans;

/**
 * The Class RolloutCountryBean.
 */
public class RolloutCountryBean {

	/** The title. */
	private String title;

	/** The name. */
	private String name;

	/** The language. */
	private String language;

	/** The rollout configs. */
	private String[] rolloutConfigs;

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the language.
	 *
	 * @param language the new language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Gets the rollout configs.
	 *
	 * @return the rollout configs
	 */
	public String[] getRolloutConfigs() {
		return rolloutConfigs;
	}

	/**
	 * Sets the rollout configs.
	 *
	 * @param rolloutConfigs the new rollout configs
	 */
	public void setRolloutConfigs(String[] rolloutConfigs) {
		this.rolloutConfigs = rolloutConfigs;
	}

}
