package de.predefined.powerpointremote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * The main activity that handles the users interactions with the ui.
 * @author Julius
 *
 */
public class MainActivity extends Activity implements View.OnClickListener {
	/** ImageView for the current slide */
	private ImageView mCurrentSlide;
	/** The server name. */
	private String mServerName = "";
	/** The host address of the server. */
	private String mHostAdress = "";
	/**
	 * The chronometer that displays the whole time since the presentation
	 * started .
	 */
	private Chronometer mWholePresChr;
	/**
	 * The chronometer that displays the time since the current slide was
	 * opened.
	 */
	private Chronometer mCurrSlideChr;
	/** The TextView to display notes. */
	private TextView mNotes;
	/** The main layout to add the OnSwipeTouchListener to it. */
	private LinearLayout mMainLayout;
	/** true if the presentation is running. */
	private boolean mIsPresentationRunning = false;
	/** The connection manager. */
	private ConnectionManager mConnection;
	/** The Menu/ Actionbar. */
	private Menu mMenu;

	/*
	 * The onCreate-method, something like a constructor in android Activities
	 */
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.activity_main);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// UI initialization
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
		// Start a new Broadcastreceiver Thread:
		BroadcastReceiver bcr = new BroadcastReceiver(this);
		bcr.start();

	}

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

	@Override
	public void onClick(View view) {
		if (view.equals(mCurrentSlide)) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.msg_current_slide_image);
			ImageView alertView = new ImageView(this);
			alertView.setImageBitmap(((BitmapDrawable) mCurrentSlide
					.getDrawable()).getBitmap());
			alert.setView(alertView);
			alert.show();
		}

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
			alert.setTitle(R.string.msg_conncetion_request);
			alert.setMessage(String.format(
					getString(R.string.label_control_question),
					this.mServerName));
		} else {
			alert.setTitle(R.string.msg_wrong_pairing_code);
			alert.setMessage(R.string.label_connect_retry);
		}
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(R.string.label_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mConnection != null)
							mConnection.disconnect();
						mConnection = new ConnectionManager(
								MainActivity.this.mHostAdress, 34012, input
										.getText().toString(),
								MainActivity.this);
						mConnection.start();
					}
				});

		alert.setNegativeButton(R.string.label_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	/**
	 * Changes the current slide image.
	 * 
	 * @param b
	 *            The new Image.
	 */
	public void onImageChanged(Slide newSlide) {
		Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
		Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);	
		fadeOut.setDuration(500);
		fadeIn.setDuration(500);
		mCurrentSlide.startAnimation(fadeOut);
		mCurrentSlide.setImageBitmap(newSlide.getCurrentView());		
		mCurrentSlide.setVisibility(View.VISIBLE);
		mCurrentSlide.startAnimation(fadeIn);
		
	}

	/**
	 * The method is called if we receive new notes from the server, i.e. a new
	 * Slide.
	 * 
	 * @param notes
	 *            Slide notes received from the server.
	 */
	public void onNewSlideReceived(Slide newSlide) {
		if (!mIsPresentationRunning) {
			mWholePresChr.setBase(SystemClock.elapsedRealtime());
			mWholePresChr.start();
			mIsPresentationRunning = true;
		}
		mCurrSlideChr.setBase(SystemClock.elapsedRealtime());
		mCurrSlideChr.start();
		this.mNotes.setText(getString(R.string.label_notes) + "\n"
				+ newSlide.getNotes());
	}

	/**
	 * When the presentation ends we have to reset some values.
	 */
	public void onPresentationEnded() {
		mWholePresChr.stop();
		mCurrSlideChr.stop();
		this.mNotes.setText(R.string.label_presentation_ended);
		this.mCurrentSlide.setImageBitmap(null);
		mIsPresentationRunning = false;
	}

	/**
	 * Called, when the Connection was successful.
	 */
	public void onConnectSuccess() {
		Toast.makeText(this, getString(R.string.msg_connection_successful),
				Toast.LENGTH_LONG).show();
		mMenu.findItem(R.id.menu_start).setEnabled(true);
		mMenu.findItem(R.id.menu_next).setEnabled(true);
		mMenu.findItem(R.id.menu_previous).setEnabled(true);
		mMenu.findItem(R.id.menu_stop).setEnabled(true);
	}

	/**
	 * Called, when the connection is lost.
	 */
	public void onConnectionLost() {
		Toast.makeText(this, getString(R.string.msg_connection_lost),
				Toast.LENGTH_LONG).show();
		mMenu.findItem(R.id.menu_start).setEnabled(false);
		mMenu.findItem(R.id.menu_next).setEnabled(false);
		mMenu.findItem(R.id.menu_previous).setEnabled(false);
		mMenu.findItem(R.id.menu_stop).setEnabled(false);
		this.mNotes.setText(R.string.label_notes);
		this.mCurrentSlide.setVisibility(View.INVISIBLE);
		BroadcastReceiver bcr = new BroadcastReceiver(this);
		bcr.start();
	}
}
