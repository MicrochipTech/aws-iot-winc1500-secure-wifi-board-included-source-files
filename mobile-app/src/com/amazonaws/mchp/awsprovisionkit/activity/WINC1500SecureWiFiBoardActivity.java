package com.amazonaws.mchp.awsprovisionkit.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;


import com.amazonaws.mchp.awsprovisionkit.R;
import com.amazonaws.mchp.awsprovisionkit.model.AwsRouter;
import com.amazonaws.mchp.awsprovisionkit.model.infoContainer;
import com.amazonaws.mchp.awsprovisionkit.model.itemInfo;
import com.amazonaws.mchp.awsprovisionkit.service.AwsService;
import com.amazonaws.mchp.awsprovisionkit.task.json.AwsJsonMsg;
import com.amazonaws.mchp.awsprovisionkit.task.json.AwsShadowJsonMsg;
import com.amazonaws.mchp.awsprovisionkit.task.ui.VerticalSwipeRefreshLayout;
import com.amazonaws.mchp.awsprovisionkit.utils.AppHelper;
import com.amazonaws.mchp.awsprovisionkit.utils.ServiceConstant;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.amazonaws.mchp.awsprovisionkit.activity.WINC1500SecureWiFiBoardActivity.updateType.UNKNOWN;

@SuppressLint("HandlerLeak")
public class WINC1500SecureWiFiBoardActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, Spinner.OnItemSelectedListener {

	static final String LOG_TAG = WINC1500SecureWiFiBoardActivity.class.getCanonicalName();

	/**
	 * cuase time is tight ,  hard code for demo .
	 */


	private static final String CMD_CTRL_LED1 = "LED_1";

	//user define
	private static final int CMD_GET_DEV_STATUS =0x0;
    private static final int CMD_UPDATE_UI =0x1;
	private static final int PULL_TO_REFRESH = 0x2;
	private static final int REFRESH_ICON_DISMISS = 0x3;
	private static final int CMD_CTRL_COLOR = 0x4;
	private static final int CMD_CTRL_DEV =0x5;



	private VerticalSwipeRefreshLayout mSwipeLayout;
	private RadioButton rbButton1, rbButton2, rbButton3;
	private NavigationView nDrawer;
	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar toolbar;
	private	Spinner led_spinner;
	private TextView tvThingName;
	private TextView tvTemperatureVal;
	private TextView tvHumidityVal;

	private CognitoUser user;
	private String username;

	private String thing_name, deviceName, macAddr;

	private boolean requestGetDevState = false;

	awsProvisionKitReceiver receiver;

	enum updateType {
		UNKNOWN, LED, BUTTON, TEMP, HUM, PRESSURE, MAC
	}


	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case CMD_CTRL_DEV:
					//Toast.makeText(wifiSensorBoardControlActivity.this, "ctrl "+msg.obj.toString() + " " + msg.arg1, Toast.LENGTH_SHORT).show();

					break;

				case CMD_GET_DEV_STATUS:

					break;

				case PULL_TO_REFRESH:
				case REFRESH_ICON_DISMISS:
					mSwipeLayout.setRefreshing(false);

					break;

				case CMD_CTRL_COLOR:

