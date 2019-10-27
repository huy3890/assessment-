package com.aem.assessment.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.crx.JcrConstants;

public class PersistentCacheUtil {

	private static final String CACHING_PATH = "assessment/cache";
	private static final String CACHING_ROOT = "/var";

	private static final Logger LOG = LoggerFactory.getLogger(PersistentCacheUtil.class);
	private static final String MIMETYPE_JAVA_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	private static synchronized Node getCacheRootNode(final Session crxSession, final boolean createIfNotExistent)
			throws RepositoryException {
		Node n = crxSession.getNode(CACHING_ROOT);
		final String[] nodeNames = CACHING_PATH.split("\\/");
		for (int i = 0; i < nodeNames.length; i++) {
			if (!n.hasNode(nodeNames[i])) {
				if (createIfNotExistent) {
					n.addNode(nodeNames[i], JcrConstants.NT_FOLDER);
					n.getSession().save();
				} else {
					return null;
				}
			}
			n = n.getNode(nodeNames[i]);
		}
		return n;
	}

	public static void persistData(final String cacheNodeName, final InputStream in, final String dataMimeType,
			final Session crxSession) {
		Node cacheNode = null;
		try {
			cacheNode = getCacheRootNode(crxSession, true);
			if (!cacheNode.hasNode(cacheNodeName)) {
				cacheNode = cacheNode.addNode(cacheNodeName, JcrConstants.NT_FILE);
				final Node contentNode = cacheNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
				contentNode.setProperty(JcrConstants.JCR_DATA, crxSession.getValueFactory().createBinary(in));
				contentNode.setProperty(JcrConstants.JCR_MIMETYPE, dataMimeType);
				contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.getInstance());
				cacheNode.getParent().getSession().save();
			} else {
				cacheNode = cacheNode.getNode(cacheNodeName);
				cacheNode = cacheNode.getNode(JcrConstants.JCR_CONTENT);
				cacheNode.setProperty(JcrConstants.JCR_DATA, crxSession.getValueFactory().createBinary(in));
				cacheNode.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.getInstance());
				cacheNode.getSession().save();
			}
		} catch (RepositoryException e) {
			LOG.error("Failed to persist data in " + CACHING_PATH + "/" + cacheNodeName + ". Cause: " + e.getMessage());
			return;
		}
	}

	public static InputStream loadData(final String cacheNodeName, final Session crxSession) {
		InputStream in = null;
		try {
			Node cacheNode = getCacheRootNode(crxSession, false);
			if (null == cacheNode || !cacheNode.hasNode(cacheNodeName)) {
				return null;
			}
			cacheNode = cacheNode.getNode(cacheNodeName);
			if (cacheNode.hasNode(JcrConstants.JCR_CONTENT)) {
				cacheNode = cacheNode.getNode(JcrConstants.JCR_CONTENT);
				final Property prop = cacheNode.getProperty(JcrConstants.JCR_DATA);
				in = prop.getBinary().getStream();
			}
		} catch (PathNotFoundException e) {
			return null;
		} catch (RepositoryException e) {
			LOG.error("Unable to load cached data from " + CACHING_PATH + "/" + cacheNodeName + ". Cause: "
					+ e.getMessage());
			return null;
		}
		return in;
	}

	public static Serializable loadPersistedObject(final String cacheNodeName, final Session crxSession) {
		final InputStream in = loadData(cacheNodeName, crxSession);
		if (null != in) {
			try {
				final ObjectInputStream oin = new ObjectInputStream(in);
				try {
					return (Serializable) oin.readObject();
				} finally {
					oin.close();
				}
			} catch (IOException e) {
				LOG.error("Unable to de-serialize data from cache-node '" + cacheNodeName + "'. Cause: "
						+ e.getMessage());
			} catch (ClassNotFoundException e) {
				LOG.error("Unable to de-serialize data from cache-node '" + cacheNodeName + "'. Cause: "
						+ e.getMessage());
			}
		}
		return null;
	}

	public static Node getCacheNode(final String cacheNodeName, final Session crxSession) {
		try {
			Node cacheNode = getCacheRootNode(crxSession, false);
			if (null == cacheNode || !cacheNode.hasNode(cacheNodeName)) {
				return null;
			}
			return cacheNode.getNode(cacheNodeName);

		} catch (PathNotFoundException e) {
			return null;
		} catch (RepositoryException e) {
			LOG.error("Unable to get cache node from " + CACHING_PATH + "/" + cacheNodeName + ". Cause: "
					+ e.getMessage());
			return null;
		}
	}

	public static void deleteObject(final String cacheNodeName, final Session crxSession) {

		Node cacheNode = null;

		try {
			cacheNode = getCacheRootNode(crxSession, true);

			if (cacheNode != null) {
				Node deleteCacheNode = cacheNode.getNode(cacheNodeName);
				if (deleteCacheNode != null) {
					deleteCacheNode.remove();
					cacheNode.getSession().save();
				}
			}
		} catch (RepositoryException e) {
			LOG.error("Error deleting the cache node" + e.getMessage());
		}

	}

	public static void persistObject(final String cacheNodeName, final Serializable data, final Session crxSession) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream oout = new ObjectOutputStream(baos);
			try {
				oout.writeObject(data);
			} finally {
				oout.close();
			}
		} catch (IOException e) {
			LOG.error("Failed to persist data in " + CACHING_PATH + "/" + cacheNodeName + ". Cause: " + e.getMessage());
		}
		persistData(cacheNodeName, new ByteArrayInputStream(baos.toByteArray()), MIMETYPE_JAVA_SERIALIZED_OBJECT,
				crxSession);
	}
}
