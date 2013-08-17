package de.predefined.powerpointremote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;
import java.net.Socket;

/**
 * This class manages the connection to the server.
 * @author Julius
 */
public class ConnectionManager extends Thread {
	/** The socket. */
	private Socket connection;
	/** The data output stream.. */
	private DataOutputStream out;
	/** The data input stream. */
	private DataInputStream in;
	/** The host name. */
	private String hostName;
	/** The pairing code */
	private String key;
	/** The port of the server. */
	private int port;
	/**
	 * Reference to MainActivity to avoid unnecessary Listener interfaces (for
	 * this small project).
	 */
	private MainActivity runningOn;
	/** The current slide object. */
	private Slide mSlide;

	/**
	 * Create a new ConnectionManager.
	 * 
	 * @param host
	 *            The servers hostname.
	 * @param port
	 *            The servers port.
	 * @param key
	 *            The pairing key, entered by the user.
	 * @param runningOn
	 *            the instance of our MainActivity that created this Thread.
	 */
	public ConnectionManager(String host, int port, String key,
			MainActivity runningOn) {
		this.key = key;
		this.hostName = host;
		this.port = port;
		this.runningOn = runningOn;
		this.mSlide = new Slide();
	}

	/**
	 * Tries to open a connection for our PPTREMOTE-Protocol.
	 * 
	 * @param hostName
	 *            The servers hostname
	 * @param key
	 *            The pairing key, entered by the user
	 * @throws WrongPairingCodeException
	 *             Is thrown, when the pairing code is wrong
	 */
	private void connectTo(String hostName, String key)
			throws WrongPairingCodeException {
		try {
			connection = new Socket(hostName, port);
			out = new DataOutputStream(connection.getOutputStream());
			in = new DataInputStream(connection.getInputStream());
			runningOn.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					runningOn.onConnectSuccess();
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		// main loop
		authenticate(key);
		processRequests();

	}

	/**
	 * closes the connection
	 */
	public void disconnect() {
		try {
			this.connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The authentication request
	 * 
	 * @param key
	 *            pairing code, entered by the user
	 */
	private void authenticate(String key) {
		try {
			out.writeByte(0);
			byte[] arbytes = key.getBytes("UTF-8");
			out.write(this.encodeInt(arbytes.length));
			out.write(arbytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a request to display the next slide
	 */
	public void nextSlide() {
		try {
			out.writeByte(MessageID.NEXT_SLIDE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a request to display the previous slide
	 */
	public void previousSlide() {
		try {
			out.writeByte(MessageID.PREVIOUS_SLIDE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a request to start the presentation
	 */
	public void startPresentation() {
		try {
			out.writeByte(MessageID.START_PRESENTATION);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a request to stop the presentation.
	 */
	public void stopPresentation() {
		try {
			out.writeByte(MessageID.STOP_PRESENTATION);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * starts the whole connection-process
	 */
	@Override
	public void run() {
		try {
			connectTo(hostName, key);
		} catch (WrongPairingCodeException e) {
			runningOn.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ConnectionManager.this.runningOn
							.authenticateProcedure(true);
				}
			});

		}
	}

	/**
	 * Gets an Integer from a byte array.
	 * 
	 * @param arr
	 *            The byte Array.
	 * @return The integer.
	 */
	private int getIntFromBytes(byte[] arr) {
		int result = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == 1)
				result += Math.pow(2, i);
		}
		Log.i("PPTREMOTE", "Received int: " + result);
		return result;
	}

	/**
	 * Decodes an integer value to a byte array.
	 * 
	 * @param value
	 *            The integer value.
	 * @return The byte array.
	 */
	private byte[] encodeInt(int value) {
		byte[] intBuffer = new byte[32];

		for (int i = 0; i < intBuffer.length; i++) {
			int FLAG = (1 << i);
			boolean isSet = (value & FLAG) == FLAG;
			intBuffer[i] = (byte) (isSet ? 1 : 0);
		}

		return intBuffer;
	}

	/**
	 * Receives an integer from the server.
	 * 
	 * @return the integer received from the server.
	 * @throws IOException
	 *             from DataInputStram.readbyte()
	 */
	private int receiveInt() throws IOException {
		byte[] intBuffer = new byte[32];

		for (int i = 0; i < intBuffer.length; i++) {
			intBuffer[i] = this.in.readByte();
		}

		int i = this.getIntFromBytes(intBuffer);
		System.out.println("INT: " + i + "Byte array:" + intBuffer.toString());

		return i;
	}

	/**
	 * Main communication with the server.
	 * 
	 * @throws WrongPairingCodeException
	 *             is thrown if the user entered a wrong pairing code.
	 */
	private void processRequests() throws WrongPairingCodeException {
		while (!connection.isClosed()) {
			try {
				if (in.available() > 0) {
					byte read = in.readByte();
					switch (read) {
					case MessageID.PING:
						System.out.print("PING received. ");
						break;
					case MessageID.AUTHENTICATE:
						boolean answer = in.readBoolean();
						Log.i("PPTREMOTE", "Received auth answer.");
						if (!answer) {
							throw new WrongPairingCodeException();
						}
						break;
					case MessageID.NOTES_DATA:
						int notesLength = this.receiveInt();
						byte[] notesArr = new byte[notesLength];
						in.readFully(notesArr, 0, notesLength);
						final String notes = new String(notesArr, "UTF-8");
						Log.i("PPTREMOTE", "Received notes data.");
						this.mSlide.setNotes(notes);
						runningOn.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								runningOn.onNewSlideReceived(mSlide);
							}
						});
						break;
					case MessageID.IMAGE_DATA:
						// new image
						int imageLength = this.receiveInt();
						byte[] temp = new byte[imageLength];
						in.readFully(temp, 0, imageLength);
						ByteArrayInputStream imageStream = new ByteArrayInputStream(
								temp);
						Bitmap image = BitmapFactory.decodeStream(imageStream);
						Log.i("PPTREMOTE", "Received image.");
						this.mSlide.setCurrentView(image);
						runningOn.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								runningOn.onImageChanged(mSlide);
							}
						});
						break;
					case MessageID.STOP_PRESENTATION:
						Log.i("PPTREMOTE", "The presentation ended.");
						runningOn.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								runningOn.onPresentationEnded();
							}
						});
						break;
					}
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						Log.e("PPTREMOTE", e.getMessage());
					}
				}
			} catch (IOException e) {
				Log.e("PPTREMOTE", e.getMessage());
			}
		}

		runningOn.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				runningOn.onConnectionLost();
			}

		});
		this.disconnect();

	}

}
