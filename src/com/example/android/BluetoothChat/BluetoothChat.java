/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Int2;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
/**
 * ����ARM����������� �������� ��׿ң�ض˳���
 *                ɽ����ѧ(����)  
 */
@SuppressLint("NewApi")
public class BluetoothChat extends Activity implements SensorEventListener {
	// ����
	private long exitTime = 0;
	private SeekBar seekBar = null;
	private static final String TAG = "BluetoothChat";
	private static boolean D = true;
	private static final String info = "junge";
	// ���͵���Ϣ���ʹ�bluetoothchatservice�������
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
    public static final String BluetoothData = "fullscreen";
	public String filename = ""; // ��������洢���ļ���
	private String newCode = "";
	private String newCode2 = "";
	private String fmsg = ""; // ���������ݻ���
	// �����ִ��յ���bluetoothchatservice�������
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// 

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// Intent��Ҫ ����
	public static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// ���ֿؼ�
	private TextView mTitle;
	private EditText mInputEditText;
	private EditText mOutEditText;
	private EditText mOutEditText2;
	private Button mSendButton;
	private CheckBox HEXCheckBox;
	private Button breakButton;
	private CheckBox checkBox_sixteen;
	

	
	// ���ֵ�����װ��
	private String mConnectedDeviceName = null;
	// ������Ϣ���ַ���������
	private StringBuffer mOutStringBuffer;
	// ���ص�����������
	private BluetoothAdapter mBluetoothAdapter = null;
	// ��Ա������������
	private BluetoothChatService mChatService = null;
	// ���ñ�ʶ����ѡ���û����ܵ����ݸ�ʽ
	private boolean dialogs;
	
    //��һ���������-->����
	private int sum =1;
	private int UTF =1;
	

	// �����񵳼�¼�������������׽���
	String mmsg = "";
	String mmsg2 = "";

	//��������ָ��
	private byte[] getCtr =new byte[32];
	private TextView valThrottle = null;
	private int count;
	private boolean isUnlock=true;
	
	//�������ƿؼ�
	private Button btnUnlock = null;
	private Button btnStop= null;
	private Button btnRead= null;
	private Button btnSave= null;
	//
	private TextView rc_roll=null;
	private TextView rc_pit=null;
	  private SensorManager mSensorManager;  
	    private Sensor mSensor;  
	    private float mX, mY, mZ;  
	    private long lasttimestamp = 0;  
	    Calendar mCalendar;
	 private int THROTTLE;
	 private int connect;
	 private int roll;
	 private int pitch;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
       
