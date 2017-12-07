package com.lab309.computerRemote;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteArrayConverter;

/**
 * Created by Vitor Andrade dos Santos on 3/27/17.
 */

public class Constants {
	public static final int broadcastPort = 0x4343;	//representa a string "CC"

	private static final String appid = "ComputerControl";
	public static final byte[] applicationId = ByteArrayConverter.latinStringToArray(appid, new byte[SizeConstants.sizeOfLatinString(appid)], 0);
	
	public static final byte identityRequestCode = 1;
	public static final byte connectionRequestCode = 2;
	
	public static final int requestResponseTimeLimit = 2000;
	public static final int wrongRequestAnswerLimit = 20;

	public static final int maxIdStringSize = 65*SizeConstants.sizeOfChar;
	public static final int maxPasswordString = 37*SizeConstants.sizeOfChar;
	public static final int broadcastBufferSize = applicationId.length + SizeConstants.sizeOfByte + maxPasswordString;
	public static final int commandBufferSize = SizeConstants.sizeOfInt+257*SizeConstants.sizeOfChar;
	
	public static final int commandQueueSize = 10;
	
	public static final int publicKeySize = 32;	//size of the public key used in bytes

	/*COMANDOS*/
}
