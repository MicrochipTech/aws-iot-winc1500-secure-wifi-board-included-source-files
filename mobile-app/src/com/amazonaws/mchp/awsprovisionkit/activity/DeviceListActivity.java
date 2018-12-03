package com.amazonaws.mchp.awsprovisionkit.activity;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mchp.awsprovisionkit.base.MyThreadPool;
import com.amazonaws.mchp.awsprovisionkit.model.AwsZeroTouchProvisionKitDevice;
import com.amazonaws.mchp.awsprovisionkit.model.itemInfo;
import com.amazonaws.mchp.awsprovisionkit.opensource.downloader.asyn.AsyncTask;
import com.amazonaws.mchp.awsprovisionkit.task.json.AwsShadowJsonMsg;
import com.amazonaws.mchp.awsprovisionkit.task.net.MsgData;
import com.amazonaws.mchp.awsprovisionkit.task.net.MsgMulticast;
import com.amazonaws.mchp.awsprovisionkit.task.net.MyConfig;
import com.amazonaws.mchp.awsprovisionkit.task.net.MyHelper;
import com.amazonaws.mchp.awsprovisionkit.task.net.WlanAdapter;
import com.amazonaws.mchp.awsprovisionkit.task.ui.*;
import com.amazonaws.mchp.awsprovisionkit.utils.*;
import com.amazonaws.mchp.awsprovisionkit.task.json.AwsJsonMsg;
import com.amazonaws.mchp.awsprovisionkit.service.AwsService;
import com.amazonaws.mchp.awsprovisionkit.R;
import com.amazonaws.mchp.awsprovisionkit.task.ui.SlideListView;
import com.amazonaws.mchp.awsprovisionkit.model.AwsRouter;
import com.amazonaws.mchp.awsprovisionkit.adapter.*;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoJWTParser;

import zxing.CaptureActivity;


@SuppressLint("HandlerLeak")
public class DeviceListActivity  extends AppCompatActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener {

	static final String LOG_TAG = DeviceListActivity.class.getCanonicalName();

	/** The ll NoDevice */
	private ScrollView llNoDevice;

	/** The img NoDevice */
	private ImageView imgNoDevice;

	/** The btn NoDevice */
	private Button btnNoDevice;

	/** The ic FoundDevices */
	private View icFoundDevices;

	/** The tv FoundDevicesListTitle */
	private TextView tvFoundDevicesListTitle;

	/** The slv FoundDevices */
	private SlideListView slvFoundDevices;

	/** The sv ListGroup */
	private ScrollView svListGroup;

	TextView aDiaglogText;
	private View aDiagView;

	/** 适配器 */
	gatewayListAdapter myadapter;

	/** 设备列表分类 */
	List<AwsRouter> foundDevicesList;

	/** 设备热点名称列表 */
	ArrayList<String> softNameList;

	/** 与APP绑定的设备的ProductKey */
	private List<String> ProductKeyList;

	Intent intent;

	public static List<String> boundMessage;

	public ProgressDialog progressDialog;

	protected static final int GETLIST = 0;


	protected static final int UPDATALIST = 1;


	protected static final int TOCONTROL = 2;


	protected static final int TOAST = 3;

	/* Display progress diaglog */
	protected static final int PROGRESSDIAG= 4;

	protected static final int PROGRESSDIAG_DISMISS= 5;

	protected static final int BOUND = 9;


	protected static final int UNBOUND = 99;

	protected static final int SHOWDIALOG = 999;

	private static final int PULL_TO_REFRESH = 888;

	private String idToken;
	private CognitoUser user;
	private String username;

	private VerticalSwipeRefreshLayout mSwipeLayout;

	private VerticalSwipeRefreshLayout mSwipeLayout1;

	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;

	private View promptsView;

	private 	EditText userInput;

	private LayoutInflater li;
	private NavigationView nDrawer;
	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar toolbar;

    private Integer mPubCounter;

	gatewayListJsonMsgReceiver receiver;

	WlanAdapter mWifiAdapter = null;

	final Context context = this;
	String provision_passphrase;
	public String devName;
	public WifiConfiguration currentApInfo = null;
    public String uuid;


	String[] thing_name = new String[10];
	ArrayList<AwsZeroTouchProvisionKitDevice> thingList = new ArrayList<AwsZeroTouchProvisionKitDevice>();
	int number_of_thing = 0;
	private String filename = "config.txt";
	private String filepath = "MyFileStorage";
	File myExternalFile;


