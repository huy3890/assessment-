package com.aem.assessment.core.utils;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

public class PageUtil {
	private static final String HOME_TEMPLATE = "Assessment/templates/homePage";

	public static Page getHomePage(final Page currentPage) {
		if (Objects.isNull(currentPage)) {
			return null;
		}
		if (currentPage.getTemplate() != null
				&& StringUtils.contains(currentPage.getTemplate().getPath(), HOME_TEMPLATE)) {
			return currentPage;
		}
		final Page parentPage = currentPage.getParent();
		return getHomePage(parentPage);
	}

	public static Page getCurrentPage(Resource resource) {
		PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
		if (pageManager == null) {
			return null;
		}
		return pageManager.getContainingPage(resource);
	}
}
