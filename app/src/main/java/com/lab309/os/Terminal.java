package com.lab309.os;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteArrayConverter;

import java.io.OutputStream;

import java.io.IOException;

public class Terminal {

	/*STATIC ATTRIBUTES*/
	private static final String runMsg = " executed: ";
	private static final byte[] runMessage = ByteArrayConverter.stringToArray(Terminal.runMsg, new byte[SizeConstants.sizeOfString(Terminal.runMsg)], 0);

	/*ATTRIBUTES*/
	private Runtime runtime;
	private OutputStream log;

	/*CONSTRUCTORS*/
	public Terminal (OutputStream log) {
		this.runtime = Runtime.getRuntime();
		this.log = log;
	}

	/*METHODS*/
	public void execute (String origin, String command) throws IOException {

		int sizeOfOrigin = SizeConstants.sizeOfString(origin) - SizeConstants.sizeOfChar;
		int sizeOfCommand = SizeConstants.sizeOfString(command) - SizeConstants.sizeOfChar;
		byte[] line = new byte[sizeOfOrigin + Terminal.runMessage.length + sizeOfCommand];
		int offset;

		//gera linha a ser escrita no log
		ByteArrayConverter.stringToArray(origin, line, 0);
		offset = sizeOfOrigin;

		ByteArrayConverter.copyArrayTo(Terminal.runMessage, 0, Terminal.runMessage.length-SizeConstants.sizeOfChar, line, offset);
		offset += Terminal.runMessage.length - SizeConstants.sizeOfChar;

		ByteArrayConverter.stringToArray(command, line, offset);
		offset += sizeOfCommand;

		ByteArrayConverter.charToArray('\n', line, offset);

		//escreve linha no log
		log.write(line);

		//executa comando
		this.runtime.exec(command);

	}

}
