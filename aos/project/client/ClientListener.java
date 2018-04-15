package aos.project.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ClientListener implements Runnable, Serializable {
	public static ObjectInputStream isM_ob = null; // input stream Mserver
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void run() {
		System.out.println("client listening.. ");
		try {
			
			isM_ob = new ObjectInputStream(Client.clientSocketM.getInputStream());
			new Thread(new ClientListener_2(isM_ob)).start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
