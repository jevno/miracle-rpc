package com.miracle.module.rpc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.miracle.module.rpc.common.support.FilterInf;
import com.miracle.module.rpc.common.support.Listener;

public class ExtensionLoaderTest {

	@Test
	public void testGetExtensionInstances() {
		List<Object> extObjs = ExtensionLoader.getExtensionInstances(Listener.class, new String[]{"my", "your"});
		System.out.println(extObjs);
		assertEquals(2, extObjs.size());
		assertTrue(extObjs.get(0) instanceof Listener);
		assertTrue(extObjs.get(1) instanceof Listener);
		
		List<Object> extFilterInfs = ExtensionLoader.getExtensionInstances(FilterInf.class, new String[]{"ext1", "ext2"});
		System.out.println(extFilterInfs);
		assertEquals(2, extFilterInfs.size());
		assertTrue(extFilterInfs.get(0) instanceof FilterInf);
		assertTrue(extFilterInfs.get(1) instanceof FilterInf);
	}
	
	@Test
	public void testGetExtensionClasses() {
		Map<String, Class<?>> extListeners = ExtensionLoader.getExtensionClss(Listener.class);
		System.out.println(extListeners);
		assertEquals(2, extListeners.size());
		assertTrue(Listener.class.isAssignableFrom(extListeners.get("my")));
		assertTrue(Listener.class.isAssignableFrom(extListeners.get("your")));
	}

}
