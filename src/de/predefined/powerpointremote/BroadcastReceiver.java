package de.predefined.powerpointremote;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This BroadcastRecevier receives UDP-Broadcastmessages from the server.
 * @author Julius
 */
public class BroadcastReceiver extends Thread {
	/** Our port is 34012, this can later be changed in the Settings */
	private final int PORT = 34012;
	/**
	 * Reference to MainActivity to avoid unnecessary Listener interfaces (for
	 * this small project).
	 */
	private MainActivity runningOn;
	
	/**
	 * The Constructor
	 * @param c
	 * 		Reference to the MainActivity.
	 */
	public BroadcastReceiver(MainActivity c) {
		this.runningOn = c;
	}

	/**
	 * This method starts a cycle, that is broken, when we receive a
	 * UDP-Broadcastmessage from a PowerPoint Remote server.
	 */
	public void run() {
		String msg = null;
		DatagramPacket packet = null;
		try {
			
			DatagramSocket dsocket = new DatagramSocket(PORT);
			byte[] buffer = new byte[2048];
			
			packet = new DatagramPacket(buffer, buffer.length);
			dsocket.setBroadcast(true);			
			while (true) {
				Log.i("BroadcastReceiver", "Starting BroadcastReceiving.");
				dsocket.receive(packet);
				Log.i("BroadcastReceiver", "Ended receving packets");
				msg = new String(buffer, 0, packet.getLength());
				System.out.println(packet.getAddress().getHostName() + ": "
						+ msg);
				System.out.println("Broadcast received from:"
						+ packet.getAddress().getHostName() + ": " + msg);
				if (msg.contains("PowerPoint Remote")) {
					break;
				}
				packet.setLength(buffer.length);
			}
		} catch (IOException e) {
			Log.e("PPTREMOTE", e.getMessage() + "\n" + e.getStackTrace() + "\n"
					+ e.getCause());
		}
		final String[] result = { msg, packet.getAddress().getHostName() };
		runningOn.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				runningOn.onServerFound(result);
			}
		});

		return;

	}

}
