package com.aem.assessment.core.utils;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.crx.JcrConstants;

public class CacheDataUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheDataUtil.class);

	public static boolean isCacheExpired(Node cacheNode, int cacheTimeout) {
		if (cacheNode == null) {
			return true;
		}
		Calendar cacheModifledDate;
		try {
			Node cacheContentNode = cacheNode.getNode(JcrConstants.JCR_CONTENT);
			if (cacheContentNode == null) {
				return true;
			}
			cacheModifledDate = cacheContentNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
			Calendar now = Calendar.getInstance();
			long cacheModifiedTime = now.getTimeInMillis() - cacheModifledDate.getTimeInMillis();
			return (cacheModifiedTime > cacheTimeout * 1000);
		} catch (RepositoryException e) {
			LOGGER.error("Fail to get cache modified date.");
			return true;
		}
	}

	private CacheDataUtil() {
	}
}
