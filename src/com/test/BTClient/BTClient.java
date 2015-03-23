/* Project: App Controller for copter 4(including Crazepony)
 * Author: 	Huang Yong xiang 
 * Brief:	This is an open source project under the terms of the GNU General Public License v3.0
 * TOBE FIXED:  1. disconnect and connect fail with Bluetooth due to running thread 
 * 				2. Stick controller should be drawn in dpi instead of pixel.  
 * 
 * */

package com.test.BTClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.test.BTClient.DeviceListActivity; 
import com.test.BTClient.MySurfaceView;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
//import android.view.Menu;            //��ʹ�ò˵����������
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
 
import com.test.BTClient.*;

@SuppressLint("NewApi")
public class BTClient extends Activity {

	private final static int REQUEST_CONNECT_DEVICE = 1; // �궨���ѯ�豸���

	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP����UUID��
	//
	private final static int UPDATE_MUV_STATE_PERIOD=200;
	
	
	List<WayPoint> wpRoute;	//�滮·��
//	private WayPoint[] wpArr=new WayPoint[3]; 
	
	//
	private InputStream is; // ������������������������
	//private TextView text0; //��ʾ������
	private EditText edit0; // ��������������
	private TextView dis; // ����������ʾ��
	private ScrollView sv; // ��ҳ�����������
	private String smsg = ""; // ��ʾ�����ݻ���
	private String fmsg = ""; // ���������ݻ���
	 
	private TextView throttleText,yawText,pitchText,rollText;
	private TextView pitchAngText,rollAngText,yawAngText,altText,GPSFixText,homeFixText,distanceText,voltageText;
	private Button armButton,lauchLandButton,headFreeButton,altHoldButton,posHoldButton,accCaliButton;
	private Spinner wpSpinner;
	private ArrayAdapter<String> adapter;
//	private WayPoint wp1=new WayPoint("�̵�", 45443993,126373228); 
 	private Navigation nav;
 	private boolean navFirstStart=false;
	//private newButtonView newButton;

	public String filename = ""; // ��������洢���ļ���
	BluetoothDevice _device = null; // �����豸
	BluetoothSocket _socket = null; // ����ͨ��socket
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;
	//
	boolean lauchFlag=false;
	long timeNew, timePre=0;
	//
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter(); // ��ȡ�����������������������豸

