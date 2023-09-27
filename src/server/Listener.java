package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import utils.Scribe;

public class Listener extends Thread {
	
	@SuppressWarnings("unused")
	private Socket Socket = null;
	private int Idx = 0;
	private Table Table = null;
	private String Name = null;
	private DataOutputStream OutStream = null;
	private DataInputStream InStream = null;
	
	private Scribe Logger = null;
	
	private void SendMessage(String message) throws IOException {
		this.OutStream.writeUTF(message);
		this.OutStream.flush();
	}
	
	private String WaitMessage() throws IOException {
		return this.InStream.readUTF();
	}
	
	public Listener(Socket s, Table t, int idx, String name, Scribe logger) throws IOException {
		this.Socket = s;
		this.Table = t;
		this.Idx = idx;
		
		this.OutStream = new DataOutputStream(s.getOutputStream());
		this.InStream = new DataInputStream(s.getInputStream());
		
		this.Logger = logger;
		
		this.Name = name;
		this.SendMessage(name);
		this.SendMessage(Integer.toString(this.Idx));
	}
	
	public int GetIdx() {
		return this.Idx;
	}
	
	public String GetName() {
		return this.Name;
	}
	
	private void LogError(String message) {
		System.err.println(this.Name + " [" + this.Idx + "] " + message);
	}
	
	private void LogMessage(String message) {
		this.Logger.Log(this.Name + " [" + this.Idx + "] " + message + "\n");
	}
	
	public void run() {
		String command = null;
		int leftFork = this.Idx;
		int rightFork = (this.Idx + 1) % this.Table.GetSize();
		
		String leftFork_s = Integer.toString(leftFork);
		String rightFork_s = Integer.toString(rightFork);
		
		// start the philosopher associated with the listener
		while(true) {
			try {
				this.SendMessage("START");
				break;
			} catch(IOException e) {
				continue;
			}
		}
		
		
		while(true) {
			// main execution loop: listen to the associated socket
			try {
				command = this.WaitMessage();
			} catch(IOException e) {
				this.LogError("Caught an IOException while listening to the socket. "
						+ "Connection may be closed. Stoping the listener and releasing forks.");
				try {
					this.Table.ReleaseForks(leftFork, rightFork);
				} catch(Exception f) {}
				break;
			}
			
			try {
				//this.LogMessage("I received " + command);
				// now interpret the command !
				if(command.equals("EAT")) {
					// try to grab the forks...
					this.LogMessage("wants to eat with forks " + leftFork_s + " and " + rightFork_s + ".");
					this.Table.GrabForks(leftFork, rightFork);
					// and notify the philosopher when it's ready
					this.LogMessage("begins to eat.");
					this.SendMessage("EAT WELL");
					
				} else if (command.equals("*BURP*")) {
					// this philosopher has eaten well. time to release the forks.
					this.Table.ReleaseForks(leftFork, rightFork);
					// and notify the philosopher
					this.LogMessage("releases forks " + leftFork_s + " and " + rightFork_s + ".");
					this.SendMessage("THINK WELL");
				} else {
					this.LogError("Server received a cryptic command while listening: " + command);
					continue;
				}
			} catch(IOException e) {
				this.LogError("Caught an IOException while interpreting the command " + command + ". Stopping the thread.");
				try {
					this.Table.ReleaseForks(leftFork, rightFork);
				} catch(Exception f) {}
				break;
			}
		}
	
	}
}
