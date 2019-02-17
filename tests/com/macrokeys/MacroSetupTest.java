package com.macrokeys;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.macrokeys.rendering.RectF;

public class MacroSetupTest {

	@Test
	public void testSaveSetup() {
		try {			
			MacroScreen screen = new MacroScreen();
			MacroKey k = new MacroKey();
			LimitedKeySequence seq = new LimitedKeySequence(new Integer[] { KeyEvent.VK_V });
			k.setKeySeq(seq);
			screen.getKeys().add(k);
			List<MacroScreen> l = new ArrayList<>();
			l.add(screen);
			MacroSetup setupOut = new MacroSetup(l);
			
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			setupOut.save(out);
			ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
			MacroSetup setupIn = MacroSetup.load(input);
			
			assertTrue(setupIn.getMacroScreens().size() == l.size());
			MacroScreen m = setupIn.getMacroScreens().get(0);
			assertTrue(m.getKeys().size() == 1);
			MacroKey key = m.getKeys().get(0);
			
			assertTrue(key.getKeySeq().getKeys().size() == 1);
			assertTrue(key.getKeySeq().getKeys().get(0) == KeyEvent.VK_V);
		} catch (IOException e) {
			
		} catch (MSLoadException e) {
			
		}
	}
	
	@Test
	public void testFitFor() {
		final float F_X = 50, F_Y = 100;
		
		MacroScreen s1 = new MacroScreen();
		MacroKey k11 = new MacroKey();
		k11.setArea(new RectF(0, 0, 100, 100));
		MacroKey k12 = new MacroKey();
		k12.setArea(new RectF(0, 0, 200, 200));
		MacroKey k13 = new MacroKey();
		k13.setArea(new RectF(40, 110, 80, 150));
		s1.getKeys().add(k11);
		s1.getKeys().add(k12);
		s1.getKeys().add(k13);
		
		MacroScreen s2 = new MacroScreen();
		MacroKey k21 = new MacroKey();
		k21.setArea(new RectF(0, 0, 100, 50));
		MacroKey k22 = new MacroKey();
		k22.setArea(new RectF(0, 0, 100, 200));
		s2.getKeys().add(k21);
		s2.getKeys().add(k22);
		
		ArrayList<MacroScreen> lst = new ArrayList<>();
		lst.add(s1);
		lst.add(s2);
		MacroSetup m = new MacroSetup(lst).fitFor(F_X, F_Y);
		
		assertFit(m, F_X, F_Y);
	}
	
	@Test
	public void testFitFor_ExeedeOnX() {
		final float F_X = 50, F_Y = 100;
		
		MacroScreen s = new MacroScreen();
		for(int y = 0; y <= F_Y + 10; y += 10) {
			RectF r = new RectF(0, y, F_X, y + 10);
			MacroKey k = new MacroKey();
			k.setArea(r);
			s.getKeys().add(k);
		}
		
		ArrayList<MacroScreen> lst = new ArrayList<>();
		lst.add(s);
		MacroSetup m = new MacroSetup(lst).fitFor(F_X, F_Y);
		
		assertFit(m, F_X, F_Y);
	}
	
	@Test
	public void testFitFor_ExeedeOnY() {
		final float F_X = 50, F_Y = 100;
		
		MacroScreen s = new MacroScreen();
		for(int x = 0; x <= F_X + 10; x += 10) {
			RectF r = new RectF(x, 0, x + 10, F_Y);
			MacroKey k = new MacroKey();
			k.setArea(r);
			s.getKeys().add(k);
		}
		
		ArrayList<MacroScreen> lst = new ArrayList<>();
		lst.add(s);
		MacroSetup m = new MacroSetup(lst).fitFor(F_X, F_Y);
		
		assertFit(m, F_X, F_Y);
	}
	
	@Test
	public void testFitFor_AlreadyFit() {
		final float F_X = 50, F_Y = 100;
		final int sizeX = 10, sizeY = 10;
		
		MacroScreen s = new MacroScreen();
		
		for(int y = 0; y < F_Y - sizeY; y += sizeY) {
			for(int x = 0; x < F_X - sizeX; x += sizeX) {
				RectF r = new RectF(x, y, x + sizeX, y + sizeY);
				MacroKey k = new MacroKey();
				k.setArea(r);
				s.getKeys().add(k);
			}
		}
		
		ArrayList<MacroScreen> lst = new ArrayList<>();
		lst.add(s);
		MacroSetup m1 = new MacroSetup(lst);
		MacroSetup m2 = m1.fitFor(F_X, F_Y);
		
		try {
			assertNoChange(m1, (MacroSetup)m1.clone());
		} catch (CloneNotSupportedException e) {
			assert false;
		}
		assertNoChange(m1, m2);
	}
	
	@Test
	public void testEquals() {
		MacroScreen s1 = new MacroScreen();
		MacroKey k = new MacroKey();
		s1.getKeys().add(k);
		
		List<MacroScreen> l1 = new ArrayList<>();
		l1.add(s1);
		
		MacroSetup m1 = new MacroSetup(l1);
		try {
			assertTrue(m1.equals(m1.clone()));
		} catch (CloneNotSupportedException e) {
			fail("Must be supported");
		}
		
		
		
		MacroScreen s2 = new MacroScreen();
		MacroKey k2 = new MacroKey();
		k2.setColorEdge(4730236);
		s2.getKeys().add(k2);
		
		List<MacroScreen> l2 = new ArrayList<>();
		l2.add(s2);
		MacroSetup m2 = new MacroSetup(l2);
		
		assertFalse(m2.equals(m1));
	}
	
	
	private void assertFit(MacroSetup m, float w, float h) {
		for(MacroScreen s : m.getMacroScreens()) {
			for(MacroKey k : s.getKeys()) {
				assertTrue(k.getArea().right <= w && k.getArea().bottom <= h);
			}
		}
	}
	
	private void assertNoChange(MacroSetup m1, MacroSetup m2) {
		assertTrue(m1.equals(m2));
	}

}
