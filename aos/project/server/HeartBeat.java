package aos.project.server;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

public class HeartBeat implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long pastTime = System.currentTimeMillis() / 1000L;
	public static volatile boolean flag = true;

	@Override
	public void run() {
		try {
			Thread.sleep(18000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("creating connection for heart Beat");
		try {
			Server.serverToMSoc = new Socket(Server.mServerAddr, 5050);
			Server.os_SM_ob = new ObjectOutputStream(
					Server.serverToMSoc.getOutputStream());
			Server.os_SM_ob.flush();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Server.is_SM_ob.close();
				Server.os_SM_ob.close();
				Server.serverToMSoc.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		System.out.println(" beginning to send heart beat, flag " + flag);
		// while(flag && System.currentTimeMillis()/1000L - initTime < 60){
		try {
			while (true) {
				if (flag) {
					Server.os_SM_ob
							.writeObject("SERVER_UP:SERVER_UP:SERVER_UP");
					Server.os_SM_ob.flush();
					if ((System.currentTimeMillis() / 1000L - pastTime) >= 5) {
						System.out
								.println("time update "
										+ (System.currentTimeMillis() / 1000L - pastTime));
						// System.out.println("Sending heat beat message");
						Server.os_SM_ob.writeObject(System.currentTimeMillis()
								/ 1000L + ":HEARTBEAT" + ":" + Server.serverId);
						Server.os_SM_ob.flush();

						try {
							System.out.println("server hashmap "
									+ Server.serverMeta);
							if (!Server.serverMeta.isEmpty()) {
								Server.os_SM_ob.reset();
								Server.os_SM_ob.writeObject(Server.serverMeta);
								Server.os_SM_ob.flush();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						pastTime = System.currentTimeMillis() / 1000L;
					}
				} else {
					Server.os_SM_ob
							.writeObject("SERVER_DOWN:SERVER_DOWN:SERVER_DOWN");
					Server.os_SM_ob.flush();
				}

			}
		} catch (IOException c) {
			c.printStackTrace();
		}
		/*
		 * try { Server.is_SM.close(); Server.os_SM.close();
		 * Server.serverToMSoc.close(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

	}

}
