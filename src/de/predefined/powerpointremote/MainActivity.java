package de.predefined.powerpointremote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {
	private ImageView mCurrentSlide;
	private String mServerName = "";
	private String mHostAdress = "";
	private Chronometer mWholePresChr;
	private Chronometer mCurrSlideChr;
	private TextView mNotes;
	private LinearLayout mMainLayout;
	private boolean mIsPresentationRunning = false;
	private ConnectionManager mConnection;
	private Menu mMenu;

	/**
	 * The onCreate-method, something like a constructor in android Activities
	 * 
	 * @param savedInstanceState
	 */
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.activity_main);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// UI initialisation
		mMainLayout = (LinearLayout) findViewById(R.id.mainLayout);
		mCurrentSlide = (ImageView) findViewById(R.id.imageView1);
		mCurrentSlide.setOnClickListener(this);
		mWholePresChr = (Chronometer) findViewById(R.id.chronometer1);
		mCurrSlideChr = (Chronometer) findViewById(R.id.chronometer2);
		mNotes = (TextView) findViewById(R.id.textView3);
		mCurrentSlide.setVisibility(View.INVISIBLE);
		// setting the Swipe Listener to our mainLayout
		mMainLayout.setOnTouchListener(new OnSwipeTouchListener() {

			public void onSwipeRight() {
				MainActivity.this.mConnection.nextSlide();
			}

			public void onSwipeLeft() {
				MainActivity.this.mConnection.previousSlide();
			}
		});
		// start a new Broadcastreceiver Thread
		BroadcastReceiver bcr = new BroadcastReceiver(this);
		bcr.start();

	}

	/**
	 * "Constructor" of the options menue
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		this.mMenu = menu;
		mMenu.findItem(R.id.menu_start).setEnabled(false);
		mMenu.findItem(R.id.menu_next).setEnabled(false);
		mMenu.findItem(R.id.menu_previous).setEnabled(false);
		mMenu.findItem(R.id.menu_stop).setEnabled(false);
		return true;
	}

	/**
	 * This method is called, when an Options Menu Item is selected
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_start:
			mConnection.startPresentation();
			return true;
		case R.id.menu_next:
			mConnection.nextSlide();
			return true;
		case R.id.menu_previous:
			mConnection.previousSlide();
			return true;
		case R.id.menu_stop:
			mConnection.stopPresentation();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Debug things
	 * 
	 * @param view
	 */
	@Override
	public void onClick(View view) {
		if (view.equals(mCurrentSlide))
			Toast.makeText(this.getApplicationContext(), "Touched ImageView1",
					Toast.LENGTH_LONG).show();
	}

	/**
	 * When the connectionManager finds a server, this method is called
	 * 
	 * @param host
	 *            A String Array that contains {hostAddress, serverName}
	 */
	public void onServerFound(String[] host) {
		mHostAdress = host[1];
		String[] temp = host[0].split("-");
		for (int i = 1; i < temp.length; i++) {
			mServerName += temp[i];
		}
		authenticateProcedure(false);
	}

	/**
	 * The process to connect to a Server
	 * 
	 * @param retry
	 *            Is this the first call of the method (false) or did the user
	 *            enter a wrong pairing code (true)?
	 */
	public void authenticateProcedure(boolean retry) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		if (!retry) {
			alert.setTitle("Connection request");
			alert.setMessage("If you want to remote control "
					+ this.mServerName + ", please enter the pairing code.");
		} else {
			alert.setTitle("Wrong Pairing Code.");
			alert.setMessage("If you want to remote control "
					+ this.mServerName + ", please try again:");
		}
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mConnection != null)
					mConnection.disconnect();
				mConnection = new ConnectionManager(
						MainActivity.this.mHostAdress, 34012, input.getText()
								.toString(), MainActivity.this);
				mConnection.start();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	/**
	 * Changes the current Slide Image
	 * 
	 * @param b
	 *            the new Image
	 */
	public void onImageChanged(Slide newSlide) {
		mCurrentSlide.setVisibility(View.VISIBLE);
		mCurrentSlide.setImageBitmap(newSlide.getCurrentView());
	}

	/**
	 * The method is called if we receive new notes from the server, i.e. a new
	 * Slide
	 * 
	 * @param notes
	 *            Slide notes received from the server
	 */
	public void onNewSlideReceived(Slide newSlide) {
		if (!mIsPresentationRunning) {
			mWholePresChr.setBase(SystemClock.elapsedRealtime());
			mWholePresChr.start();
			mIsPresentationRunning = true;
		}
		mCurrSlideChr.setBase(SystemClock.elapsedRealtime());
		mCurrSlideChr.start();
		this.mNotes.setText(newSlide.getNotes());
	}

	/**
	 * When the presentation ends we have to reset some values
	 */
	public void onPresentationEnded() {
		mWholePresChr.stop();
		mCurrSlideChr.stop();
		this.mNotes.setText("Presentation ended.");
		this.mCurrentSlide.setImageBitmap(null);
		mIsPresentationRunning = false;
	}

	/**
	 * Called, when the Connection was successful.
	 */
	public void onConnectSuccess() {
		Toast.makeText(this, "Connection was successful.", Toast.LENGTH_LONG).show();
		mMenu.findItem(R.id.menu_start).setEnabled(true);
		mMenu.findItem(R.id.menu_next).setEnabled(true);
		mMenu.findItem(R.id.menu_previous).setEnabled(true);
		mMenu.findItem(R.id.menu_stop).setEnabled(true);
	}

	/**
	 * Called, when the connection is lost.
	 */
	public void onConnectionLost() {
		Toast.makeText(this, "Connection Lost.", Toast.LENGTH_LONG).show();
		mMenu.findItem(R.id.menu_start).setEnabled(false);
		mMenu.findItem(R.id.menu_next).setEnabled(false);
		mMenu.findItem(R.id.menu_previous).setEnabled(false);
		mMenu.findItem(R.id.menu_stop).setEnabled(false);
	}
}
