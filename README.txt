+----------------------------------+
|	    SLR201 Project 	   |
+----------------------------------+

The Remote Diner of the Philosophers
*----------------------------------*

Authors: Marco Proietto, Gaspard Thevenon
~~~

This project implements the problem of the dining philosophers, using remote communication.
It is implemented in Java, and uses raw sockets and I/O streams for communication.

Structure of the project
************************

The project has the following structure:

- `src/` contains the source files of the project. It contains three subfolders:
	| `src/client/`: contains the java source files for the clients
	| `src/server/`: contains the java source files for the server
	| `src/utils/`: contains common utilitary source files
- `bin/` contains the java .jar files after compilation
- `doJars.sh` is a shell script to build the jar files
- `README.md` is this file

Compile the project
*******************

A script file is provided to create the jar files.
It only depends on `bash`, on `javac`, and on `jar`, and requires no argument:

`$ ./doJars.sh`

Please ensure that it is executable.

Run the project
***************

Once the project is compiled, there will be two jar files in the bin directory:
Server.jar and Client.jar. Their uses is the following:

`Server.jar`:
It runs the server. The server can be symbolically as the table, it holds the shared
resources (typically the *forks*), and will receive requests from the philosophers.
There should be one instance of the server per diner.

The executable expects one or two arguments. The first is a positive integer giving the number
of philosophers to wait for. The second is optional and precises an output file for logs.
If no log file is provided, logs will be displayed in the standard output.
For example, to wait for 10 philosophers and write logs to server.log, one can run:

`$ java -jar bin/Server.jar 10 server.log`

Once the number of philosophers to wait for is reached, the dinner will begin.
All philosophers will be notified, and the server will start listening to requests.


`Client.jar`:
It runs one client. There can be several client instances per diner.
A client instance represents a group of philosophers.

The executable expects one or two arguments. The first is a positive integer giving the number
of philosophers in the client instance. The second argument is the same as for Server.jar.
For example, if the server waits for 10 philosophers, one can run two 
client instances (here in background):

`$ java -jar bin/Client.jar 7 client1.log&`
`$ java -jar bin/Client.jar 3 client2.log&`

Here, as the server is waiting for 10 philosophers, it will detect the two clients reaching it,
the first with 7 philosophers and the second with 3 philosophers. After that, the server will detect
that all expected philosophers are present, and the diner will begin.

Precisions on the implementation
********************************

In this section, we will detail the implementation a little bit.

Once the server is created, it waits for clients to connect. Once a client connects,
the server initiates one "Listener" per philosopher. A Listener runs on a specific thread,
and will be responsible for listening to the commands of one philosopher, and addressing them.

When an instance of a client is run, it creates Philosopher instances. A Philosopher runs on
a specific thread, and is responsible for eating (and thinking). The client notifies the server
for each philosopher created, and the server replies by acknowledging it (and by giving the
philosopher a unique name at the table).

There is a one-one correspondance between Client Philosophers and Server Listeners.

A Philosopher and a Listener communicate via simple messages, transfered via I/O streams
and sockets. The messages are:

- "EAT" (Philosopher -> Listener): request from the Philosopher to eat. The associated
	| listener tries to acquire the forks.
- "EAT WELL" (Listener - Philosopher): notification from the Listener that the forks were
	| acquired and that the Philosopher can begin to eat.
- "*BURP*" (Philosopher -> Listener): notification from the Philosopher that they have finished
	| eating and that the Listener can release the forks
- "THINK WELL" (Listener -> Philosopher): acknowledgement from the Listener that the forks were
	| released, and that one cycle has been completed.

A Logger instance is associated to the server and to each client. It is responsible for
the logging part, ensuring safe concurrent access to the output file - with INTRA-process 
synchronization, not if another process tries to access this file.

The forks synchronizations are handled by the server. On the server, there is one Table instance,
which holds a fixed number of forks (which are actually ReentrantLocks). When a Listener tries
to grab forks for a Philosopher, it executes a *synchronized* method of the table to try to
acquire the two forks. During the method, it tries to lock the two forks. If one of them is locked,
it releases the other when necessary, and *wait()*. When it wakes up, it tries again, until it
can lock both forks, and exit the method.
Another *synchronized* method is executed by the Listener to release forks. It simply unlock the
forks, and proceeds to *notifyAll()* Listener which may stuck trying to grab forks, so they
can try again.
