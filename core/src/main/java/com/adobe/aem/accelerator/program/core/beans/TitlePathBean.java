package com.adobe.aem.accelerator.program.core.beans;

public class TitlePathBean {

	private String path;
	private String title;

	public TitlePathBean(String path, String title) {
		this.path = path;
		this.title = title;
	}

	public TitlePathBean() {

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
