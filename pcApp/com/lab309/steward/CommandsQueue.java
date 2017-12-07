/*
 *	Class for managing insertion and removal of commands from a queue
 *	Any call to the removal of a command will block until the queue has something to be removed
 *	In case the queue is destroyed while there's a blocked thread waiting for it, an IllegalThreadStateException will be thrown
 *
 */

package com.lab309.Steward;

import com.lab309.adt.ConcurrentStaticQueue;

import com.lab309.network.UDPDatagram;

import java.net.SocketException;

public class CommandsQueue {
	/*ATRIBUTES*/
	private ConcurrentStaticQueue<UDPDatagram> queue;
	private Object popSignal;
	private boolean destroyed;
	
	/*CONSTRUCTORS*/
	public CommandsQueue (int size) {
		this.queue = new ConcurrentStaticQueue<UDPDatagram>(size);
		this.destroyed = false;
	}
	
	/*METHODS*/
	//blocks until a command is available to be processed
	public UDPDatagram pop () throws IllegalThreadStateException {
		UDPDatagram next;
		
		next = this.queue.pop();
		while (next == null) {
			synchronized (this.popSignal) { this.popSignal.wait(); }
			if (destroyed) throw new IllegalThreadStateException("Queue destroyed");
			next = this.queue.pop();
		}
		return command;
	}
	
	//throws an IndexOutOfBoundsException in case the queue was full and the command had to be discarded
	public void push (UDPDatagram command) throws IndexOutOfBoundsException {
		try {
			this.queue.push(command);	//possible IndexOutOfBoundsException
			synchronized (this.popSignal) { this.popSignal.notify(); }
		} catch (IndexOutOfBoundsException e) {
			throw e;
		}
	}
}
