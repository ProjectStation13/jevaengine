package io.github.jevaengine.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class MapBuilderTest
{
	@Test
	public void testPut()
	{
		Map<String, Integer> putTest = new MapBuilder<String, Integer>().a("one", 1).a("three", 3).a("four", 4).get();

		assertEquals(new Integer(1), putTest.get("one"));
		assertEquals(new Integer(3), putTest.get("three"));
		assertEquals(new Integer(4), putTest.get("four"));
	}
}
