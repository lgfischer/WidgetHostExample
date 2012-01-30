package com.lgfischer.widgethost;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * This activity serves as an example of how to search, add and remove widgets
 * from a window.
 * 
 * @author Leonardo Garcia Fischer (http://coderender.blogspot.com/)
 * 
 */
public class WidgetHostExampleActivity extends Activity {

	static final String TAG = "WidgetHostExampleActivity";

	AppWidgetManager mAppWidgetManager;
	AppWidgetHost mAppWidgetHost;

	ViewGroup mainlayout;

	/**
	 * Called on the creation of the activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mainlayout = (ViewGroup) findViewById(R.id.main_layout);

		mAppWidgetManager = AppWidgetManager.getInstance(this);
		mAppWidgetHost = new AppWidgetHost(this, R.id.APPWIDGET_HOST_ID);
	}

	/**
	 * Launches the menu to select the widget. The selected widget will be on
	 * the result of the activity.
	 */
	void selectWidget() {
		int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		addEmptyData(pickIntent);
		startActivityForResult(pickIntent, R.id.REQUEST_PICK_APPWIDGET);
	}

	/**
	 * This avoids a bug in the com.android.settings.AppWidgetPickActivity,
	 * which is used to select widgets. This just adds empty extras to the
	 * intent, avoiding the bug.
	 * 
	 * See more: http://code.google.com/p/android/issues/detail?id=4272
	 */
	void addEmptyData(Intent pickIntent) {
		ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
		pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
		ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
		pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
	}

	/**
	 * If the user has selected an widget, the result will be in the 'data' when
	 * this function is called.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == R.id.REQUEST_PICK_APPWIDGET) {
				configureWidget(data);
			} else if (requestCode == R.id.REQUEST_CREATE_APPWIDGET) {
				createWidget(data);
			}
		} else if (resultCode == RESULT_CANCELED && data != null) {
			int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) {
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			}
		}
	}

	/**
	 * Checks if the widget needs any configuration. If it needs, launches the
	 * configuration activity.
	 */
	private void configureWidget(Intent data) {
		Bundle extras = data.getExtras();
		int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		if (appWidgetInfo.configure != null) {
			Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidgetInfo.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			startActivityForResult(intent, R.id.REQUEST_CREATE_APPWIDGET);
		} else {
			createWidget(data);
		}
	}

	/**
	 * Creates the widget and adds to our view layout.
	 */
	public void createWidget(Intent data) {
		Bundle extras = data.getExtras();
		int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

		AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
		hostView.setAppWidget(appWidgetId, appWidgetInfo);
		mainlayout.addView(hostView);

		Log.i(TAG, "The widget size is: " + appWidgetInfo.minWidth + "*" + appWidgetInfo.minHeight);
	}

	/**
	 * Registers the AppWidgetHost to listen for updates to any widgets this app
	 * has.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mAppWidgetHost.startListening();
	}

	/**
	 * Stop listen for updates for our widgets (saving battery).
	 */
	@Override
	protected void onStop() {
		super.onStop();
		mAppWidgetHost.stopListening();
	}

	/**
	 * Removes the widget displayed by this AppWidgetHostView.
	 */
	public void removeWidget(AppWidgetHostView hostView) {
		mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
		mainlayout.removeView(hostView);
	}

	/**
	 * Handles the menu.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "Menu selected: " + item.getTitle() + " / " + item.getItemId() + " / " + R.id.addWidget);
		switch (item.getItemId()) {
		case R.id.addWidget:
			selectWidget();
			return true;
		case R.id.removeWidget:
			removeWidgetMenuSelected();
			return false;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Handle the 'Remove Widget' menu.
	 */
	public void removeWidgetMenuSelected() {
		int childCount = mainlayout.getChildCount();
		if (childCount > 1) {
			View view = mainlayout.getChildAt(childCount - 1);
			if (view instanceof AppWidgetHostView) {
				removeWidget((AppWidgetHostView) view);
				Toast.makeText(this, R.string.widget_removed_popup, Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Toast.makeText(this, R.string.no_widgets_popup, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Creates the menu with options to add and remove widgets.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.widget_menu, menu);
		return true;
	}
}