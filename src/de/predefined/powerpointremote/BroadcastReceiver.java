package de.predefined.powerpointremote;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by Julius on 18.05.13.
 */
public class BroadcastReceiver extends Thread {
	/**
	 * Our port is 34012, this can later be changed in the Settings
	 */
	private final int PORT = 34012;
	private MainActivity current;

	public BroadcastReceiver(MainActivity c) {
		this.current = c;
	}

	/**
	 * This method starts a cycle, that is broken, when we receive a
	 * UDP-Broadcastmessage from a PowerPoint Remote server.
	 */
	public void run() {
		String msg = null;
		DatagramPacket packet = null;
		try {
			// Create a socket to listen on the port.
			DatagramSocket dsocket = new DatagramSocket(PORT);
			byte[] buffer = new byte[2048];

			// Create a packet to receive data into the buffer
			packet = new DatagramPacket(buffer, buffer.length);
			dsocket.setBroadcast(true);
			// Now loop forever, waiting to receive packets and printing them.
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
		current.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				current.onServerFound(result);
			}
		});

		return;

	}

}
