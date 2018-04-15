package aos.project.mserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MserverHeartBeatListener implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ServerSocket mServerListener_heart = null;
	private MserverHeartBeatListenerThread mHLT = null;
	private Thread t = null;
	@Override
	public void run() {
		try {
			mServerListener_heart = new ServerSocket(5050);
			System.out.println("listening for heartbeat ");
			//mHConnection = mServerListener_heart.accept();
			//isM_ob = new ObjectInputStream(mHConnection.getInputStream());
			// osM = new PrintWriter(mHConnection.getOutputStream(),true);
			while (true) {
				mHLT = new MserverHeartBeatListenerThread(mServerListener_heart.accept());
				t = new Thread(mHLT);
				t.start();
		
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

}
