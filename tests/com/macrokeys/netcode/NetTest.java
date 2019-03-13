package com.macrokeys.netcode;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.macrokeys.LimitedKeySequence;
import com.macrokeys.MacroKey;
import com.macrokeys.MacroScreen;
import com.macrokeys.MacroSetup;
import com.macrokeys.netcode.MacroNetClient.SSDPServerInfo;

@Disabled
public class NetTest {

	@Test
	public void testServerDiscovery() {
		MacroNetServer s = null;
		try {
			MacroSetup setup = macroSetup(KeyEvent.VK_V);
			s = new MacroNetServer(setup);
			s.start();
			SSDPServerInfo[] servers = MacroNetClient.findServer(3000);
			assertTrue(servers.length == 1);
			SSDPServerInfo info = servers[0];
			
			String svrName = MacroNetServer.serverName();
			if(svrName.length() > NetStatic.SSDP_NAME_LENGTH) {
				svrName = svrName.substring(0, NetStatic.SSDP_NAME_LENGTH - 1);
			}
			info.name.equals(svrName);
		} catch (IOException | AWTException e) {
			fail("Eccezione generata");
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}
	
	
	
	@Test
	public void testConnection() {
		MacroNetServer s = null;
		try {
			/*
			KeyPresser p = new KeyPresser();
			LimitedKeySequence win = new LimitedKeySequence(new Integer[] { KeyEvent.VK_WINDOWS });
			LimitedKeySequence copy = new LimitedKeySequence(new Integer[] { KeyEvent.VK_CONTROL, KeyEvent.VK_V });
			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() - start < 1000) {
				p.press(win);
				p.press(copy);
				Thread.sleep(60);
			}
			*/
			
			MacroSetup setup = macroSetup(KeyEvent.VK_V);
			s = new MacroNetServer(setup);
			s.start();
			MacroNetClient c = new MacroNetClient("localhost");
			c.connectToServer();
			
			/*
			c.sendKeyStroke(setup.macroKeyFromID(0));
			c.sendKeyStroke(setup.macroKeyFromID(0));
			c.sendKeyStroke(setup.macroKeyFromID(0));
			*/
			
			
		} catch (Throwable t) {
			t.printStackTrace();
			if(s != null) {
				s.close();
				s = null;
			}
			fail("An exception occurred");
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}
	
	@Test
	public void testTimeout() {
		MacroNetServer s = null;
		try {
			MacroSetup setup = macroSetup(KeyEvent.VK_V);
			s = new MacroNetServer(setup);
			s.start();
			MacroNetClient c = new MacroNetClient("localhost");
			c.connectToServer();
			
			assertTrue(c.isConnected(), "Must be connected");
			
			try {
				Thread.sleep((long) (5000 * 1.1));
			} catch(InterruptedException e) {
				
			}
			
			assertTrue(c.isConnected(), "Must be connected");
			
		} catch (Throwable t) {
			t.printStackTrace();
			if(s != null) {
				s.close();
				s = null;
			}
			fail("An exception occurred");
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}
	
	
	/**
	 * Ottiene una {@link MacroSetup} contentente una {@link MacroScreen} contenente
	 * un {@link MacroKey} avente come sequenza di tasto il tasto {@code key}
	 * @param key Tasto associato alla {@link MacroKey} da inserire
	 * @return Setup aventi le proprietà descritte precedentemente; non null
	 */
	private MacroSetup macroSetup(int key) {
		MacroScreen screen = new MacroScreen();
		MacroKey k = new MacroKey();
		
		LimitedKeySequence seq = new LimitedKeySequence(new Integer[] { key });
		k.setKeySeq(seq);
		screen.getKeys().add(k);
		List<MacroScreen> l = new ArrayList<>();
		l.add(screen);
		
		return new MacroSetup(l);
	}
	
	
	@Test
	public void testSendKeys() {
		MacroNetServer s = null;
		try {
			MacroSetup setup = macroSetup(KeyEvent.VK_V);
			s = new MacroNetServer(setup);
			s.start();
			MacroNetClient c = new MacroNetClient("localhost");
			c.connectToServer();
			
			MacroKey k = setup.getMacroScreens().get(0).getKeys().get(0);
			
			c.keyDown(k);
			c.keyUp(k);
			
		} catch (Throwable t) {
			t.printStackTrace();
			if(s != null) {
				s.close();
				s = null;
			}
			fail("An exception occurred");
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

}
