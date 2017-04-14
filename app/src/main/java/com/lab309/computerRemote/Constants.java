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
	public static final byte identityRequest = 1;
	public static final byte connectionRequest = 2;
	public static final int requestResponseTimeLimit = 2000;
	public static final int wrongRequestAnswerLimit = 20;

	public static final int maxIdStringSize = 65*SizeConstants.sizeOfChar;
	public static final int maxPasswordString = 37*SizeConstants.sizeOfChar;
	public static final int broadcastBufferSize = applicationId.length + SizeConstants.sizeOfByte + maxPasswordString;
	public static final int commandBufferSize = SizeConstants.sizeOfInt+257*SizeConstants.sizeOfChar;

	/*COMANDOS*/

	/* Variavel: commandExecuteLine
	 * Campo de dados: | String s |
	 *
	 * Executa um processo com seus devidos argumentos de acordo com s. s eh uma string identica a uma string passada manualmente em um terminal
	 * A execucao de processos sem uma interface grafica que requeiram entrada e saida de usuario poderao bloquear indefinidamente
	 *
	 */
	public static final int commandExecuteLine = 1;

}
