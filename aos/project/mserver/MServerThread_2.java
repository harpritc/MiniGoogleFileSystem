package aos.project.mserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketException;

public class MServerThread_2 implements Runnable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ObjectInputStream isM_ob = null;
	private ObjectOutputStream osM_ob = null;
	public MServerThread_2(ObjectInputStream isM_ob,ObjectOutputStream osM_ob){
		this.isM_ob = isM_ob;
		this.osM_ob = osM_ob;
	}
	@Override
	public void run() {
		try {

			while (true) {
				System.out.println("Connection Accepted ");
				Object inputObj = null;
				//synchronized (MServerThread_2.class) {
					
					try {
						inputObj = isM_ob.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if (inputObj == null) {
						break;
					}
					
				//}
					// string
					if (inputObj instanceof String) {
						String input = (String) inputObj;
	
						String[] parts = input.split(":");
	
						switch (parts[1]) {
						// message from client -- > sending to serever to create
						// file
						// clientId + ":" + command + ":" + fileName + ":" + chId
						case ("CREATE"): {
							System.out.println("input from client " + input
									+ " flag " + Mserver.flagMap);
							
							Mserver.createFile(parts[2],
									Integer.parseInt(parts[3]),osM_ob);
							
							 
							break;
						}
						// Heartbeat message
						// timeUpdate:SID
						// append cid:command:filename: bytecount
						case ("APPEND"): {
							System.out.println("input from client " + input
									+ " flag " + Mserver.flagMap);
							
							Mserver.appendFile(parts[2],
									Integer.parseInt(parts[3]), osM_ob);
							
							
							break;
						}
	
						// read
						// read cid:command:filename:startByte: bytecount
						case ("READ"): {
							System.out.println("input from client " + input
									+ " flag " + Mserver.flagMap);
							
							Mserver.readFile(parts[2],
									Integer.parseInt(parts[3]),
									Integer.parseInt(parts[4]), osM_ob);
							
							break;
						}
						
						}
					}

			}
		} catch (SocketException e) {
			e.printStackTrace();
			
			try {
				osM_ob.close();
				isM_ob.close();
				//MServerThread.mConnection.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				//MServerThread.mConnection.close();
				isM_ob.close();
				osM_ob.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
