package server;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
	private Vector<ReentrantLock> Forks = new Vector<ReentrantLock>();
	private int Size = 0;
	
	public int GetSize() {
		return this.Size;
	}
	
	public Table(int size) {
		for(int i=0; i<size; i++) {
			this.Forks.add(new ReentrantLock());
		}
		this.Size = size;
	}
	
	public synchronized void GrabForks(int idx1, int idx2) {
		assert 0 <= idx1 && idx1 < this.Size;
		assert 0 <= idx2 && idx2 < this.Size;
		
		ReentrantLock l1 = Forks.get(idx1);
		ReentrantLock l2 = Forks.get(idx2);
		
		while(true){
			
			boolean canEat = true;
			if(!l1.tryLock())
			{
				canEat = false;
			} else if (!l2.tryLock()){
				// put down the first fork
				l1.unlock();
				canEat = false;
			}
			
			if(canEat) {
				break;
			}
			
			// we don't want to hold forks if we don't have both
			try { wait(); } catch(Exception e) {
				return;
			}
			
		}
	}
	
	public synchronized void ReleaseForks(int idx1, int idx2) {
		assert 0 <= idx1 && idx1 < this.Size;
		assert 0 <= idx2 && idx2 < this.Size;
		
		ReentrantLock l1 = Forks.get(idx1);
		ReentrantLock l2 = Forks.get(idx2);
		
		l1.unlock();
		l2.unlock();
		
		// tell the listeners that they can try to grab a fork
		notifyAll();
	}
}
