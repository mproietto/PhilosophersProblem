package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class Scribe {
	private PrintStream stream = null;
	private boolean IoError = false;
	
	public Scribe(File f) throws FileNotFoundException {
		// the scribe will write to a file
		this.stream = new PrintStream(f);
	}
	
	public Scribe(PrintStream out) {
		this.stream = new PrintStream(out);
	}
	
	public boolean GetIoError() {
		return this.IoError;
	}
	
	public void ResetIoError() {
		this.IoError = false;
	}
	
	public synchronized void Log(String message) {
		try {
			this.stream.write(message.getBytes(Charset.forName("UTF-8")));
		} catch (IOException e) {
			this.IoError = true;
		}
	}
}
