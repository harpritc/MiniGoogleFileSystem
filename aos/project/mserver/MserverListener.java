package aos.project.mserver;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;

public class MserverListener implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void run() {
		System.out.println("Mserver listening from server.. ");

		String input = "";
		try {
			try {
				while (true) {

					input = (String) Mserver.m_isS_ob.readObject();
					if (input == null) {
						break;
					}
					System.out.println("input: " + input);
					if (input.trim().contains("File name already exist")) {
						System.out.println("sending to client");
						MServerThread.osM_ob
								.writeObject("File already exist, Create file with different name");
						MServerThread.osM_ob.flush();
					}
					
					if (input.trim().contains("Created file in Server Id")) {
						System.out.println("sending to client created");
						MServerThread.osM_ob
								.writeObject(input);
						MServerThread.osM_ob.flush();
					}


				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} catch (SocketException e) {
			e.printStackTrace();
			try {
				Mserver.m_isS_ob.close();
				Mserver.m_osS_ob.close();
				Mserver.mSocketS.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

}
