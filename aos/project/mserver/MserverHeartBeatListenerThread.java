package aos.project.mserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MserverHeartBeatListenerThread implements Runnable{
	Socket mHlConnection = null;
	private static ObjectInputStream isM_ob = null;
	
	static volatile boolean wait = false;
	
	public MserverHeartBeatListenerThread (Socket mHlConnection){
		this.mHlConnection = mHlConnection;
	}
	@Override
	public void run() {
		try {
			
			isM_ob = new ObjectInputStream(mHlConnection.getInputStream());
			new Thread(new MserverHeartBeatListenerThread_2(isM_ob)).start();
			// osM = new PrintWriter(mHConnection.getOutputStream(),true);
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
