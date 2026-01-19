package main.java.com.linkmanager.app.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamInterpreter extends Thread {
	private InputStream is;
	private String type;

	public StreamInterpreter(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(type + ">" + line);  
			}
 
		} 
		catch (IOException ioe) {
			ioe.printStackTrace();  
		}
	}
}