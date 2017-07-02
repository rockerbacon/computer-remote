package com.lab309.os;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteArrayConverter;

import java.io.OutputStream;

import java.io.IOException;

public class Terminal {

	/*ATTRIBUTES*/
	private Runtime runtime;

	/*CONSTRUCTORS*/
	public Terminal () {
		this.runtime = Runtime.getRuntime();
	}

	/*METHODS*/
	public void execute (String command) throws IOException {
		//executa comando
		this.runtime.exec(command);
	}

}
