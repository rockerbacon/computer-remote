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
	private static final String finishConnectMessage = "CCfconnect";
	private static final String connectionCheckMessage = connectMessage;
	
	public static final int answerTimeLimit = 500;
	public static final int wrongAnswerLimit = 10;

	public static final int maxName = 64*SizeConstants.sizeOfChar;
	public static final int maxCommandArgSize = 1*1024*1024;	//1mb
	public static final int maxErrorMessage = 256*SizeConstants.sizeOfChar;
	
	public static final int broadcastBufferSize = SizeConstants.sizeOfString(helloMessage);
	public static final int connectionBufferSize = SizeConstants.sizeOfString(connectMessage)+SizeConstants.sizeOfInt+Constants.maxName;
	public static final int commandBufferSize = SizeConstants.sizeOfByte+maxCommandArgSize;
	
	public static final long int connectionCheckInterval = 5*60000;
	
	public static final int commandQueueSize = 10;
	
	public static final int passwordSize = 4;	//size of the password in bytes
	public static final int publicKeySize = 32;	//size of the public key used in bytes

	/*COMANDOS*/
}
