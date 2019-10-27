package com.aem.assessment.core.services.osgi;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Assessment Navigation Config", description = "Setting up navigation caching")
public @interface NavigationOcd {
	// Enable Navigation Caching
	@AttributeDefinition(name = "enabledCaching", description = "Enabled Navigation caching", defaultValue = "false")
	boolean getEnabledCaching();

	// Cache Timeout
	@AttributeDefinition(name = "cacheTimeout", description = "Caching timeout (seconds)", defaultValue = "300")
	String getCachingTimeout();
}