					break;
                case CMD_UPDATE_UI:
                    break;
			}
		}
		;
	};




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_winc1500_secure_wifi_board);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		username = AppHelper.getCurrUser();
		user = AppHelper.getPool().getUser(username);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
		thing_name = extras.getString(ServiceConstant.ThingName);
		deviceName = extras.getString(ServiceConstant.DevName);
		macAddr = extras.getString(ServiceConstant.DevMacAddr);

		Log.d(LOG_TAG, "onCreate mac="+macAddr);
		initDevice();
		initView();

		mSwipeLayout.post(new Runnable() {
			@Override
			public void run() {
				mSwipeLayout.setRefreshing(true);
			}
		});

		receiver = new awsProvisionKitReceiver();
		handler.sendEmptyMessage(CMD_GET_DEV_STATUS);
	}

	private void initDevice() {
		Intent intent = getIntent();

	}

	// Perform the action for the selected navigation item
	private void performAction(MenuItem item) {
		// Close the navigation drawer
		Log.d(LOG_TAG, "Debug 7777777 item="+item.getItemId());
		mDrawer.closeDrawers();

		// Find which item was selected
		switch(item.getItemId()) {
			case R.id.nav_user_sign_out:
				// Start sign-out
				user.signOut();

				Intent intent = new Intent();
				if(username == null)
					username = "";
				intent.putExtra("name",username);
				setResult(RESULT_OK, intent);

				//unsubscribe the channel
				Intent subscribe_intent = new Intent(WINC1500SecureWiFiBoardActivity.this, AwsService.class);
				subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
				subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowDelta);
				startService(subscribe_intent);

				subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
				subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowUpdate);
				startService(subscribe_intent);


				finish();

				Intent activity_intent = new Intent(WINC1500SecureWiFiBoardActivity.this, MainActivity.class);
				startActivity(activity_intent);

				break;
			case R.id.nav_about:
				// For the inquisitive
				//Intent aboutAppActivity = new Intent(this, AboutApp.class);
				//startActivity(aboutAppActivity);
				break;

		}
	}

	// Private methods
	// Handle when the a navigation item is selected
	private void setNavDrawer() {
		nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem item) {
				performAction(item);
				return true;
			}
		});
	}

	private void initView() {

		tvThingName = (TextView) findViewById(R.id.mac_addr);
		tvThingName.setText(macAddr);

		tvTemperatureVal = (TextView) findViewById(R.id.tvWifiSensorTemp);
		tvTemperatureVal.setText("");

		tvHumidityVal = (TextView) findViewById(R.id.tvWifiSensorHum);
		tvHumidityVal.setText("");

		mSwipeLayout = (VerticalSwipeRefreshLayout) findViewById(R.id.id_swipe_aws_provision_kit);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setVisibility(View.VISIBLE);
		mSwipeLayout.setProgressViewOffset(true, getResources().getDimensionPixelSize(R.dimen.refresher_offset),
				getResources().getDimensionPixelSize(R.dimen.refresher_offset_end));


		// Set toolbar for this screen
		toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		toolbar.setTitle("");
		TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
		main_title.setText(deviceName);
		setSupportActionBar(toolbar);

		// Set navigation drawer for this screen
		mDrawer = (DrawerLayout) findViewById(R.id.device_kit_drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
		mDrawer.addDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
		nDrawer = (NavigationView) findViewById(R.id.nav_gatewaylist_view);
		setNavDrawer();

		View navigationHeader = nDrawer.getHeaderView(0);
		TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(R.id.textViewNavUserSub);
		navHeaderSubTitle.setText(username);

		rbButton1 = (RadioButton) findViewById(R.id.button1);
		rbButton2 = (RadioButton) findViewById(R.id.button2);
		rbButton3 = (RadioButton) findViewById(R.id.button3);

		led_spinner = (Spinner) findViewById(R.id.led_spinner);
		led_spinner.setOnItemSelectedListener(this);

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}



	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume In");

		IntentFilter filter = new IntentFilter(ServiceConstant.CloudStatus);
		filter.addAction(ServiceConstant.JSONShadowMsgReport);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(receiver, filter);

		Intent subscribe_intent = new Intent(WINC1500SecureWiFiBoardActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgSubscribeShadowDelta);
		startService(subscribe_intent);

		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgSubscribeShadowUpdate);
		startService(subscribe_intent);


		onRefresh();

	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
		requestGetDevState = false;
		handler.removeCallbacks(publishPublishGetCmd);

		//unsubscribe the channel
		Intent subscribe_intent = new Intent(WINC1500SecureWiFiBoardActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowDelta);
		startService(subscribe_intent);

		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowUpdate);
		startService(subscribe_intent);


		// TODO GosMessageHandler.getSingleInstance().SetHandler(null);

	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view,
							   int pos, long id) {

		if (requestGetDevState == false) {
			Log.e(LOG_TAG, "----------------->onItemSelected In, pos = " + pos);
			switch (pos)
			{
				case 0:
					changeLEDColor(0,0,1);
					break;
				case 1:
					changeLEDColor(0,1,0);
					break;
				case 2:
					changeLEDColor(1,1,0);
					break;
				case 3:
					changeLEDColor(1,0,0);
					break;
				case 4:
					changeLEDColor(0,1,1);
					break;
				case 5:
					changeLEDColor(1,0,1);
					break;
				case 6:
					changeLEDColor(1,1,1);
					break;


			}


		}
	}

	public void updateLEDColorOnUi(int red, int green, int blue) {
		Log.d(LOG_TAG, "updateLEDColorOnUi: r=" + red + " g=" + green + " b=" + blue);
		int spinner_val = 0;

		if (red == 0 && green == 0 && blue == 1)
			spinner_val = 0;
		else if (red == 0 && green == 1 && blue == 0)
			spinner_val = 1;
		else if (red == 1 && green == 1 && blue == 0)
			spinner_val = 2;
		else if (red == 1 && green == 0 && blue == 0)
			spinner_val = 3;
		else if (red == 0 && green == 1 && blue == 1)
			spinner_val = 4;
		else if (red == 1 && green == 0 && blue == 1)
			spinner_val = 5;
		else if (red == 1 && green == 1 && blue == 1)
			spinner_val = 6;
		else
			spinner_val = -1;

		Log.d(LOG_TAG, "updateLEDColorOnUi: spinner_val=" + spinner_val);
		if (spinner_val >= 0)
			led_spinner.setSelection(spinner_val);

	}

	public void changeLEDColor(int red, int green, int blue) {
		// Is the view now checked?

		AwsShadowJsonMsg json_shadow_msg = new AwsShadowJsonMsg();

		itemInfo item_obj1 = new itemInfo();
		item_obj1.item = "LED_R";
		item_obj1.value = String.valueOf(red);
		json_shadow_msg.desire_info.add(item_obj1);

		itemInfo item_obj2 = new itemInfo();
		item_obj2.item = "LED_G";
		item_obj2.value = String.valueOf(green);
		json_shadow_msg.desire_info.add(item_obj2);

		itemInfo item_obj3 = new itemInfo();
		item_obj3.item = "LED_B";
		item_obj3.value = String.valueOf(blue);
		json_shadow_msg.desire_info.add(item_obj3);

		String msg = json_shadow_msg.generateJsonMsg(json_shadow_msg.AWS_JSON_COMMAND_GENERATE_DESIRE_MSG);

		Intent subscribe_intent = new Intent(WINC1500SecureWiFiBoardActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.putExtra(ServiceConstant.JSONMsgObject,msg);
		subscribe_intent.setAction(ServiceConstant.JSONMsgShadowUpdate);
		startService(subscribe_intent);
	}



	//@Override


	public void onRefresh() {
		handler.sendEmptyMessageDelayed(PULL_TO_REFRESH, 8000);
		//updateUI();
		requestGetDevState = true;
		handler.post(publishPublishGetCmd);

	}

	private Runnable publishPublishGetCmd= new Runnable() {
		public void run() {

			Intent subscribe_intent = new Intent(WINC1500SecureWiFiBoardActivity.this, AwsService.class);
			subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
			subscribe_intent.setAction(ServiceConstant.JSONMsgShadowGet);
			startService(subscribe_intent);


		}
	};


	public void shadowGetUpdateUI(ArrayList<itemInfo> shadow_msg)
	{
		updateType update_option = UNKNOWN;
		RadioButton cur_radio_btn = rbButton1;
		String red = "0";
		String green = "0";
		String blue = "0";
		Integer update_rgb_flag = 0;


		for (int i=0; i<shadow_msg.size(); i++){
			Log.d(LOG_TAG, "Debug:" + shadow_msg.get(i).item + "=" + shadow_msg.get(i).value);



			switch (shadow_msg.get(i).item.toString())
			{
				case "LED_R":
					red = shadow_msg.get(i).value;
					update_rgb_flag = 1;
					break;
				case "LED_G":
					green = shadow_msg.get(i).value;
					update_rgb_flag = 1;
					break;
				case "LED_B":
					blue = shadow_msg.get(i).value;
					update_rgb_flag = 1;
					break;
				case "BUTTON_1":
					cur_radio_btn = rbButton1;
					update_option = updateType.BUTTON;
					break;
				case "BUTTON_2":
					cur_radio_btn = rbButton2;
					update_option = updateType.BUTTON;
					break;
				case "BUTTON_3":
					cur_radio_btn = rbButton3;
					update_option = updateType.BUTTON;
					break;
				case "temp":
					update_option = updateType.TEMP;
					break;
				case "hum":
					update_option = updateType.HUM;
					break;
				case "macAddr":
					update_option = updateType.MAC;
					break;
				default:
					update_option = UNKNOWN;
					break;
			}

			if (update_option == updateType.BUTTON) {
				if (shadow_msg.get(i).value.equals("0"))
					cur_radio_btn.setChecked(true);
				else
					cur_radio_btn.setChecked(false);
			}
			else if (update_option == updateType.TEMP) {
				double val = Double.valueOf(shadow_msg.get(i).value)/100;

				//tvTemperatureVal.setText(String.valueOf(Integer.valueOf(shadow_msg.get(i).value)/100) + "." + String.valueOf(Integer.valueOf(shadow_msg.get(i).value)%100));
				tvTemperatureVal.setText(new DecimalFormat("##.##").format(val));
			}
			else if (update_option == updateType.HUM) {
				tvHumidityVal.setText(shadow_msg.get(i).value.toString());
			}
			else if (update_option == updateType.MAC) {
			}

		}
		if (update_rgb_flag == 1)
		{
			updateLEDColorOnUi(Integer.valueOf(red), Integer.valueOf(green), Integer.valueOf(blue));
		}

	}
	private class awsProvisionKitReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ServiceConstant.CloudStatus)) {
				String connMessage = intent.getStringExtra(ServiceConstant.CloudStatusConn);
				if (connMessage.equals("Connected")) {
				}
			}
			else if (intent.getAction().equals(ServiceConstant.JSONShadowMsgReport)) {

				handler.sendEmptyMessage(REFRESH_ICON_DISMISS);
				requestGetDevState = false;
				AwsShadowJsonMsg jsonShadowMsgObj = intent.getParcelableExtra(ServiceConstant.JSONShadowMsgObject);
				if (jsonShadowMsgObj != null) {
					Log.d(LOG_TAG, "Receive AWS Shadow JSON message");
					jsonShadowMsgObj.printDebugLog();
					ArrayList<itemInfo> report_info_shadow = jsonShadowMsgObj.getReportInfo();
					shadowGetUpdateUI(report_info_shadow);

					ArrayList<itemInfo> desire_info_shadow = jsonShadowMsgObj.getDesireInfo();
					shadowGetUpdateUI(desire_info_shadow);

					String topic = intent.getStringExtra(ServiceConstant.MQTTChannelName);
					Log.d(LOG_TAG, "Topic:" + topic);
					String[] split = topic.split("/");

					AwsRouter
							router = new AwsRouter();

					router.setDeviceName("Secure WiFi Board");

					for (int i=0; i<report_info_shadow.size(); i++){
						if (report_info_shadow.get(i).item.equals("macAddr"))
							router.setMacAddr(report_info_shadow.get(i).value);
					}

					//router.setMacAddr(split[2]);
					router.setDevType(split[2]);
					router.setThingName(split[2]);
					
				}
			}
		}
	}
}
