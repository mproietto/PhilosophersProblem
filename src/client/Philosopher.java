package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import utils.Scribe;

public class Philosopher extends Thread {
	
	private static int MaxReflexionTime = 256;
	private static int MaxEatTime = 256;
	
	@SuppressWarnings("unused")
	private Socket Socket = null;
	private int Idx = 0;
	private String Name = null;
	private DataOutputStream OutStream = null;
	private DataInputStream InStream = null;
	
	private Scribe Logger = null;
	
	private int Phases = 0;
	
	private void SendMessage(String message) throws IOException {
		this.OutStream.writeUTF(message);
		this.OutStream.flush();
	}
	
	private String WaitMessage() throws IOException {
		return this.InStream.readUTF();
	}
	
	public int GetPhases() {
		return this.Phases;
	}
	
	public String GetName() {
		return this.Name;
	}
	
	public int GetIdx() {
		return this.Idx;
	}
	
	private void LogMessage(String message) {
		this.Logger.Log(this.Name + " [" + Integer.toString(this.Idx) + "] " + message + "\n");
	}
	
	public Philosopher(Socket s, Scribe logger) throws IOException {
		this.Socket = s;
		
		this.OutStream = new DataOutputStream(this.Socket.getOutputStream());
		this.InStream = new DataInputStream(this.Socket.getInputStream());
		
		this.Logger = logger;
		
		// now register to the server
		this.Name = this.WaitMessage();
		String idxStr = this.WaitMessage();
		try {
			this.Idx = Integer.valueOf(idxStr);	
		} catch(NumberFormatException e) {
			System.err.println("Unable to register, incorrect index number received: " + idxStr);
		}
	}
	
	public void run() {
		
		int eatTime = 0;
		int reflexionTime = 0;
		String message = null;
		
		// synchronize with the server
		try {
			this.WaitMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			
			reflexionTime = new Random().nextInt(Philosopher.MaxReflexionTime);
			this.LogMessage("begins to think for " + Integer.toString(reflexionTime) + " ms.");
			try { 
				Thread.sleep(reflexionTime); 
			} catch(Exception e) {
				break;
			}
			
			this.LogMessage("is famished, wants to grab forks.");
			
			try {
				this.SendMessage("EAT");
				message = this.WaitMessage();
			} catch (IOException e) {
				this.LogMessage("an exception happened when communicating with the server. "
						+ "Maybe the connection was closed. Interrupting the thread.");
				return;
			}
			
			if(!message.equals("EAT WELL")) {
				this.LogMessage("Received a cryptic message: " + message + ". Interruption the thread.");
				break;
			}
			
			eatTime = new Random().nextInt(Philosopher.MaxEatTime);
			this.LogMessage("begins to eat for " + Integer.toString(eatTime) + " ms.");
			
			try { 
				Thread.sleep(eatTime); 
			} catch(Exception e) {
				break;
			}
			this.LogMessage("has eaten well.");
			
			try {
				this.SendMessage("*BURP*");
				message = this.WaitMessage();
			} catch (IOException e) {
				this.LogMessage("an exception happened when communicating with the server. "
						+ "Maybe the connection was closed. Interrupting the thread.");
				return;
			}
			
			if(!message.equals("THINK WELL")) {
				this.LogMessage("Received a cryptic message: " + message + ". Interruption the thread.");
				break;
			}
			
			this.Phases++;
		}
		
	}
}
