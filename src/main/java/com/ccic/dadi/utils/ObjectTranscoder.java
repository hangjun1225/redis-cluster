package com.ccic.dadi.utils;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTranscoder {

	private static Logger LOGGER = LoggerFactory.getLogger(ObjectTranscoder.class);

	public static byte[] serialize(Object value) {
		if (value == null) {
			throw new NullPointerException("Can't serialize null");
		}
		byte[] rv = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream os = null;
		try {
			bos = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bos);
			os.writeObject(value);
//			os.close();
//			bos.close();
			rv = bos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Non-serializable object", e);
		} finally {
			try {
				if (os != null)
					os.close();
				if (bos != null)
					bos.close();
			} catch (Exception e2) {
				LOGGER.info("xxxx", e2);
			}
		}
		return rv;
	}

	public static Object deserialize(byte[] in) {
		Object rv = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if (in != null) {
				bis = new ByteArrayInputStream(in);
				is = new ObjectInputStream(bis);
				rv = is.readObject();
//				is.close();
//				bis.close();
			}
		} catch (Exception e) {
			LOGGER.info("eeee", e);
		} finally {
			try {
				if (is != null)
					is.close();
				if (bis != null)
					bis.close();
			} catch (Exception e2) {
				LOGGER.info("eeee", e2);
			}
		}
		return rv;
	}
}