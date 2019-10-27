package com.aem.assessment.core.beans;

import java.util.List;

public class NavigationBean extends BaseBean {

	private String title;
	private String path;
	private List<NavigationBean> childNavigationList;

	public String getTitle() {
		return title;
	}

	public String getPath() {
		return path;
	}

	public List<NavigationBean> getChildNavigationList() {
		return childNavigationList;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setChildNavigationList(List<NavigationBean> childNavigationList) {
		this.childNavigationList = childNavigationList;
	}

}
