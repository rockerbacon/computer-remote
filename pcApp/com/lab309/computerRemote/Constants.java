package com.lab309.computerRemote;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteArrayConverter;

/**
 * Created by Vitor Andrade dos Santos on 3/27/17.
 */

public class Constants {
	public static final int broadcastPort = 0x4343;	//representa a string "CC"

	private static final String helloMessage = "CChello";
	private static final String connectMessage = "CCConnect";
	
	public static final int answerTimeLimit = 2000;
	public static final int wrongAnswerLimit = 20;

	public static final int maxName = 64*SizeConstants.sizeOfChar;
	public static final int maxCommandArgSize = 1*1024*1024;	//1mb
	
	public static final int broadcastBufferSize = SizeConstants.sizeOfString(connectMessage)+SizeConstants.sizeOfInt+maxName;
	public static final int commandBufferSize = SizeConstants.sizeOfByte+maxCommandArgSize;
	
	public static final int commandQueueSize = 10;
	
	public static final int publicKeySize = 32;	//size of the public key used in bytes

	/*COMANDOS*/
}
