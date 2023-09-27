package server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;
import utils.Scribe;

public class Server {
	
	private static String[] PhilosopherNames =
		{"Spinoza", "Descartes", "Platon", "Russell", "Socrate", "De Beauvoir", "Kierkegaard", "Bergson",
		 "Camus", "Kant", "Locke", "Strauss", "Pascal", "Leibniz", "Seneque", "Voltaire", "Machiavel", "Nietzsche", "Marx",
		 "Confucius", "Hume", "Wittgenstein", "Wollstonecraft", "Lao Tseu", "Emerson", "Hobbes", "Bentham", "Diogenes"};
	private static Vector<String> RemainingPhilosopherNames = 
			new Vector<String>(Arrays.asList(Server.PhilosopherNames));

	private static int MaxPhilo = 20;
	private static int Port = 50000;
	private static String HelpMessage = 
			"Incorrect executable arguments. Correct usage is:\n"
			+ "java -jar Server.jar NPhilo [Outputfile],\n"
			+ "where NPhilo is the expected number of philosophers (between 1 and " + MaxPhilo + ")\n"
			+ "and Outputfile is an optional argument giving the name of the output file. If none is provided, logs"
			+ "will be displayed to the standard output.";
	private static Table table = null;
	
	private static String NewName(int idx) {
		String name = null;
		if(Server.RemainingPhilosopherNames.isEmpty()) {
			// there are too many philosophers !
			name = "Philosopher#" + Integer.toString(idx);
		} else {
			int nameidx = new Random().nextInt(Server.RemainingPhilosopherNames.size());
			name = Server.RemainingPhilosopherNames.get(nameidx);
			Server.RemainingPhilosopherNames.remove(nameidx);
		}
		return name;
	}
	
	private static Listener WaitForNewPhilosopher(ServerSocket serverSocket, int idx, Scribe logger) throws IOException {
		Socket socket = serverSocket.accept();
		String name = Server.NewName(idx);
		return new Listener(socket, Server.table, idx, name, logger);
	}
	
	public static void main(String[] args) throws IOException {
		

		int nrPhilosophers = 0;
		Scribe logger = null;
		
		// parse the argument
		{
			if(args.length != 1 && args.length != 2) {
				System.out.println(Server.HelpMessage);
				return;
			}
			try {
				nrPhilosophers = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				System.out.println(Server.HelpMessage);
				return;
			}
			
			if(nrPhilosophers <= 0 || nrPhilosophers > Server.MaxPhilo) {
				System.out.println(Server.HelpMessage);
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
		
		// display server address
		InetAddress myAddr = InetAddress.getLocalHost();
		System.out.println("Server is up on local host " + myAddr.toString() + " on port " + Server.Port);
		
		// set the table
		table = new Table(nrPhilosophers);
		
		Vector<Listener> listeners = new Vector<Listener>();
		
		{
			// block of code to wait for the philosophers to join
			ServerSocket serverSocket;
			
			// wait until the philosophers join the table
			int idx = 0;
			serverSocket = new ServerSocket(Server.Port);

			while(idx < nrPhilosophers) {
				
				logger.Log("Waiting for the next philosopher...\n");
				
				// 50000 : dedicated port
				Listener newPhilo = Server.WaitForNewPhilosopher(serverSocket, idx, logger);
				listeners.add(newPhilo);
				
				logger.Log(newPhilo.GetName() + 
						" [" + Integer.toString(newPhilo.GetIdx()) +
						"/" + Integer.toString(nrPhilosophers - 1) +
						"] has joined the table !\n");
				
				idx++;
			}

			serverSocket.close();
		}
		
		logger.Log("All philosophers are here. Beginning the dinner.\n");
		logger.Log("================================================\n");
		
		{
			// then start all the threads
			for(int i=0; i<nrPhilosophers; i++) {
				listeners.get(i).start();
			}
		}
		
		{
			// wait until the listeners are done
			for(int i=0; i<nrPhilosophers; i++) {
				try {
					listeners.get(i).join();
				} catch(Exception e) {}
			}
		}
		
		logger.Log("=============================================\n");
		logger.Log("All listeners are stopped. Closing the server.\n");
	}

}
