package com.macrokeys;

import static org.junit.Assert.*;

import org.junit.Test;

public class MacroKeyTest {


	@Test
	public void testSetId() {
		MacroKey m = new MacroKey();
		try {
			m.setId(-1);
		} catch(IllegalArgumentException e) {
			//OK
		} catch(Exception e) {
			fail("Exception not correct");
		}
		
		m.setId(0);
		assertTrue(m.getId() == 0);
	}

	@Test
	public void testSetColorFill() {
		MacroKey m = new MacroKey();
		m.setColorFill(100);
		assertTrue(m.getColorFill() == 100);
		m.setColorFill(0);
		assertTrue(m.getColorFill() == 0);
		m.setColorFill(2234234);
		assertTrue(m.getColorFill() == 2234234);
	}

	@Test
	public void testSetArea() {

	}

	@Test
	public void testSetShape() {
		
	}

	@Test
	public void testSetColorEdge() {
		
	}

	@Test
	public void testSetColorEdgePress() {
		
	}

	@Test
	public void testSetColorFillPress() {
		
	}

	@Test
	public void testSetText() {
		
	}

	@Test
	public void testSetKeySeq() {
		
	}

	@Test
	public void testClone() {
		
	}

}