	private MySurfaceView stickView; 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); // ���û���Ϊ������ main.xml 
		//��ʾ text
		throttleText = (TextView)findViewById(R.id.throttleText); // 
		yawText = (TextView)findViewById(R.id.yawText);
		pitchText = (TextView)findViewById(R.id.pitchText);
		rollText = (TextView)findViewById(R.id.rollText);
		//pitchAngText,rollAngText,yawAngText,altText,GPSFixText,homeFixText,voltageText
		pitchAngText = (TextView)findViewById(R.id.pitchAngText);
		rollAngText = (TextView)findViewById(R.id.rollAngText);
		yawAngText = (TextView)findViewById(R.id.yawAngText);
		altText = (TextView)findViewById(R.id.altText);
		GPSFixText = (TextView)findViewById(R.id.GPSFixText);
		homeFixText = (TextView)findViewById(R.id.homeFixText);
		voltageText = (TextView)findViewById(R.id.voltageText);
		distanceText= (TextView)findViewById(R.id.distanceText);
		 
		edit0 = (EditText) findViewById(R.id.Edit0); // �õ��������
		sv = (ScrollView) findViewById(R.id.ScrollView01); // �õ���ҳ���
		dis = (TextView) findViewById(R.id.in); // �õ�������ʾ���
		//ҡ��
		stickView=(MySurfaceView)findViewById(R.id.stickView);
		//��ť
		armButton=(Button)findViewById(R.id.armButton);
		lauchLandButton=(Button)findViewById(R.id.lauchLandButton);
		headFreeButton=(Button)findViewById(R.id.headFreeButton);
		altHoldButton=(Button)findViewById(R.id.altHoldButton);
		posHoldButton=(Button)findViewById(R.id.posHoldButton);
		accCaliButton=(Button)findViewById(R.id.accCaliButton);
		//GPS ����
	 	nav=new Navigation(); 
		//�����б�
		wpSpinner=(Spinner)findViewById(R.id.wayPointSpinner);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,nav.wpStr);//����Spinner��Ӧ��Adapter
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//Adapter���
		wpSpinner.setAdapter(adapter);//����
		wpSpinner.setOnItemSelectedListener(new wpSpinnerSelectedListener());
		
	//	wp1.setWP("AA", 2, 3);
		//ReadThread.start(); 
		//Log.v("run",(stickView.touchReadyToSend?"TURE":"FALSE")); 
		// ����򿪱��������豸���ɹ�����ʾ��Ϣ����������
		if (_bluetooth == null) {
			Toast.makeText(this, "�޷����ֻ���������ȷ���ֻ��Ƿ����������ܣ�", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		} 
		// �����豸���Ա�����
		new Thread() {
			public void run() {
				if (_bluetooth.isEnabled() == false) {
					_bluetooth.enable();
				}
			}
		}.start();
	}
	@Override
	public void onPause()
	{
		navFirstStart=false;
		super.onPause(); 
	}
	@Override
	public void onStop()
	{
		navFirstStart=false;
		super.onStop(); 
	}
	// Spinner Listener   ע�⣺App��ʼ����ʱ��Item�ᱻ ѡ������˻���ν��뵽�˷��� 
	class wpSpinnerSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0,View arg1,int arg2,long arg3){
	 	//	System.out.println(nav.findWayPoint("�̵�").getLat());
			String selectedPlace=arg0.getSelectedItem().toString();
			WayPoint selectedWayPoint=nav.wpSave.get(arg2); 
			System.out.println(selectedPlace + Integer.toString(arg2));	//����¥ 2
		   System.out.println(selectedWayPoint.latitude + ","+ selectedWayPoint.longtitude);
		   Protocol.nextWp=selectedWayPoint;
		   if(navFirstStart==false)	//���˳�ʼ������ItemSeleted
			   navFirstStart=true;
		   else
			   btSendBytes(Protocol.getSendData(Protocol.MSP_SET_1WP, Protocol.getCommandData(Protocol.MSP_SET_1WP)));
			//System.out.println(Integer.toString(nav.findWayPoint(selectedPlace).getLat()));
			//	Log.d("TAG",arg0.getSelectedItem().toString()+ Integer.toString(arg2));
		//	Log.d("TAG",Integer.toString(nav.findWayPoint(selectedPlace).getLat()));
			}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub 
		} 
	}
	//�ص�Ѱ�� 																							���ǲ�����ô�໨������ðɣ�Ҫ�ǻ� ��Ҳ�������ڵ��ң�һֱ��������֪����ǿ��ǿ����ø������㣬������Χ���е��ˣ�������뱣�����˷����������������·��ɶ���ﲻ�������쵰���ǣ�֮�����ھ��ƺ���������Ϊ���Լ����������е�������������ͬ�顣
	
	//**----------------------------Button:Send-------------------*//
	// ���Ͱ�����Ӧ
	public void onSendButtonClicked(View v) {
		int i = 0;
		int n = 0;
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")//���Ѿ�������ʱ�ŷ���
		{
			try {
				OutputStream os = _socket.getOutputStream(); // �������������
				byte[] bos = edit0.getText().toString().getBytes();
				/**--------------���з���ת��----------*/
				for (i = 0; i < bos.length; i++) {
					if (bos[i] == 0x0a)
						n++;
				}
				byte[] bos_new = new byte[bos.length + n];
				n = 0;
				for (i = 0; i < bos.length; i++) { // �ֻ��л���Ϊ0a,�����Ϊ0d 0a���ٷ���
					if (bos[i] == 0x0a) {
						bos_new[n] = 0x0d;
						n++;
						bos_new[n] = 0x0a;
					} else {
						bos_new[n] = bos[i];
					}
					n++;
				} 
				os.write(bos_new);
			} 
			catch (IOException e) {
				Toast.makeText(this, "����ʧ��", Toast.LENGTH_SHORT).show();
			} 
			catch (Exception e){
				Toast.makeText(this, "����ʧ��", Toast.LENGTH_SHORT).show();
			}
		}
		else
			Toast.makeText(this, "δ�����豸���޷�����", Toast.LENGTH_SHORT).show();
	}
	//----------------Button sendAb for test---------//
	public void onSendArmButtonClicked(View v)
	{
		 
//		Protocol.throttle=1000;
//		btSendBytes(Protocol.getSendData(Protocol.SET_THROTTLE, Protocol.getCommandData(Protocol.SET_THROTTLE)));
		//btSendBytes(Protocol.getSendData(Protocol.ARM_IT, Protocol.getCommandData(Protocol.ARM_IT)));
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")
		{
			if(armButton.getText()!="����")
			{ 
				btSendBytes(Protocol.getSendData(Protocol.ARM_IT, Protocol.getCommandData(Protocol.ARM_IT)));
				armButton.setText("����");
			}
			else
			{
				btSendBytes(Protocol.getSendData(Protocol.DISARM_IT, Protocol.getCommandData(Protocol.DISARM_IT)));
				armButton.setText("����");
			}
		}
		else
			Toast.makeText(this, "δ�����豸", Toast.LENGTH_SHORT).show(); 
	} 
	
	//Take off , land down
	public void onlauchLandButtonClicked(View v)
	{
		if(lauchLandButton.getText()!="����")
		{
			btSendBytes(Protocol.getSendData(Protocol.LAUCH, Protocol.getCommandData(Protocol.LAUCH)));
			lauchLandButton.setText("����"); 
			Protocol.throttle=Protocol.LAUCH_THROTTLE;
			stickView.SmallRockerCircleY=stickView.rc2StickPosY(Protocol.throttle);
		//	btSendBytes(Protocol.getSendData(Protocol.SET_THROTTLE, Protocol.getCommandData(Protocol.SET_THROTTLE)));
			stickView.touchReadyToSend=true;
		}
		else
		{
			btSendBytes(Protocol.getSendData(Protocol.LAND_DOWN, Protocol.getCommandData(Protocol.LAND_DOWN)));
			lauchLandButton.setText("���");
			Protocol.throttle=Protocol.LAND_THROTTLE;
			stickView.SmallRockerCircleY=stickView.rc2StickPosY(Protocol.throttle);
		//	btSendBytes(Protocol.getSendData(Protocol.SET_THROTTLE, Protocol.getCommandData(Protocol.SET_THROTTLE))); 
			stickView.touchReadyToSend=true;
		}
	}
	//��ͷģʽ��
	public void onheadFreeButtonClicked(View v)
	{
		//bool modeOn;
		
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")
		{ 
			if(headFreeButton.getCurrentTextColor()!=Color.GREEN)
			{	btSendBytes(Protocol.getSendData(Protocol.HEAD_FREE, Protocol.getCommandData(Protocol.HEAD_FREE)));
			//	headFreeButton.setText("HeadStrict");
				//headFreeButton.setHighlightColor(Color.GREEN); 
				//headFreeButton.setBackgroundColor(Color.GREEN);
				headFreeButton.setTextColor(Color.GREEN);
			}
			else
			{
				btSendBytes(Protocol.getSendData(Protocol.STOP_HEAD_FREE, Protocol.getCommandData(Protocol.STOP_HEAD_FREE)));
			//	headFreeButton.setText("��ͷ");
			//	headFreeButton.setBackgroundColor(Color.BLACK);
				headFreeButton.setTextColor(Color.WHITE);
			}
		}
		else
			Toast.makeText(this, "δ�����豸", Toast.LENGTH_SHORT).show();
		
	}
	//����
	public void onposHoldButtonClicked(View v)
	{
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")
		{
			if(posHoldButton.getCurrentTextColor()!=Color.GREEN )
			{	// ���� �� 
				btSendBytes(Protocol.getSendData(Protocol.POS_HOLD, Protocol.getCommandData(Protocol.POS_HOLD))); 
				posHoldButton.setTextColor(Color.GREEN); 
			} 
			else	// ȡ��
			{ 
				btSendBytes(Protocol.getSendData(Protocol.STOP_POS_HOLD, Protocol.getCommandData(Protocol.STOP_POS_HOLD)));
				posHoldButton.setTextColor(Color.WHITE);  
			}
		}
		else
			Toast.makeText(this, "δ�����豸", Toast.LENGTH_SHORT).show(); 
	}
	//���߼�
	public void onaltHoldButtonClicked(View v)
	{
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")
		{
			if( altHoldButton.getCurrentTextColor()!=Color.GREEN )
			{	//���߶��㶼��
				btSendBytes(Protocol.getSendData(Protocol.HOLD_ALT, Protocol.getCommandData(Protocol.HOLD_ALT))); 
			//	altPosHoldButton.setText("������");
				altHoldButton.setTextColor(Color.GREEN);
			//	stickView.SmallRockerCircleY=150;		//tobe fixed!! use back middle function instead
				stickView.altCtrlMode=1;	
			} 
			else	//��ȡ��
			{ 
				btSendBytes(Protocol.getSendData(Protocol.STOP_HOLD_ALT, Protocol.getCommandData(Protocol.STOP_HOLD_ALT)));
				altHoldButton.setTextColor(Color.WHITE); 
			//	stickView.stickHomeY1=-1;
				stickView.altCtrlMode=0;
			}
		}
		else
			Toast.makeText(this, "δ�����豸", Toast.LENGTH_SHORT).show(); 
	}
	
	//У׼
	public void onAccCaliButtonClicked(View v)
	{
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")
		{
			btSendBytes(Protocol.getSendData(Protocol.MSP_ACC_CALIBRATION, Protocol.getCommandData(Protocol.MSP_ACC_CALIBRATION)));
		}
		else
			Toast.makeText(this, "δ�����豸", Toast.LENGTH_SHORT).show(); 
	}
	public void btSendBytes(byte[] data)
	{
		Button btnConnect=(Button) findViewById(R.id.Button03);
		if(btnConnect.getText()=="�Ͽ�")//���Ѿ�������ʱ�ŷ���
		{
			try {
				OutputStream os = _socket.getOutputStream(); // �������������
				//byte[] bos = edit0.getText().toString().getBytes();
				//byte[] bos={0x0a,0x0d};
				os.write(data);
			} 
			catch (IOException e) {
			//	Toast.makeText(this, "����ʧ��", Toast.LENGTH_SHORT).show();
			} 
			catch (Exception e){
			//	Toast.makeText(this, "����ʧ��", Toast.LENGTH_SHORT).show();
			}
		}
		 else
			 Toast.makeText(this, "δ�����豸���޷�����", Toast.LENGTH_SHORT).show();
	}
	
	// ���ջ�������ӦstartActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // ���ӽ������DeviceListActivity���÷���
			Log.v("run","ActRes");
			// ��Ӧ���ؽ��
			if (resultCode == Activity.RESULT_OK) 
			{ // ���ӳɹ�����DeviceListActivity���÷���
				// MAC��ַ����DeviceListActivity���÷���
				Log.v("run","ActRes2");
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ������豸���
				_device = _bluetooth.getRemoteDevice(address);

				// �÷���ŵõ�socket
				try {
					_socket = _device.createRfcommSocketToServiceRecord(UUID
							.fromString(MY_UUID));
				} catch (IOException e) {
					Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
				}
				// ����socket
				Button btn = (Button) findViewById(R.id.Button03);
				try {
					/**---------------successfully Connected */
					_socket.connect();
					Toast.makeText(this, "����" + _device.getName() + "�ɹ���",
							Toast.LENGTH_SHORT).show();
					btn.setText("�Ͽ�");
				} catch (IOException e) {
					try {
						Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
						_socket.close();
						_socket = null;
					} catch (IOException ee) {
						Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT)
								.show();
					}

					return;
				} 
				// �򿪽����߳�
				try {
					is = _socket.getInputStream(); // �õ���������������
				} catch (IOException e) {
					Toast.makeText(this, "��������ʧ�ܣ�", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (bThread == false) {
					Log.v("run","ThreadStart");
					ReadThread.start();
					bThread = true;
					
				} else {
					bRun = true;
				}
			}
			break;
		default:
			break;
		}
	}
	
	// ���������̣߳���Android���Ҫֱ����Thread�����UI��Ҫ����UI��Ӧ�ý�����Ϣ���У�
	Thread ReadThread = new Thread() {
		
		public void run() { 
			int num = 0,totNum=0;
			long time1=0,time2=0; 
			byte[] buffer_new = new byte[1024];
			byte[] buffer = new byte[1024]; 
			List<Byte> totBuffer=new LinkedList<Byte>();
			int reCmd=-2;
			int i = 0;
			int n = 0;
			bRun = true;
			// �����߳�
			while (true) {
				try {
					while (is.available() == 0) {//wait for BT rec Data
						while (bRun == false) { 
						//	Log.v("run","bRunFalse");
						} 
					//	Log.v("run","runing1");
						//ң�ط��ͷ�֧�߳�
						if(stickView.touchReadyToSend==true)// process stick movement
						{
							
							btSendBytes(Protocol.getSendData(Protocol.SET_4CON, Protocol.getCommandData(Protocol.SET_4CON))); 
//								System.out.println("Thro: " + a+"," +Protocol.outputData[0]+ ","+ Protocol.outputData[3] +","+ Protocol.outputData[4]);
					
							Message msg=handler.obtainMessage();
							msg.arg1=2;
							handler.sendMessage(msg);
							stickView.touchReadyToSend=false;
						} 
						
						timeNew=SystemClock.uptimeMillis();	//ϵͳ���е��˵�ʱ��
						if(timeNew-timePre>UPDATE_MUV_STATE_PERIOD)
						{
							timePre=timeNew;
						 	btSendBytes(Protocol.getSendData(Protocol.FLY_STATE, Protocol.getCommandData(Protocol.FLY_STATE))); 

						}
					}
					//�����ݹ���
					time1=time2=SystemClock.uptimeMillis();
					boolean frameRec=false;
					while (!frameRec) {
						num = is.read(buffer); // �������ݣ�������buffer��num���ֽ� 
					 	n = 0; 
					 	
					 	String s0 = new String(buffer, 0, num);
						fmsg += s0; // �����յ�����
						for (i = 0; i < num; i++) {
							if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
								buffer_new[n] = 0x0a;
								i++;
							} else {
								buffer_new[n] = buffer[i];
							}
							n++;
						}
						String s = new String(buffer_new, 0, n);
						smsg += s; // д����ջ��� 
						reCmd=Protocol.processDataIn( buffer,num);
					/*	for(int j=0;j<num;j++)
							totBuffer.add(buffer[j]);
					 	totNum+=num; 
						while(is.available()==0 && !frameRec)//wait for more data in 5ms
						{ 
							time1=SystemClock.uptimeMillis();
							if(time1-time2>30)	//5ms��� ����Ϊ���� 
								frameRec=true;  
						}
						time2=time1;  */
						   if (is.available() == 0)	 
							   frameRec=true; // ��ʱ��û�����ݲ�����������ʾ
					} 

					totNum=0;
					if(reCmd>=0)
					{
						Message msg=handler.obtainMessage();
						msg.arg1=reCmd;//
						handler.sendMessage(msg); 
					}
					// ��Ϣ��ӵ�handler����Ϣ���У�handler�ӵ��󣬽��в�������ʾˢ�µ� 
//					Message msg=handler.obtainMessage();
//					msg.arg1=3;//
//					handler.sendMessage(msg);
				} catch (IOException e) {
				}
			}
		}
	};
	//------------------Handler---------------------//
	//����һ����Ϣ������У���Ҫ�������߳�֪ͨ���ˣ��� ˢ��UI
	Handler handler = new Handler() {
		//��ʾ���յ�������
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.arg1==1)
			{
				dis.setText(smsg); 
				sv.scrollTo(0, dis.getMeasuredHeight()); // �����������һҳ
			}
			else if(msg.arg1==2)
			{
				throttleText.setText("Throttle:"+Integer.toString(Protocol.throttle));
				yawText.setText("Yaw:"+Integer.toString(Protocol.yaw));
				pitchText.setText("Pitch:"+Integer.toString(Protocol.pitch));
				rollText.setText("Roll:"+Integer.toString(Protocol.roll));
			}
			else if(msg.arg1==3)//����״̬
			{
		//		 throttleText.setText(throttleText.getText()+"1");
			//	byte temp[]={1,2};
	
			}
			else if(msg.arg1==Protocol.FLY_STATE)
			{
			//	throttleText.setText(throttleText.getText()+"1");
			 	pitchAngText.setText("Pitch Ang: "+Protocol.pitchAng);
			 	rollAngText.setText("Roll Ang: "+Protocol.rollAng);
			 	yawAngText.setText("Yaw Ang: "+Protocol.yawAng);
			 	altText.setText("Alt:"+Protocol.alt + "m");
			 	if(Protocol.GPSFix!=0) 
			 	{
			 		GPSFixText.setTextColor(Color.GREEN); 
			 		GPSFixText.setText("GPS Fixed:"+Protocol.staNum);
			 	}
			 	else  
			 	{
			 		GPSFixText.setTextColor(Color.RED);
			 		GPSFixText.setText("GPS Not Fix:"+Protocol.staNum);
			 	}
			 	if(Protocol.GPSFixHome!=0)
			 	{
			 		homeFixText.setTextColor(Color.GREEN); 
			 		homeFixText.setText("Home Not Fix");
			 	}
			 	else
			 	{
			 		homeFixText.setTextColor(Color.RED); 
			 		homeFixText.setText("Home Fixed");
			 	}
			 	voltageText.setText("Voltage:"+Protocol.voltage + " V");
			 	distanceText.setText("speedZ:"+Protocol.speedZ + "m/s");
			}
		}
	};

	// �رճ�����ô�����
	public void onDestroy() {
		super.onDestroy();
		if (_socket != null) // �ر�����socket
			try {
				_socket.close();
			} catch (IOException e) {
			}
		  _bluetooth.disable(); //�ر���������
	}

	// �˵�������
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {//�����˵�
	 * MenuInflater inflater = getMenuInflater();
	 * inflater.inflate(R.menu.option_menu, menu); return true; }
	 */

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { //�˵���Ӧ����
	 * switch (item.getItemId()) { case R.id.scan:
	 * if(_bluetooth.isEnabled()==false){ Toast.makeText(this, "Open BT......",
	 * Toast.LENGTH_LONG).show(); return true; } // Launch the
	 * DeviceListActivity to see devices and do scan Intent serverIntent = new
	 * Intent(this, DeviceListActivity.class);
	 * startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); return
	 * true; case R.id.quit: finish(); return true; case R.id.clear: smsg="";
	 * ls.setText(smsg); return true; case R.id.save: Save(); return true; }
	 * return false; }
	 */ 
	//------------------------------------Button:Connect----------------------------//
	// ���Ӱ�����Ӧ����
	public void onConnectButtonClicked(View v) {
		if (_bluetooth.isEnabled() == false) { // ����������񲻿�������ʾ
			Toast.makeText(this, " ��������...", Toast.LENGTH_LONG).show();
			return;
		}
		// ��δ�����豸���DeviceListActivity�����豸����
		Button btn = (Button) findViewById(R.id.Button03);
		if (_socket == null) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // ��ת��������
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
		} else {	//�������ϣ��Ͽ�
			// �ر�����socket
			try {
				is.close();
				_socket.close();
				_socket = null;
				bRun = false;
				btn.setText("����");
				
				//ReadThread.stop();//------added
			} catch (IOException e) {
			}
		}
		return;
	}

	// ���水����Ӧ����
	public void onSaveButtonClicked(View v) {
		Save();
	}

	// ���������Ӧ����
	public void onClearButtonClicked(View v) {
		smsg = "";
		fmsg = "";
		dis.setText(smsg);
		return;
	}

	// �˳�������Ӧ����
	public void onQuitButtonClicked(View v) {
		finish();
	}

	// ���湦��ʵ��
	private void Save() {
		// ��ʾ�Ի��������ļ���
		LayoutInflater factory = LayoutInflater.from(BTClient.this); // ͼ��ģ�����������
		final View DialogView = factory.inflate(R.layout.sname, null); // ��sname.xmlģ��������ͼģ��
		new AlertDialog.Builder(BTClient.this).setTitle("�ļ���")
				.setView(DialogView) // ������ͼģ��
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() // ȷ��������Ӧ����
						{
							public void onClick(DialogInterface dialog,
									int whichButton) {
								EditText text1 = (EditText) DialogView
										.findViewById(R.id.sname); // �õ��ļ����������
								filename = text1.getText().toString(); // �õ��ļ���

								try {
									if (Environment.getExternalStorageState()
											.equals(Environment.MEDIA_MOUNTED)) { // ���SD����׼����

										filename = filename + ".txt"; // ���ļ���ĩβ����.txt
										File sdCardDir = Environment
												.getExternalStorageDirectory(); // �õ�SD����Ŀ¼
										File BuildDir = new File(sdCardDir,
												"/data"); // ��dataĿ¼���粻����������
										if (BuildDir.exists() == false)
											BuildDir.mkdirs();
										File saveFile = new File(BuildDir,
												filename); // �½��ļ���������Ѵ������½��ĵ�
										FileOutputStream stream = new FileOutputStream(
												saveFile); // ���ļ�������
										stream.write(fmsg.getBytes());
										stream.close();
										Toast.makeText(BTClient.this, "�洢�ɹ���",
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(BTClient.this, "û�д洢����",
												Toast.LENGTH_LONG).show();
									}

								} catch (IOException e) {
									return;
								}

							}
						}).setNegativeButton("ȡ��", // ȡ��������Ӧ����,ֱ���˳��Ի������κδ���
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show(); // ��ʾ�Ի���
	}
	
	
}