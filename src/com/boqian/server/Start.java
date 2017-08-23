package com.boqian.server;

import java.io.IOException;

public class Start {
	
	public static void main(String[] args) {
		new Thread(){
			
			public void run(){
				try {
					LoginServer.openServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}.start();
		
		new Thread(){
			
			public void run(){
				try {
					RegServer.openServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}.start();
	}

}
