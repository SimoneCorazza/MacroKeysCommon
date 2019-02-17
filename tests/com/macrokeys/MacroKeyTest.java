package com.macrokeys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MacroKeyTest {


	@Test
	public void shouldSetId() {
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
	public void shouldSetColorFill() {
		MacroKey m = new MacroKey();
		m.setColorFill(100);
		assertEquals(100, m.getColorFill());
		
		m.setColorFill(0);
		assertEquals(0, m.getColorFill());
		
		m.setColorFill(2234234);
		assertEquals(2234234, m.getColorFill());
	}

}
