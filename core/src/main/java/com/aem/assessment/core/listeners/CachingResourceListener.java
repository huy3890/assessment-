package com.aem.assessment.core.listeners;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.assessment.core.utils.PersistentCacheUtil;

@Component(immediate = true, service = EventListener.class)
public class CachingResourceListener implements EventListener {

	final Logger log = LoggerFactory.getLogger(this.getClass());
	private Session adminSession;
	private static final String PATH = "/content/Assessment";
	private static final String CACHING_FILE_NAME = "NavigationData";

	@Reference
	org.apache.sling.jcr.api.SlingRepository repository;

	@Reference
	private ResourceResolverFactory rrFactory;

	@Activate
	public void activate(ComponentContext context) throws Exception {
		log.info("activating ExampleObservation");
		try {
//			adminSession = repository.loginService("datawrite", null);
			ResourceResolver adminResolver = this.rrFactory.getAdministrativeResourceResolver(null);
			adminSession = adminResolver.adaptTo(Session.class);
			adminSession.getWorkspace().getObservationManager().addEventListener(this, // handler
					Event.PROPERTY_ADDED | Event.NODE_ADDED | Event.NODE_REMOVED | Event.NODE_MOVED, // binary
																										// combination
																										// of event
																										// types
					PATH, // path
					true, // is Deep?
					null, // uuids filter
					null, // nodetypes filter
					false);

		} catch (RepositoryException e) {
			log.error("unable to register session", e);
			throw new Exception(e);
		}
	}

	@Deactivate
	public void deactivate() {
		if (adminSession != null) {
			adminSession.logout();
		}
	}

	@Override
	public void onEvent(EventIterator it) {
		try {
			log.info("********INSIDE TRY *****");
			PersistentCacheUtil.deleteObject(CACHING_FILE_NAME, adminSession);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
