 The visibility protocol goes as follows:
 	1-Client broadcasts a packet through the broadcast port, having: (Constants.helloMessage)
 
 	2-Server sends a packet to the client's IP through the broadcast port, having:
 		(String serverName, int connectionPort)
 		validationByte will be 0 in case the server does not use encryption
 
 	The visibility protocol is the only one that sends unencrypted messages regardless of the encrypted variable. All other communications with the server are encrypted.
 
 The connection protocol goes as follows:
 	1-Client sends a packet to the server's IP through the connection port, having: (Constants.connectMessage, int answerPort)
 
 	2-Server sends a packet to the client's IP through the answer port, having: (Constants.connectMessage, int commandsPort, byte[] validationBytes)
 		If the password for the encryption was wrong the server will not answer
 
 	3-Client sends a packet to the server's IP through the connection port, having: (Constants.finishConnectMessage, int feedbackPort, String clientName), after which the connection is fully established
 	
 
 
 The transmission of commands follows the protocol:
 	3-Client sends packet throught the command port to the server's IP, having: (byte validationByte, byte commandId, ... args)
 		For the arguments of each type of command see class Steward
