package com.aem.assessment.core.services.osgi;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = NavigationCachingConfig.class, immediate = true, name = "com.aem.assessment.core.services.osgi.NavigationCachingConfig")
@Designate(ocd = NavigationOcd.class)
public class NavigationCachingConfig {
	private String cacheTimeout;

	private boolean enabledCaching;

	@Activate
	public void activate(NavigationOcd config) {
		cacheTimeout = config.getCachingTimeout();
		enabledCaching = config.getEnabledCaching();
	}

	public String getCacheTimeout() {
		return cacheTimeout;
	}

	public boolean isEnabledCaching() {
		return enabledCaching;
	}

	public void setCacheTimeout(String cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

	public void setEnabledCaching(boolean enabledCaching) {
		this.enabledCaching = enabledCaching;
	}

}
