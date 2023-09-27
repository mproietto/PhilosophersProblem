package client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import utils.Scribe;

/*
 * TODO: instead of the hardcoded 'localhost', add the connection address as an executable parameter
 * */

public class Client {

	private static int MaxPhilo = 20;
	private static int Port = 50000;
	private static String HelpMessage = 
			"Incorrect executable arguments. Correct usage is:\n"
			+ "java -jar Client.jar NPhilo [Outputfile],\n"
			+ "and Outputfile is an optional argument giving the name of the output file. If none is provided, logs"
			+ "will be displayed to the standard output.";
	
	public static void main(String[] args) throws IOException {
		
		int nrPhilosophers = 0;
		Scribe logger = null;
		
		{
			// parse the arguments
			if(args.length != 1 && args.length != 2) {
				System.out.println(Client.HelpMessage);
				return;
			}
			try {
				nrPhilosophers = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				System.out.println(Client.HelpMessage);
				return;
			}
			
			if(nrPhilosophers <= 0 || nrPhilosophers > Client.MaxPhilo) {
				System.out.println(Client.HelpMessage);
				return;
			}
			
			if(args.length == 2) {
				// if provided, write to the output file
				logger = new Scribe(new File(args[1]));
			} else {
				// if no output file is provided, write to standard output
				logger = new Scribe(System.out);
			}
		}
		
		Vector<Philosopher> philo = new Vector<Philosopher>();
		
		{
			// connect to the server and create the different philosophers
			Socket s = null;
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName("localhost");	
			} catch(UnknownHostException e) {
				System.err.println("Unknown host exception happened when trying to connect to the server.");
				e.printStackTrace();
				return;
			}
			
			try {
				for(int i=0; i<nrPhilosophers; i++) {
					s = new Socket(addr, Client.Port);
					Philosopher newPhilo = new Philosopher(s, logger);
					newPhilo.start();
					philo.add(newPhilo);
				}
			} catch(IOException e) {
				System.err.println("IOException happened when trying to connect to the server.");
				e.printStackTrace();
				return;
			}

		}
		
		for(int i=0; i<nrPhilosophers; i++) {
			try {
				philo.get(i).join();
			} catch(Exception e) {}
		}
		
		for(int i=0; i<nrPhilosophers; i++) {
			Philosopher philosopher = philo.get(i);
			logger.Log("Philosopher " + philosopher.GetName() + " [" 
						+ Integer.toString(philosopher.GetIdx()) + "] has eaten "
						+ Integer.toString(philosopher.GetPhases()) + " times.\n");
		}
	}

}
