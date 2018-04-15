package aos.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class ServerListener implements Runnable {

	private Thread t;
	private static volatile boolean flag = true;
	private ServerThread sT = null;

	@Override
	public void run() {
		// Server.sendHeartBeat();

		try {
			System.out.println("Server listening port " + Server.sPort);
			Server.sListener = new ServerSocket(Server.sPort);
			System.out.println(flag);
			while (true) {
				System.out.println("waiting...");
				sT = new ServerThread(Server.sListener.accept());
				sT.startRunning();
				t = new Thread(sT);
				t.setPriority(8);
				t.start();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
