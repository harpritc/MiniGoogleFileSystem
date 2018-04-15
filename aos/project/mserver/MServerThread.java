package aos.project.mserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MServerThread implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Socket mConnection = null;
	private static ObjectInputStream isM_ob = null;
	public static ObjectOutputStream osM_ob = null;

	public MServerThread(Socket mConnection) {
		MServerThread.mConnection = mConnection;
	}

	@Override
	public void run() {
		try {
			System.out.println("Mserver Thread ....");
			isM_ob = new ObjectInputStream(mConnection.getInputStream());
			osM_ob = new ObjectOutputStream(mConnection.getOutputStream());
			osM_ob.flush();
			
			new Thread(new MServerThread_2(isM_ob,osM_ob)).start();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

}
