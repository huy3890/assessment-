package com.aem.assessment.core.utils;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.assessment.core.beans.BaseBean;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourceUtil {
	private static final ObjectMapper _OBJECT_MAPPER = new ObjectMapper();

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

	private ResourceUtil() {
	}

	public static <T extends BaseBean> T contentJsonStrToObjectT(String strJsonContent, Class<T> contentClass) {
		if (StringUtils.isBlank(strJsonContent)) {
			return null;
		}
		try {
			return _OBJECT_MAPPER.readValue(strJsonContent, contentClass);
		} catch (IOException e) {
			LOGGER.error("Can't convert JSON to Java object", e.getCause());
			return null;
		}
	}

	public static String objectToJson(final Object input) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(input);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return StringUtils.EMPTY;
	}
}
