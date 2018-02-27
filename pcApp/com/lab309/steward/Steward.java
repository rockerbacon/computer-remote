/*
 * Class containing all needed 
 *
 */

package com.lab309.steward;

import java.util.HashMap;

import java.lang.Runnable;

import java.awt.Robot;
import java.awt.AWTException;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteBuffer;

import com.lab309.network.UDPDatagram;

import java.io.IOException;

public class Steward {
	
	public static interface Command {
		//f should return a string describing the command issued
		public String execute (ByteBuffer buffer);
	}
	
	/*FUNCTIONS*/
	/*
	 *	Used to execute command lines, signature: (String commandLine)
	 *
	 */
	 public static final int writeLineCode = 0;
	 public static class WriteLine implements Command {
	 	private Runtime runtime;
	 	
	 	public WriteLine () {
	 		this.runtime = Runtime.getRuntime();
	 	}
	 	
	 	@Override
	 	public String execute (ByteBuffer buffer) {
	 		String line = buffer.retrieveString();
	 		try {
	 			this.runtime.exec(line);
	 			return "command executed successfully";
	 		} catch (IOException e) {
	 			return "issued invalid command line \"" + line + "\""; 
	 		}
	 	}	
	 }
	
	/*
	 *	Used to issue keyboard commands, signature: (int key, byte event)
	 *	See constants to know the event codes
	 */
	public static final int useKeyboardCode = 1;
	public static class UseKeyboard implements Command {
		private Robot robot;
		
		//constants
		public static final byte press = 0;
		public static final byte release = 1;
		public static final byte click = 2;
		
		public UseKeyboard () {
			try {
				this.robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public String execute (ByteBuffer buffer) {
			int key = buffer.retrieveInt();
			int event = buffer.retrieveByte();
			String eventStr = " ";
			switch (event) {
				case press:
					robot.keyPress(key);
					eventStr = "pressed ";
				break;
				case release:
					robot.keyRelease(key);
					eventStr = "released ";
				break;
				case click:
					robot.keyPress(key);
					robot.keyRelease(key);
					eventStr = "clicked ";
				break;
				default:
					return "issued invalid keyboard command " + event;
			}
			return eventStr + key;
		}
	}
	
	public static final int stopWorkingCode = -1;
	
	/*ATRIBUTES*/
	private CommandsQueue commands;
	private boolean working;
	private HashMap<Integer, Command> function;
	
	/*CONSTRUCTORS*/
	//the command queue must be a reference to a queue that's being populated by a server
	public Steward (CommandsQueue queue) {
		this.commands = queue;
		this.working = false;
		
		this.function = new HashMap<Integer, Command>();
		this.function.put(Steward.writeLineCode, new Steward.WriteLine());
		this.function.put(Steward.useKeyboardCode, new Steward.UseKeyboard());
	}
	
	/*GETTERS*/
	public Command getFunction (int code) {
		return this.function.get(code);
	}
	
	/*METHODS*/
	//starts a new thread to receive commands until a kill packet is received
	//kill packets can be sent through the function freeSteward()
	public void work () { new Thread ( new Runnable() {	public void run () {
		UDPDatagram packet;
		ByteBuffer command;
		String clientName;
		String result;
		int code;
		
		Steward.this.working = true;
		while (Steward.this.working) {
			try {
				//identify command
				packet = Steward.this.commands.pop();	//possible IllegalThreadStateException
				command = packet.getBuffer();
				clientName = command.retrieveString();
				
				//check command code
				code = command.retrieveInt();
				if (code == Steward.stopWorkingCode) {	//check for a kill command
					Steward.this.working = false;
					//log steward stop	
				} else {
					result = Steward.this.function.get(code).execute(command);	//execute command
					//log result
				}	
			} catch (IllegalThreadStateException | InterruptedException e) {
				Steward.this.working = false;
			} catch (IndexOutOfBoundsException e) {
				//this is an unwanted case where the network is extremely overloaded and the Stewards can't receive their correct kill packet
				//if this case is reached the queue size should be increased
				e.printStackTrace();
			}
		}
	}}).start();		
	}
	
	//A packet will be placed on the queue indicating that one Steward must stop working. The steward that gets the packet first is the one that will stop, no order is ensured
	//In case the packet cannot be placed on the queue because it's overloaded the steward issuing the command is the one who stops (although problems may occur, see comments in the function)
	public void freeSteward () {
		if (!this.working) {
			return;
		}
		UDPDatagram killPacket = new UDPDatagram(new ByteBuffer(SizeConstants.sizeOfString("localhost")+SizeConstants.sizeOfInt));
		killPacket.getBuffer().pushString("localhost");
		killPacket.getBuffer().pushInt(Steward.stopWorkingCode);
		try {
			this.commands.push(killPacket);
		} catch (IndexOutOfBoundsException e) {
			//will happen if queue is full in which case the Steward will be in the middle of processing packets and setting the variable will sufice as the thread won't be blocked for long
			//This assumes the number of Stewards is not larger than the queue size. In the opposite case the queue may be full and Stewards may still be in a waiting state
			Steward.this.working = false;
		}	
	}
	
}
