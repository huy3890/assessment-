package com.aem.assessment.core.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.framework.ServiceException;

import com.aem.assessment.core.beans.NavigationBean;
import com.aem.assessment.core.services.osgi.NavigationCachingConfig;
import com.aem.assessment.core.utils.CacheDataUtil;
import com.aem.assessment.core.utils.PageUtil;
import com.aem.assessment.core.utils.PersistentCacheUtil;
import com.aem.assessment.core.utils.ResourceUtil;
import com.day.cq.wcm.api.Page;

@Model(adaptables = Resource.class)
public class HeaderModel {

	@Inject
	@Named("sling:resourceType")
	@Default(values = "No resourceType")
	protected String resourceType;

	// level to get children pages
	private static final int LEVEL_PAGE = 3;

	@SlingObject
	private Resource resource;

	@Inject
	private NavigationCachingConfig navigationCachingConfig;

	private NavigationBean presenter;

	private Map<String, String> cacheObject;
	private static final String CACHING_FILE_NAME = "NavigationData";
	private static final String HTML = ".html";

	@Inject
	private ResourceResolverFactory rrFactory;

	@PostConstruct
	protected void init() {
		presenter = getNavigationData();
	}

	private static NavigationBean getNavigationBean(final Page page, final int pageLevel, int count) {
		if (Objects.isNull(page) || count > pageLevel) {
			return null;
		}
		count++;
		final NavigationBean navigationBean = new NavigationBean();
		navigationBean.setTitle(page.getTitle());
		navigationBean.setPath(page.getPath() + HTML);
		final List<NavigationBean> listChildrenNavigationBean = new ArrayList<>();
		final Iterator<Page> iterator = page.listChildren();
		while (iterator.hasNext()) {
			final Page subPage = iterator.next();
			if (Objects.nonNull(subPage)) {
				final NavigationBean subNavigationBean = getNavigationBean(subPage, pageLevel, count);
				listChildrenNavigationBean.add(subNavigationBean);
			}
		}
		navigationBean.setChildNavigationList(listChildrenNavigationBean);
		return navigationBean;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private NavigationBean getNavigationData() throws ServiceException {
		ResourceResolver adminResolver;
		Session session = null;
		try {
			adminResolver = this.rrFactory.getAdministrativeResourceResolver(null);
			session = adminResolver.adaptTo(Session.class);
		} catch (LoginException e) {
			e.printStackTrace();
			return null;
		}
		this.cacheObject = null;
		if (navigationCachingConfig.isEnabledCaching()) {
			Node cacheNode = PersistentCacheUtil.getCacheNode(CACHING_FILE_NAME, session);
			final int cacheTimeout = Integer.parseInt(navigationCachingConfig.getCacheTimeout());
			if (!CacheDataUtil.isCacheExpired(cacheNode, cacheTimeout)) {
				this.cacheObject = (Map<String, String>) PersistentCacheUtil.loadPersistedObject(CACHING_FILE_NAME,
						session);
			}
		}
		if (Objects.isNull(cacheObject)) {
			final Page page = PageUtil.getCurrentPage(this.resource);
			final Page homepage = PageUtil.getHomePage(page);
			int count = 0;
			presenter = getNavigationBean(homepage, LEVEL_PAGE, count);
			final String json = ResourceUtil.objectToJson(presenter);
			this.cacheObject = new HashMap<String, String>();
			cacheObject.put("Navigation-Caching", json);
			PersistentCacheUtil.persistObject(CACHING_FILE_NAME, (Serializable) this.cacheObject, session);
			return presenter;

		}
		final String json = cacheObject.get("Navigation-Caching");
		final NavigationBean result = ResourceUtil.contentJsonStrToObjectT(json, NavigationBean.class);
		return result;
	}

	public NavigationBean getPresenter() {
		return presenter;
	}

	public boolean hasContent() {
		return Objects.nonNull(presenter);
	}

}