	private class NetworkProvisionTask extends AsyncTask<String, Integer, String> {

		public Boolean isStoped = false;
		//private WifiConfiguration currentApInfo = null;

		private void printProgressDiagMsg(String text)
		{

			Message msg = new Message();
			msg.what = PROGRESSDIAG;
			msg.obj = text;
			handler.sendMessage(msg);

		}
		private void clearProgressDiagMsg()
		{

			Message message = new Message();
			message.what = PROGRESSDIAG_DISMISS;
			handler.sendMessage(message);

		}
		private ScanResult searchTargetDevice(List<ScanResult> list, String ssid)
		{

			for ( ScanResult sr : list) {
				if (null == sr.SSID || sr.SSID.isEmpty())
					continue;
				if (sr.SSID.equals(ssid)) {
					return sr;
				}
			}
			return null;
		}
		@Override
		protected String doInBackground(String... params) {
			MyHelper.d(">>>> Start doInBackground ....");

			/* Stage 1: Scan the device AP */
			if (params[0].equals("stage1")) {

				printProgressDiagMsg("Scanning Device...");
				List<ScanResult> rdata = mWifiAdapter.tryGetWifiList();
				if (isStoped || this.isCancelled())
					return null;

				if (rdata == null) {
					return null;
				}
				ArrayList<ScanResult> data = new ArrayList<ScanResult>();
				String targetSsid = "AWSZeroTouchKit_" + params[1];

				ScanResult sr = searchTargetDevice(rdata, targetSsid);
				if (sr == null)
					return MyConfig.ERR_ScanAPFail;

				currentApInfo = mWifiAdapter.getCurrentAPInfo();

				printProgressDiagMsg("Connecting Device...");
				Boolean r1 = mWifiAdapter.tryConnectWlan(sr, "12345678");
				if (r1)
					MyHelper.d(">>>> Success connected to wifi, SSID= " + sr.SSID);
				else
					return MyConfig.ERR_ConnectDevFail;

				return "stage1"; 	// finish stage1 to scan the deivce, then show alertDiag for user to enter password
			}
			else
			{
				MyHelper.d(">>>> Start stage 2 ....");
				if (params[1].length()<8)
					return "passphase len is worng";

				// send discovery command
				String result1, result2;
				MyHelper.d(">>>> Start stage 2-1: send DiscoveryCmd to device");
				result1 = this.sendDiscoveryCmd();
				if (MyConfig.Success_ConnectDev == result1) {
					MyHelper.d(">>>> Start stage 2-2: send provision data to device");
					result2 = this.networkProvision();
				}
				else
					return result1;

				if (result2.equals(MyConfig.ERR_SendProvDataFail))
					return result2;

				mWifiAdapter. removeCurrentAP();
				return  "stage2";	// success finish stage 2
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			if (result.equals("stage1")) {
				MyHelper.d(">>>> Finish stage 1: Scan and connect to device");
				Message msg = new Message();
				msg.what = SHOWDIALOG;
				msg.obj = currentApInfo.SSID;
				handler.sendMessage(msg);
			}
			else if (result.equals("stage2")){
				MyHelper.d(">>>> Finish stage 2: finish provisioning");
				printProgressDiagMsg("Provisioning...");
				Boolean r1 = mWifiAdapter.tryConnectWlan(currentApInfo);
			}
			else if (result.equals(MyConfig.ERR_ScanAPFail))
			{
				MyHelper.d(">>>> Fail stage 1: fail to scan the device");
				printProgressDiagMsg(result);
				//ToDo: Add delay to cancel the diaglog
			}
			else{
				printProgressDiagMsg(result);
				Boolean r1 = mWifiAdapter.tryConnectWlan(currentApInfo);
			}
		}

		public void tryStop() {
			this.isStoped = true;
			try {
				if (this.getStatus() != Status.FINISHED)
					this.cancel(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String sendDiscoveryCmd() {
			MsgData msgData = MsgMulticast.single().tryDiscovery1();
			if (msgData == null)
				return MyConfig.ERR_ConnectDevFail;

			if (msgData.hasError()) {
				MyHelper.d(">>>> tryGetPlugInfo log2");
				return msgData.getError();
			} else {
				String mac = msgData.MAC;
				String thingID = msgData.ThingID;
				printProgressDiagMsg("Provisioning...");

				MyHelper.d(">>>> Get device Mac="+ mac);
				MyHelper.d(">>>> Get device ThingID="+ thingID);

				Intent subscribe_intent = new Intent(DeviceListActivity.this, AwsService.class);
				subscribe_intent.putExtra(ServiceConstant.DevMacAddr,thingID);
				subscribe_intent.putExtra(ServiceConstant.DevName,devName);
				subscribe_intent.setAction(ServiceConstant.UpdateAcctInfoToDB);
				startService(subscribe_intent);

			}

			return MyConfig.Success_ConnectDev;
		}

		String networkProvision() {
			MsgData msgData = MsgMulticast.single().tryDiscovery2(currentApInfo, provision_passphrase, uuid);
			if (msgData == null)
				return MyConfig.ERR_SendProvDataFail ;

			return MyConfig.Success_SendProvData;
		}
	}


	Handler handler = new Handler() {
		private AlertDialog myDialog;
		private TextView dialog_name;

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GETLIST:

				break;

			case UPDATALIST:
				//if (progressDialog.isShowing()) {
				//	progressDialog.cancel();
				//}
				UpdateUI();
				break;

			case BOUND:

				break;

			case UNBOUND:
				//progressDialog.show();
				break;

			case TOCONTROL:

				break;

				case PROGRESSDIAG:
				progressDialog.setTitle("Loading");
				progressDialog.setMessage(msg.obj.toString());
				progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
				progressDialog.show();
				break;

				case PROGRESSDIAG_DISMISS:
					if (progressDialog.isShowing())
					progressDialog.dismiss();
					break;
			case TOAST:
				Toast.makeText(DeviceListActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
				break;

			case PULL_TO_REFRESH:
				handler.sendEmptyMessage(GETLIST);
				mSwipeLayout.setRefreshing(false);
				mSwipeLayout1.setRefreshing(false);

				break;

			case SHOWDIALOG:

				String str="Provision to AP "+msg.obj.toString();
				aDiaglogText.setText(str);

				alertDialog.show();
				break;
			}
		};
	};

	private void writeToFile(String data,Context context) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		}
	}
	private String readFromFile(Context context) {

		String ret = "";

		try {
			InputStream inputStream = context.openFileInput("config.txt");

			if ( inputStream != null ) {
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ( (receiveString = bufferedReader.readLine()) != null ) {
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		}
		catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		}

		Log.e("login activity", "re: " + ret);
		return ret;
	}

	protected  void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		///setTheme(R.style.CognitoAppTheme);
		setContentView(R.layout.activity_gateway_list);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//setTheme(R.style.AppTheme);
		//setContentView(R.layout.activity_gateway_list);
		handler.sendEmptyMessage(GETLIST);
		foundDevicesList = new ArrayList<AwsRouter>();
		boundMessage = new ArrayList<String>();
		mWifiAdapter = new WlanAdapter(this);
		progressDialog = new ProgressDialog(context);

		Intent intent = getIntent();
		idToken = intent.getStringExtra("idToken");
		Log.d(LOG_TAG, "Debug  idtoken="+idToken);
		uuid = CognitoJWTParser.getClaim(idToken, "sub");
		Log.d(LOG_TAG, ">>>>>>>>>>> user uuid = " + uuid);

		username = AppHelper.getCurrUser();
		user = AppHelper.getPool().getUser(username);

		initView();
		initEvent();

        receiver = new gatewayListJsonMsgReceiver();

		onRefresh();

		mSwipeLayout.setRefreshing(true);
		/*
		thing_name = AppHelper.getThingNamefromConfigFile(context);
		for (int j = 0; j< thing_name.length; j++) {
			if (thing_name[j] != null)
				Log.e(LOG_TAG, ">>>>>>>>>-- thing_name = " + thing_name[j]);
		}
		*/
		String test_value = AppHelper.getStringfromConfigFile(context, "COGNITO_POOL_ID");
		Log.e(LOG_TAG, ">>>>>>>>>-- COGNITO_POOL_ID = " + test_value);

		Intent subscribe_intent = new Intent(DeviceListActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.CognitoUuid,uuid);
		subscribe_intent.setAction(ServiceConstant.ScanThingID);
		startService(subscribe_intent);

	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "Debug: gatewayLIstActivity 8888888888888888888");
        IntentFilter filter = new IntentFilter(ServiceConstant.CloudStatus);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(ServiceConstant.JSONMsgReport);
		filter.addAction(ServiceConstant.JSONShadowMsgReport);
		filter.addAction(ServiceConstant.ThingIDListReport);
        registerReceiver(receiver, filter);

