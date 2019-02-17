package com.macrokeys.netcode;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.macrokeys.netcode.TCPMessageProtocol;

public class TCPMessageProtocolTest {

	private static final int PORT = 25347;
	
	@Test
	public void testMessages() {
		ServerSocket sv = null;
		
		try {
			sv = new ServerSocket(PORT);
			Socket client = new Socket("localhost", PORT);
			Socket server = sv.accept();
			
			TCPMessageProtocol msgServer = new TCPMessageProtocol(server);
			TCPMessageProtocol msgClient = new TCPMessageProtocol(client);
			
			final byte[] msg1 = {1, 2, 3, 4, 5, 10, 20, 40};
			final byte[] msg2 = {};
			assertReceiveMessage(msgServer, msgClient, msg1);
			assertReceiveMessage(msgServer, msgClient, msg2);
			
		} catch(IOException e) {
			fail("IOException");
		} finally {
			if(sv != null) {
				try {
					sv.close();
				} catch(IOException e) {
					
				}
			}
		}
	}
	
	@Test
	public void testTriggerTimeout() {
		ServerSocket sv = null;
		
		try {
			sv = new ServerSocket(PORT);
			Socket client = new Socket("localhost", PORT);
			Socket server = sv.accept();
			
			TCPMessageProtocol msgServer = new TCPMessageProtocol(server);
			TCPMessageProtocol msgClient = new TCPMessageProtocol(client);
			
			msgServer.setOutputKeepAlive(500);
			try {
				msgServer.receiveMessage();
				fail("SocketTimeoutException must be rised");
			} catch(SocketTimeoutException e) {
				
			}
			try {
				msgServer.sendMessage(new byte[] {1, 2, 3});
			} catch(IOException e) {
				fail("It should work");
			}
			
		} catch(IOException e) {
			fail("IOException");
		} finally {
			if(sv != null) {
				try {
					sv.close();
				} catch(IOException e) {
					
				}
			}
		}
	}
	
	
	@Test
	public void testPreventTimeout() {
		ServerSocket sv = null;
		
		try {
			sv = new ServerSocket(PORT);
			Socket client = new Socket("localhost", PORT);
			Socket server = sv.accept();
			
			TCPMessageProtocol msgServer = new TCPMessageProtocol(server);
			TCPMessageProtocol msgClient = new TCPMessageProtocol(client);
			
			Thread thSrv = new Thread(new ThReceiveTimeout(msgServer));
			Thread thClient = new Thread(new ThReceiveTimeout(msgClient));
			
			thSrv.start();
			thClient.start();
			
			msgServer.setOutputKeepAlive(500);
			msgClient.setInputKeepAlive(500);
			
			Thread.sleep(2000);
			
			msgServer.setOutputKeepAlive(250);
			msgClient.setInputKeepAlive(250);
			
			Thread.sleep(2000);
			
			msgServer.setOutputKeepAlive(1000);
			msgClient.setInputKeepAlive(1000);
			
			Thread.sleep(3000);
			
		} catch(SocketTimeoutException e) {
			fail("Must not trigger timeout");
		} catch(IOException e) {
			fail("IOException");
		} catch(InterruptedException e) {
			fail("Should not be interrupted");
		} finally {
			if(sv != null) {
				try {
					sv.close();
				} catch(IOException e) {
					
				}
			}
		}
	}
	
	private static class ThReceiveTimeout implements Runnable {
		
		private final TCPMessageProtocol m;
		
		public ThReceiveTimeout(TCPMessageProtocol m) {
			this.m = m;
		}
		
		@Override
		public void run() {
			try {
				m.receiveMessage();
			} catch(SocketTimeoutException e) {
				fail("SocketTimeoutException should not be raised");
			} catch(IOException e) {
				fail("IOException should not be raised");
			}
			fail("Sould not receive anyting");
		}
	}
	
	@Test
	public void testComunication() {
		
		final byte[] MESS1 = new byte[] { 100, 101, 102, 103, 104 };
		final byte[] MESS2 = new byte[] { 1, 2, 3, 4, 5 };
		final int NUM_IT = 10;
		final int TIMEOUT = 100;
		
		Thread thServer = new Thread(new Runnable() {
			
			ServerSocket sv = null;
			
			@Override
			public void run() {
				try {
					sv = new ServerSocket(PORT);
					Socket client = sv.accept();
					TCPMessageProtocol msgServer = new TCPMessageProtocol(client);
					msgServer.setInputKeepAlive(TIMEOUT);
					msgServer.setOutputKeepAlive(TIMEOUT);
					
					msgServer.sendMessage(MESS1);
					
					System.out.println("Test server: MESS1 sent");
					
					for(int i = 0; i < NUM_IT; i++) {
						System.out.println("Test server: start listening MESS2");
						byte[] mess = msgServer.receiveMessage();
						System.out.println("Test server: received MESS2");
						assertTrue(Arrays.equals(mess, MESS2));
					}
				} catch(IOException e) {
					e.printStackTrace();
					fail("IOException");
				} finally {
					if(sv != null) {
						try { sv.close(); }
						catch(IOException e) {
							// Nienete
						}
					}
				}
			}
		});
		
		Thread thClient = new Thread(new Runnable() {
			
			Socket client;
			
			@Override
			public void run() {
				try {
					client = new Socket("localhost", PORT);
					TCPMessageProtocol msgClient = new TCPMessageProtocol(client);
					msgClient.setInputKeepAlive(TIMEOUT);
					msgClient.setOutputKeepAlive(TIMEOUT);
					
					byte[] mess1 = msgClient.receiveMessage();
					assertTrue(Arrays.equals(mess1, MESS1));
					
					System.out.println("Test client: MESS1 receved");
					
					for(int i = 0; i < NUM_IT; i++) {
						Thread.sleep(TIMEOUT * 2);
						System.out.println("Test client: sending MESS2");
						msgClient.sendMessage(MESS2);
						System.out.println("Test client: sent MESS2");
					}
				} catch(IOException | InterruptedException e) {
					e.printStackTrace();
					fail("IOException or InterruptedException");
				} finally {
					if(client != null) {
						try { client.close(); }
						catch(IOException e) {
							// Nienete
						}
					}
				}
			}
		});
		
		
		thServer.start();
		thClient.start();
		
		try {
			thClient.join();
			thServer.join();
		} catch (InterruptedException e) {
			fail("Should not be interrupted");
		}
		
	}
	
	
	
	
	
	
	
	
	private static void assertReceiveMessageAB(TCPMessageProtocol a, TCPMessageProtocol b,
			byte[] msg) throws IOException {
		a.sendMessage(msg);
		byte[] r = b.receiveMessage();
		
		assertTrue(Arrays.equals(msg, r), "Messages must be equal");
	}
	
	
	private static void assertReceiveMessage(TCPMessageProtocol a, TCPMessageProtocol b,
			byte[] msg) throws IOException {
		assertReceiveMessageAB(a, b, msg);
		assertReceiveMessageAB(b, a, msg);
	}
	
	/*
	private static PP connect() throws IOException {
		ServerSocket sv = new ServerSocket(PORT);
		Socket client = new Socket("localhost", PORT);
		Socket server = sv.accept();
		
		TCPMessageProtocol msgServer = new TCPMessageProtocol(server);
		TCPMessageProtocol msgClient = new TCPMessageProtocol(client);
		
		return new PP(msgServer, msgClient);
	}

	
	private static class PP {
		public PP(TCPMessageProtocol msgServer,
				TCPMessageProtocol msgClient) {
			this.client = msgClient;
			this.server = msgServer;
		}

		TCPMessageProtocol client, server;
	}*/
}
