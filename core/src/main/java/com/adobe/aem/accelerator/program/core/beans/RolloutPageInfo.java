package com.adobe.aem.accelerator.program.core.beans;

public class RolloutPageInfo {

	private String path;
	private String name;
	private String title;

	public RolloutPageInfo(String path, String name, String title) {
		this.path = path;
		this.name = name;
		this.title = title;
	}

	public RolloutPageInfo() {

	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