		if (boundMessage.size() != 0) {

			if (boundMessage.get(0) == "QR Mac") {
				if (boundMessage.get(1) == "invalid")
					Toast.makeText(DeviceListActivity.this, "Wrong devices", Toast.LENGTH_SHORT).show();
				else {
					Toast.makeText(DeviceListActivity.this, "add dev: "+boundMessage.get(1), Toast.LENGTH_LONG).show();

                    NetworkProvisionTask taskScanDevAP = new NetworkProvisionTask();
                    taskScanDevAP.executeOnExecutor(MyThreadPool.getExecutor(), "stage1", boundMessage.get(1));
				}
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		boundMessage.clear();

		mPubCounter = 0;
		handler.removeCallbacks(publishPublishSearchCmd);

        unregisterReceiver(receiver);
		// TODO GosMessageHandler.getSingleInstance().SetHandler(null);

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
				finish();


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
		svListGroup = (ScrollView) findViewById(R.id.svListGroup);
		llNoDevice = (ScrollView) findViewById(R.id.llNoDevice);
		imgNoDevice = (ImageView) findViewById(R.id.imgNoDevice);
		btnNoDevice = (Button) findViewById(R.id.btnNoDevice);

		icFoundDevices = findViewById(R.id.icFoundDevices);
		slvFoundDevices = (SlideListView) icFoundDevices.findViewById(R.id.slideOnlineListView);
		tvFoundDevicesListTitle = (TextView) icFoundDevices.findViewById(R.id.tvListViewTitle);
		tvFoundDevicesListTitle.setText("Online Device Kit");
		mSwipeLayout = (VerticalSwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setOnRefreshListener(this);

		mSwipeLayout1 = (VerticalSwipeRefreshLayout) findViewById(R.id.id_swipe_ly1);
		mSwipeLayout1.setOnRefreshListener(this);

		// Set toolbar for this screen
		toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		toolbar.setTitle("");
		TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
		main_title.setText("Device List");
		setSupportActionBar(toolbar);

		// Set navigation drawer for this screen
		mDrawer = (DrawerLayout) findViewById(R.id.gatewaylist_drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
		mDrawer.addDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
		nDrawer = (NavigationView) findViewById(R.id.nav_gatewaylist_view);
		setNavDrawer();

		View navigationHeader = nDrawer.getHeaderView(0);
		TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(R.id.textViewNavUserSub);
		navHeaderSubTitle.setText(username);

		li = LayoutInflater.from(context);
		promptsView = li.inflate(R.layout.content_new_ssid_password, null);
		aDiaglogText = (TextView)promptsView.findViewById(R.id.aDiagTextView1);
		aDiaglogText.setText("");

		alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);

		// set dialog message for network provision
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								MyHelper.d(">>>Password="+userInput.getText());

								provision_passphrase = userInput.getText().toString();

                                NetworkProvisionTask taskNetworkProvsion = new NetworkProvisionTask();
								taskNetworkProvsion.executeOnExecutor(MyThreadPool.getExecutor(), "stage2", provision_passphrase);

								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		alertDialog = alertDialogBuilder.create();

		Spinner spinner = (Spinner)promptsView.findViewById(R.id.spinner1);
		final String[] devLoc = {"Kitchen", "Dinning Room", "Living Room", "Bedroom", "Bathroom"};
		ArrayAdapter<String> lunchList = new ArrayAdapter<>(DeviceListActivity.this,
				android.R.layout.simple_spinner_dropdown_item,
				devLoc);
		spinner.setAdapter(lunchList);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.d(LOG_TAG, "your selection is " + devLoc[position]);
				devName = devLoc[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


	}

	private void initEvent() {

		imgNoDevice.setOnClickListener(this);
		btnNoDevice.setOnClickListener(this);

		slvFoundDevices.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {


				//Intent activity_intent = new Intent(DeviceListActivity.this, AwsProvisionKitActivity.class);
				Intent activity_intent = new Intent(DeviceListActivity.this, WINC1500SecureWiFiBoardActivity.class);
				Bundle extras = new Bundle();
				extras.putString(ServiceConstant.DevMacAddr, foundDevicesList.get(position).getMacAddr());
				extras.putString(ServiceConstant.ThingName,foundDevicesList.get(position).getThingName());
				extras.putString(ServiceConstant.DevName,foundDevicesList.get(position).getDeviceName());

				activity_intent.putExtras(extras);
				startActivity(activity_intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.devicelist_login, menu);
		getMenuInflater().inflate(R.menu.devicelist_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case android.R.id.home:
				break;
			case R.id.action_QR_code:

				Intent activity_intent = new Intent(DeviceListActivity.this, CaptureActivity.class);
				activity_intent.putExtra(ServiceConstant.CallerCmd,"gatewayListActivity");
				startActivity(activity_intent);

				break;
			case R.id.action_addDevice:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void UpdateUI() {

			llNoDevice.setVisibility(View.GONE);
			mSwipeLayout1.setVisibility(View.GONE);
			svListGroup.setVisibility(View.VISIBLE);


		if (foundDevicesList.isEmpty()) {
			slvFoundDevices.setVisibility(View.GONE);
		} else {
			myadapter = new gatewayListAdapter(this, foundDevicesList);
			slvFoundDevices.setAdapter(myadapter);
			slvFoundDevices.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgNoDevice:
		case R.id.btnNoDevice:
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click();

		}
		return false;
	}

	/**
	 * 双击退出函数
	 */
	private static Boolean isExit = false;

	public void exitBy2Click() {
		Timer tExit = null;
			isExit = true; // 准备退出；
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	public Handler getMyHandler() {

		return handler;

	}

//	public void onRefresh() {
//		handler.sendEmptyMessageDelayed(PULL_TO_REFRESH, 2000);
//
//	}

	public void onRefresh() {
		handler.sendEmptyMessageDelayed(PULL_TO_REFRESH, 2000);
		Log.d(LOG_TAG, "Debug: gatewayLIstActivity Test77777777777");

		foundDevicesList.clear();
		UpdateUI();

        mPubCounter = 1;    // Publish Search command for 3 time
        handler.post(publishPublishSearchCmd);




	}


    private Runnable publishPublishSearchCmd= new Runnable() {
        public void run() {

            if (mPubCounter > 0) {
                mPubCounter --;

				Log.d(LOG_TAG, "Debug: publishPublishSearchCmd thingList.size="+thingList.size());


				for (int j = 0; j<thingList.size(); j++) {
					if (thingList.get(j).getThingID() != null) {
						final Intent subscribe_intent = new Intent(DeviceListActivity.this, AwsService.class);
						subscribe_intent.putExtra(ServiceConstant.AWSThingName, thingList.get(j).getThingID());
						subscribe_intent.setAction(ServiceConstant.JSONMsgShadowGet);
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								// Do something after 1s = 1000ms

								startService(subscribe_intent);
							}
						}, j*500);
						//startService(subscribe_intent);
						//handler.postDelayed(this, 2500);
					}
				}


				///handler.postDelayed(this, 2500);        // send every 2.5 second
            }
        }
    };

	private class gatewayListJsonMsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ServiceConstant.CloudStatus)) {

				String connMessage = intent.getStringExtra(ServiceConstant.CloudStatusConn);

				if (connMessage.equals("Connected")) {
					Message message = new Message();
					message.what = PROGRESSDIAG_DISMISS;
					handler.sendMessage(message);

					onRefresh();

					Intent subscribe_intent = new Intent(DeviceListActivity.this, AwsService.class);
					subscribe_intent.putExtra(ServiceConstant.CognitoUuid,uuid);
					subscribe_intent.setAction(ServiceConstant.ScanThingID);
					startService(subscribe_intent);
				}
				else if (connMessage.equals("Re-Connecting")) {
					///Message message = new Message();
					///message.what = PROGRESSDIAG_DISMISS;
					///handler.sendMessage(message);
				}

			} else if (intent.getAction().equals(ServiceConstant.JSONMsgReport)) {
				AwsJsonMsg jsonMsgObj = intent.getParcelableExtra(ServiceConstant.JSONMsgObject);
				if (jsonMsgObj != null) {
					Log.d(LOG_TAG, "Debug: Receive JSON message");
					jsonMsgObj.printDebugLog();
					int i, deviceExist=0;

					AwsRouter router;
					if (jsonMsgObj.getCmd().equals(AwsJsonMsg.AWS_JSON_COMMAND_SEARCHRESP)) {
						for (i = 0; i < foundDevicesList.size(); i++) {
							if (foundDevicesList.get(i).getMacAddr().equals(jsonMsgObj.getMacAddr())) {
								deviceExist = 1;
							}
						}
						if (deviceExist == 0) {
							router = new AwsRouter();
							if (jsonMsgObj.getDevType().equals(AwsJsonMsg.AWS_JSON_DEVTYPE_WIFISENSORBOARD)) {
								Log.d(LOG_TAG, "Debug: getDeviceName=" + jsonMsgObj.getMacAddr());
								router.setDeviceName(jsonMsgObj.getDeviceName());
							} else
								router.setDeviceName(jsonMsgObj.getDevType());

							router.setMacAddr(jsonMsgObj.getMacAddr());
							router.setDevType(jsonMsgObj.getDevType());
							foundDevicesList.add(router);
							handler.sendEmptyMessage(UPDATALIST);
						}
					}
				}
			}else if (intent.getAction().equals(ServiceConstant.JSONShadowMsgReport)) {
				AwsShadowJsonMsg jsonShadowMsgObj = intent.getParcelableExtra(ServiceConstant.JSONShadowMsgObject);
				if (jsonShadowMsgObj != null) {
					Log.d(LOG_TAG, "Receive AWS Shadow JSON message");
					jsonShadowMsgObj.printDebugLog();
					ArrayList<itemInfo> report_info_shadow = jsonShadowMsgObj.getReportInfo();
					for (int i=0; i<report_info_shadow.size(); i++){
						Log.d(LOG_TAG, "Debug:" + report_info_shadow.get(i).item + "=" + report_info_shadow.get(i).value);
					}
					String topic = intent.getStringExtra(ServiceConstant.MQTTChannelName);
					Log.d(LOG_TAG, "Topic:" + topic);
					String[] split = topic.split("/");

					boolean found = false;
					for (int i=0; i< foundDevicesList.size(); i++)
					{
						if (foundDevicesList.get(i).getMacAddr().equals(split[2]))
						{
							found = true;
							break;
						}
					}

					if (found == false) {
						AwsRouter router = new AwsRouter();

						router.setDeviceName("AWS Zero Touch Kit");    //default
						for (int i = 0; i < thingList.size(); i++) {
							if (split[2].equals(thingList.get(i).getThingID()))
								if (thingList.get(i).getDeviceName().equals("null")) {
									Log.d(LOG_TAG, "Debug log1");
									router.setDeviceName("Secure Wi-Fi Board");
								}
								else
								{
									Log.d(LOG_TAG, "Debug log2 " + thingList.get(i).getDeviceName());
									router.setDeviceName(thingList.get(i).getDeviceName() + " Board");
								}

						}


						router.setMacAddr(split[2]);    //thing id
						router.setDevType(split[2]);
						router.setThingName(split[2]);
						foundDevicesList.add(router);
						handler.sendEmptyMessage(UPDATALIST);
					}

				}
			}
			else if (intent.getAction().equals(ServiceConstant.ThingIDListReport)) {
				Log.d(LOG_TAG, "Receive ThingIDListReport");
				thingList.clear();
				int j = 0;
				// clear thing_name
				for (int i = 0; i< thing_name.length; i++) {
					thing_name[i] = null;
				}
				Bundle b = intent.getExtras();
				if (null != b) {
					ArrayList<String> arr = b.getStringArrayList(ServiceConstant.ThingIdList);
					for (int i=0; i< arr.size(); i++)
					{
						if (arr.get(i).contains("thingName")){
							AwsZeroTouchProvisionKitDevice thing = new AwsZeroTouchProvisionKitDevice();
							String[] split = arr.get(i).split(":");


							String[] IDSplit = split[0].split("=");
							thing_name[j] = IDSplit[1];
							thing.setThingID(IDSplit[1]);

							String[] NameSplit = split[1].split("=");
							thing.setDeviceName(NameSplit[1]);
							Log.i(LOG_TAG, "Added thing_ID :: " + thing.getThingID()+" thing_name :: " + thing.getDeviceName());



							thingList.add(thing);
							j++;

						}

					}
					Log.i("List", "Passed Array List :: " + arr);
				}
				onRefresh();
				//handler.post(publishPublishSearchCmd);


			}


		}
	}
}
