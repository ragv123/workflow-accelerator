package com.adobe.aem.accelerator.program.core.beans;

public class CountryBean {

	private String path;
	private String title;

	public CountryBean(String path, String title) {
		this.path = path;
		this.title = title;
	}

	public CountryBean() {

	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