		D = false;
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		Log.i(info, "" + dialogs);
		// ���ô��ڲ���
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				
				R.layout.custom_title);
		//ʵ�������ƿؼ�
		btnRead=(Button)findViewById(R.id.btn_read);
		btnSave=(Button)findViewById(R.id.btn_save);
		
		btnUnlock=(Button)findViewById(R.id.btn_unlock);
	
		rc_roll=(TextView)findViewById(R.id.rc_roll);
		rc_pit=(TextView)findViewById(R.id.rc_pitch);
		valThrottle=(TextView)findViewById(R.id.text_val);
		seekBar = (SeekBar) findViewById(R.id.throttle);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY  
        if (null == mSensorManager) {  
            Log.d(TAG, "deveice not support SensorManager");  
        }  
        // �����������ľ�׼��  
        mSensorManager.registerListener(this, mSensor,  
                SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME  
		btnUnlock.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isUnlock==true){
					btnUnlock.setText("���ż���");				
					isUnlock=false;
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) 0;
					getCtr[3]=(byte) 0;
					getCtr[4]=(byte) 50;
					getCtr[5]=(byte) 50;
					getCtr[6]=(byte) (50);
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0xA5;
					sendHEX(getCtr);
				}
				else{
					btnUnlock.setText("���Ž���");
					isUnlock=true;
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) 0;
					getCtr[3]=(byte) 0;
					getCtr[4]=(byte) 50;
					getCtr[5]=(byte) 50;
					getCtr[6]=(byte) (50);
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0x00;
					sendHEX(getCtr);
					seekBar.setProgress(0);
				}
			}
			
		});
		btnRead.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Uri uri = Uri.parse("http://www.crazepony.com");  
				Intent it = new Intent(Intent.ACTION_VIEW, uri);  
				startActivity(it);
			}
		
		});
		btnSave.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if((System.currentTimeMillis()-exitTime) > 2000){  
		            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();                                
		            exitTime = System.currentTimeMillis();   
		        } else {
		            finish();
		            System.exit(0);
		        }
			}
		
		});
	
        //���øý����������ֵ,Ĭ�������ΪO
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
        	
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				connect=1;
				THROTTLE=seekBar.getProgress();
				valThrottle.setText("����:\n"+THROTTLE);
				
				if(THROTTLE>50){
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
					getCtr[3]=(byte) ((byte)(THROTTLE>>8));
					getCtr[4]=(byte) pitch;
					getCtr[5]=(byte) roll;
					getCtr[6]=(byte) 50;
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0xA5;
				}
				else{
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
					getCtr[3]=(byte) ((byte)(THROTTLE>>8));
					getCtr[4]=(byte) pitch;
					getCtr[5]=(byte) roll;
					getCtr[6]=(byte) 50;
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0x00;
				}
			//	if(THROTTLE<=20)getCtr[31]=(byte) 0x00;
				
				
			
				if(count>=10){//��Ҫ��̫�࣬����ʮ�λ�����һ��ң������
					sendHEX(getCtr);
					count=0;
				}
				else{
					count++;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				THROTTLE=seekBar.getProgress();
				valThrottle.setText("����:\n"+THROTTLE);
				
				if(THROTTLE>50){
				getCtr[0]=(byte) 0xAA;
				getCtr[1]=(byte) 0xBB;
				getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
				getCtr[3]=(byte) ((byte)(THROTTLE>>8));
				getCtr[4]=(byte) pitch;
				getCtr[5]=(byte) roll;
				getCtr[6]=(byte) (50);
				for(int j=7;j<30;j++)getCtr[j]=0;
				getCtr[31]=(byte) 0xA5;}
				else{
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
					getCtr[3]=(byte) ((byte)(THROTTLE>>8));
					getCtr[4]=(byte) pitch;
					getCtr[5]=(byte) roll;
					getCtr[6]=(byte) (50);
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0x00;}
				
				sendHEX(getCtr);
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				seekBar.getProgress();
				THROTTLE=seekBar.getProgress();
				valThrottle.setText("����:\n"+THROTTLE);
				if(THROTTLE>50){
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
					getCtr[3]=(byte) ((byte)(THROTTLE>>8));
					getCtr[4]=(byte) pitch;
					getCtr[5]=(byte) roll;
					getCtr[6]=(byte) (50);
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0xA5;
				}else{
					getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
					getCtr[3]=(byte) ((byte)(THROTTLE>>8));
					getCtr[4]=(byte) pitch;
					getCtr[5]=(byte) roll;
					getCtr[6]=(byte) (50);
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0x00;
				}
				//if(THROTTLE==0)getCtr[31]=(byte) 0x00;
				sendHEX(getCtr);
			}
        });
        
		mInputEditText = (EditText) findViewById(R.id.editText1);
		mInputEditText.setGravity(Gravity.TOP);
		mInputEditText.setSelection(mInputEditText.getText().length(),
		mInputEditText.getText().length());
		mInputEditText.clearFocus();
		mInputEditText.setFocusable(false);
		//����ImageView
		
		
		// �����ı��ı���
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// ��ʼ��Radiobutton]
		HEXCheckBox = (CheckBox) findViewById(R.id.radioMale);
		breakButton = (Button) findViewById(R.id.button_break);
		// �õ����ص�����������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// ��ʼ��CheckBox
		checkBox_sixteen = (CheckBox) findViewById(R.id.checkBox_sixteen);
		//���ͼƬ��ת����˾ҳ��
		

		 if(getWindow().getAttributes().softInputMode==WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)

		 {

		   //���������

		   getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		 }


		// ��ʼ��Socket
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}
	    checkBox_sixteen
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
//						String getValue = mInputEditText.getText().toString();
//						if (isChecked) {
//							mInputEditText.setText(CodeFormat.stringToHex(getValue));
//							
//						} else {
//							mInputEditText.setText(fmsg);
//
//						}
					}
				});
		HEXCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mOutEditText.setText("");
					mOutEditText.setVisibility(View.GONE);
					mOutEditText2.setVisibility(View.VISIBLE);
				} else {
					mOutEditText.setVisibility(View.VISIBLE);
					mOutEditText2.setVisibility(View.GONE);
				}

			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// �����û�У�Ҫ�������á�
		// setupchat()������Ϊ��onactivityresult
		if (!mBluetoothAdapter.isEnabled()) {
	//��Ϊ����������ʾ�������Ч��fu'c'k		
    //			mBluetoothAdapter.enable();
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// ������������Ự
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	// ���Ӱ�����Ӧ����
	public void onConnectButtonClicked(View v) {

		if (breakButton.getText().equals("��������")||breakButton.getText().equals("connect")) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // ��ת��������
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
			breakButton.setText(R.string.duankai);

		} else {
			// �ر�����socket
			try {
				// �ر�����
				breakButton.setText(R.string.button_break);
				mChatService.stop();

			} catch (Exception e) {
			}
		}
		return;
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// ִ�д˼��onresume()���ǵİ����У�Ӣ������
		// ������onstart()����������ͣ����������
		// onresume()��������ʱ��action_request_enable����ء�
		if (mChatService != null) {
			// ֻ�й�����state_none������֪�������ǻ�û�п�ʼ
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// ���������������
				mChatService.start();
			}
		}
	}

	@SuppressLint("NewApi")
	private void setupChat() {
		Log.d(TAG, "setupChat()");
		// ��ʼ��׫д�����ڵķ��ؼ�
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);
		mOutEditText2 = (EditText) findViewById(R.id.edit_text_out2);
		mOutEditText2.setOnEditorActionListener(mWriteListener);

		// ��ʼ�����Ͱ�ť�������¼�������
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// ������Ϣʹ�����ݵ��ı��༭�ؼ�
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				TextView view2 = (TextView) findViewById(R.id.edit_text_out2);
				String message = view.getText().toString();
				String message2 = view2.getText().toString();
				
				try {
					message.getBytes("ISO_8859_1");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
				if (HEXCheckBox.isChecked()) {
				//	sendMessage(message2);
			
					
				}else {
					
					sendMessage(message);
				}
			}
		});

		// ��ʼ��bluetoothchatserviceִ����������
		mChatService = new BluetoothChatService(this, mHandler);

		// ��������ʼ��������Ϣ
		mOutStringBuffer = new StringBuffer("");
	}

	public void onMyButtonClick(View view) {
		if (view.getId() == R.id.button_clean) {
			mInputEditText.setText("");
			fmsg="";
			sum =0;
		}
		if (view.getId() == R.id.button_break) {

			onConnectButtonClicked(breakButton);
		}
		if (view.getId()== R.id.button_full_screen) {
			String Data =mInputEditText.getText().toString();
			if (Data.length()>0) {
				Intent intent = new Intent(); 
			intent.putExtra(BluetoothData,Data);
			//intent.setClass(BluetoothChat.this, FullScreen.class);
			startActivity(intent); 
			}else {
				Toast.makeText(this, R.string.prompt_message, Toast.LENGTH_LONG).show();
			}
			
		}
		
	}
	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// �����������վ
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	@SuppressLint("NewApi")
	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
	/**
	 * ����һ���ֽ�
	 * 
	 * @param message
	 *            һ���ı��ַ�������.
	 */
	private void sendHEX(byte[] ctrlVal) {
		// �������ʵ�������κ�����
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
		//	Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();//��ʾδ����
			return;
		}

		// ���ʵ�����ж����ĵ�
		else{
			mChatService.write(ctrlVal);
			// �����ַ�������������������ı��༭�ֶ�
			//mOutEditText.setText(mOutStringBuffer);
			//mOutEditText2.setText(mOutStringBuffer);

		}
		// }else if(message.length()<=0){
		// Toast.makeText(BluetoothChat.this, "�����ѶϿ�",
		// Toast.LENGTH_LONG).show();
		// // �û�δ����������������
		// mChatService = new BluetoothChatService(this, mHandler);
		// Intent serverIntent = new Intent(BluetoothChat.this,
		// DeviceListActivity.class);
		// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		// }
	}
	/**
	 * ����һ����Ϣ
	 * 
	 * @param message
	 *            һ���ı��ַ�������.
	 */
	private void sendMessage(String message) {
		// �������ʵ�������κ�����
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// ���ʵ�����ж����ĵ�
		if (message.length() > 0) {
			// �õ���Ϣ�ֽں͸���bluetoothchatserviceд
			byte[] send = message.getBytes();
			mChatService.write(send);
			// �����ַ�������������������ı��༭�ֶ�
			//mOutEditText.setText(mOutStringBuffer);
			//mOutEditText2.setText(mOutStringBuffer);

		}
		// }else if(message.length()<=0){
		// Toast.makeText(BluetoothChat.this, "�����ѶϿ�",
		// Toast.LENGTH_LONG).show();
		// // �û�δ����������������
		// mChatService = new BluetoothChatService(this, mHandler);
		// Intent serverIntent = new Intent(BluetoothChat.this,
		// DeviceListActivity.class);
		// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		// }
	}

	// �ж��������ߵı༭��ؼ������س���
	@SuppressLint("NewApi")
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		@TargetApi(Build.VERSION_CODES.CUPCAKE)
		@SuppressLint("NewApi")
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {

			// ����ж���һ���ؼ��ж��¼��ķ��ؼ���������Ϣ
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				if (view.getId() == R.id.edit_text_out2) {
					String tmp = view.getText().toString();
					String d;
					for(int i=0;i<tmp.length();i++){
						d=tmp.charAt(i)+"";
						if(i%2!=0){
							d+=" ";
						}
						
							sendMessage("\n"+ d);
						
					}
                
					
				}

			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	private int convertB2I(byte val){
		int result = 0;
		result=val&0x0ff;
		return result;
	}
	private byte convertI2B(int intValue){
		byte byteValue=0;
		int temp = intValue %256;
		if(intValue<0){
			byteValue = (byte)(temp<-128?256+temp:temp);
		}
		else{
			byteValue = (byte)(temp>127?temp-256:temp);
		}
		
		return byteValue;
	}
	// ������򣬻�ȡ��Ϣ��bluetoothchatservice����
	private final Handler mHandler = new Handler() {
		

		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mInputEditText.setText("");
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// ����һ���ַ���������
				String writeMessage = new String(writeBuf);
				sum=1;
				UTF=1;
				mmsg += writeMessage;
				if (checkBox_sixteen.isChecked()) {
					newCode = CodeFormat.Stringspace("\n<--"+writeMessage+"\n");
					
					mInputEditText.getText().append(newCode);
                    fmsg+="\n<--"+newCode+"\n";
				} else {

					mInputEditText.getText().append("\n<--"+writeMessage+"\n");
                    fmsg+="\n<--"+writeMessage+"\n";
				}

				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// ����һ���ַ�������Ч�ֽڵĻ�����
				if(readBuf[0]==0x71){
					Toast.makeText(BluetoothChat.this,"����ɹ���",Toast.LENGTH_SHORT).show();		
				}
				/*
				switch(readBuf[0]){
				case 0x01:
					Toast.makeText(BluetoothChat.this,"��ȡ1�ɹ���",Toast.LENGTH_SHORT).show();
					rowP.setInputType(InputType.TYPE_CLASS_TEXT);
					rowP.setText(Integer.toString((convertB2I(readBuf[1])<<8)+convertB2I(readBuf[2])));
					rowP.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				case 0x02:
					
					break;
				case 0x03:
					Toast.makeText(BluetoothChat.this,"��ȡ3�ɹ���",Toast.LENGTH_SHORT).show();
					rowD.setInputType(InputType.TYPE_CLASS_TEXT);
					rowD.setText(Integer.toString((convertB2I(readBuf[1])<<8)+convertB2I(readBuf[2])));
					rowD.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				case 0x04:
					Toast.makeText(BluetoothChat.this,"��ȡ4�ɹ���",Toast.LENGTH_SHORT).show();
					pitP.setInputType(InputType.TYPE_CLASS_TEXT);
					pitP.setText(Integer.toString((convertB2I(readBuf[1])<<8)+convertB2I(readBuf[2])));
					pitP.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				case 0x05:
					
					break;
				case 0x06:
					Toast.makeText(BluetoothChat.this,"��ȡ6�ɹ���",Toast.LENGTH_SHORT).show();
					pitD.setInputType(InputType.TYPE_CLASS_TEXT);
					pitD.setText(Integer.toString((convertB2I(readBuf[1])<<8)+convertB2I(readBuf[2])));
					pitD.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				case 0x07:
					Toast.makeText(BluetoothChat.this,"��ȡ7�ɹ���",Toast.LENGTH_SHORT).show();
					yawP.setInputType(InputType.TYPE_CLASS_TEXT);
					yawP.setText(Integer.toString((convertB2I(readBuf[1])<<8)+convertB2I(readBuf[2])));
					yawP.setInputType(InputType.TYPE_CLASS_NUMBER);break;
				case 0x08:break;
				case 0x09:
					Toast.makeText(BluetoothChat.this,"��ȡ9�ɹ���",Toast.LENGTH_SHORT).show();
					yawD.setInputType(InputType.TYPE_CLASS_TEXT);
					yawD.setText(Integer.toString((convertB2I(readBuf[1])<<8)+convertB2I(readBuf[2])));
					yawD.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				}*/
				if(readBuf[0]==0x70){
					
					Toast.makeText(BluetoothChat.this,"��ȡ0x70�ɹ���",Toast.LENGTH_SHORT).show();	
				
				}
				if (sum==1) {
					mInputEditText.getText().append(Html.fromHtml("<font color=\"blue\">"+"\n-->\n"+"</font>"));
					fmsg+="\n-->\n";
					sum++;
				}else {
					sum++;
				}
				String readMessage = new String(readBuf, 0, msg.arg1);
			/*	if (checkBox_sixteen.isChecked()) {
					if (UTF==1) {
						newCode2 = CodeFormat.bytesToHexStringTwo(readBuf, 7);
						mInputEditText.getText().append(Html.fromHtml("<font color=\"blue\">"+CodeFormat.Stringspace(newCode2)+"</font>"));
						fmsg+=Html.fromHtml("<font color=\"blue\">"+CodeFormat.bytesToHexStringTwo(readBuf, 7)+"</font>");
						UTF++;
					}else {
						UTF++;
					}
				} else*/
				{
				
                    mInputEditText.getText().append(Html.fromHtml("<font color=\"blue\">"+readMessage+"</font>"));
                    fmsg+=Html.fromHtml("<font color=\"blue\">"+readMessage+"</font>");
				}

				break;
			case MESSAGE_DEVICE_NAME:
				// ���������װ�õ�����
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"������ " + mConnectedDeviceName, Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// ��Ĭ���ַ���������ַ�����
			byte[] bs = str.getBytes();
			// ���µ��ַ����������ַ���
			return new String(bs, newCharset);
		}
		return null;
	}

	/**
	 * ���ַ�����ת����UTF-8��
	 */
	public String toUTF_8(String str) throws UnsupportedEncodingException {
		return this.changeCharset(str, "UTF_8");
	}

	@SuppressLint("NewApi")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// ��devicelistactivity��������װ��
			if (resultCode == Activity.RESULT_OK) {
				// ����豸��ַ
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �������豸����
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// ��ͼ���ӵ�װ��
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// ������������������
			if (resultCode == Activity.RESULT_OK) {
				// ���������ã����Խ���һ������Ự
				setupChat();
			} else {
				// �û�δ����������������
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// �û�δ����������������
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// ȷ�����豸�Ƿ��ֱ���
			ensureDiscoverable();
			return true;

		case R.id.setup:
			new AlertDialog.Builder(this)
					.setTitle("���ÿ�ѡ����")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setSingleChoiceItems(new String[] { "ʮ������", "�ַ���" }, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									if (dialog.equals("ʮ������")) {
										Log.d(TAG, "ʮ������");
										dialogs = true;
									} else {
										dialogs = false;
										Log.d(TAG, "�ַ���");
									}
									dialog.dismiss();
								}
							}).setNegativeButton("ȡ��", null).show();
			     return true;

		case R.id.clenr:
			finish();
			return true;
		}
		return false;
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)  {  
		
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
		      }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@SuppressLint("NewApi")
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
				if (event.sensor == null) {  
		            return;  
		        }  
		  
		        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		        	
		            float x = (float) event.values[0];  
		            float y = (float) event.values[1];  
		            float z = (float) event.values[2];  
		            mCalendar = Calendar.getInstance();  
		            long stamp = mCalendar.getTimeInMillis() / 1000l;// 1393844912  
		          
		            int second = mCalendar.get(Calendar.SECOND);// 53  
		            rc_roll.setText("����:\n"+(int)(-x*10)+"��");
					rc_pit.setText("���:\n"+(int)(-y*10)+"��");
		            float px = Math.abs(mX - x);  
		            float py = Math.abs(mY - y);  
		            float pz = Math.abs(mZ - z);  
		            Log.d(TAG, "pX:" + px + "  pY:" + py + "  pZ:" + pz + "    stamp:"  
		                    + stamp + "  second:" + second);  
		            float maxvalue = getMaxValue(px, py, pz);  
		            if (maxvalue > 2 && (stamp - lasttimestamp) > 30) {  
		                lasttimestamp = stamp;  
		                Log.d(TAG, " sensor isMoveorchanged....");  
		                 
		            }  
		  
		            mX = x;  
		            mY = y;  
		            mZ = z;  
		       /*     if(x>=8)x=8;if(x<=-8)x=-8;//�ֻ�������Ϊ+-9
		            if(y>=8)y=8;if(y<=-8)y=-8;
		            if(y<=2&&y>=-2)y=0;
		            else if(y>2)y-=2;
		            else if(y<-2)y+=2;e
					if(x<=2&&x>=-2)x=0;//���м�+-3�����ơ�
				    else if(x>2)x-=2;
			        else if(x<-2)x+=2;
			     */
		            if(connect==1){
		            roll=(int)(y*10);
		            if(roll>50)roll=50;
		            else if(roll<-50)roll=-50;
		            pitch=(int)(-x*10);
		            if(pitch>50)pitch=50;
		            else if(pitch<-50)pitch=-50;
		            roll+=50;
		            pitch+=50;
		            getCtr[0]=(byte) 0xAA;
					getCtr[1]=(byte) 0xBB;
					getCtr[2]=(byte) ((byte)(THROTTLE&0xff));
					getCtr[3]=(byte) ((byte)(THROTTLE>>8));
					
					getCtr[4]=(byte)(int)pitch;//a*5=50��a=10
					getCtr[5]=(byte)(int)roll;
					getCtr[6]=(byte) (50);
					for(int j=7;j<30;j++)getCtr[j]=0;
					getCtr[31]=(byte) 0x01;
					sendHEX(getCtr);
		            }
		        }  
			}   
			 /** 
		     * ��ȡһ�����ֵ 
		     *  
		     * @param px 
		     * @param py 
		     * @param pz 
		     * @return 
		     */  
		    public float getMaxValue(float px, float py, float pz) {  
		        float max = 0;  
		        if (px > py && px > pz) {  
		            max = px;  
		        } else if (py > px && py > pz) {  
		            max = py;  
		        } else if (pz > px && pz > py) {  
		            max = pz;  
		        }  
		  
		        return max;  
		    }  
		
}