package com.example.smartlink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Android Socket 发送广播包
 * 
 * @author Andy
 *
 */
public class SendBroadcastActivity extends Activity {

	private Button sendB, recB;
	private TextView tv1, tv2;
	private String result;
	private static DatagramSocket socket;
	private static InetAddress addr;
	private boolean isReceiving = true;
	private boolean isSending = true;
	private int count = 120;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sendbroadcast);

		try {
			// 建立套接字，参数端口号不填写，系统会自动分配一个可用端口
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		sendB = (Button) findViewById(R.id.sendB);
		recB = (Button) findViewById(R.id.recB);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);

		
		recB.setVisibility(View.INVISIBLE);
		tv1.setVisibility(View.INVISIBLE);
		tv2.setVisibility(View.INVISIBLE);
		recB.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							while (isReceiving) {
								receiveBroadcast();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}).start();

			}
		});

		sendB.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
//							sendBroadcast(getIDBytes("wangliyan", "zlkj_8888", "zlkj8888", "192.168.0.104"));
							while(isSending){
							Thread.sleep(5000);
							sendBroadcast(getIDBytes());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

			}
		});
		if (result != null) {
			tv2.setText(result);
		}
	}

	/**
	 * 按照协议要求，将用户id，ip，ssid，密码发送给IPC
	 * @param userID
	 * @param ssid
	 * @param password
	 * @param ip
	 * @return  
	 */
	public static byte[] getIDBytes() {
		String userID = ConstantValue.userid;
		String ssid = ConstantValue.ssid;
		String password = ConstantValue.password;
		String ip = ConstantValue.ip;
		// length=98 0x62 0x00
		String uStr = " {\"userID\" : \"" + userID + "\", \"SSID\" : \"" + ssid + "\", \"password\" : \"" + password
				+ "\", \"IP\" : \"" + ip + "\"}  ";

		Log.d("andy", uStr);
		// 说明：低位在前，表示Data域的长度，例如len = 10， 字节顺序为0x0a 0x00
		short dataLen = (short) uStr.length();

		dataLen = htons(dataLen);
		// String devIdString = Integer.toHexString(hInt);

		byte[] b = shortToByteArray(dataLen);

//		Log.d("andy", Integer.toHexString(dataLen));
//
//		for (int i = 0; i < b.length; i++) {
//			Log.d("andy", "b[" + i + "]=========" + b[i]);
//		}

		System.out.println("ddd" + b.toString());
		String headStr = "ZENO";
		String endStr = "zeno";

		byte[] head = new byte[4];
		head = headStr.getBytes();

		byte[] len = new byte[2];
		len[0] = b[0];
		len[1] = b[1];

		byte[] ctrl = new byte[1];
		ctrl[0] = 0X01;

		byte[] data = new byte[dataLen];
		data = uStr.getBytes();

		byte[] bytes = new byte[12 + data.length];

		System.arraycopy(head, 0, bytes, 0, 4);
		System.arraycopy(len, 0, bytes, 4, 2);
		System.arraycopy(ctrl, 0, bytes, 6, 1);
		System.arraycopy(data, 0, bytes, 7, data.length);

		byte[] crc = new byte[1];
		//crc校验 为Head、LEN、Ctrl、Data的每一位的校验和,约定为0XFF
		crc[0] = (byte)0XFF;

		byte[] end = new byte[4];
		end = endStr.getBytes();

		System.arraycopy(crc, 0, bytes, 7 + data.length, 1);
		System.arraycopy(end, 0, bytes, 8 + data.length, 4);

		return bytes;

	}

	public static short htons(short s) {
		short rslt = 0;
		byte[] bs1 = new byte[2];
		putShort(bs1, s, 0);
		byte[] bs2 = ReversEndian(bs1, 2, false);
		rslt = getShort(bs2, 0);
		return rslt;
	}

	/**
	 * 通过byte数组取到short
	 * 
	 * @param b
	 * @param index
	 *            第几位开始取
	 * @return
	 */
	public static short getShort(byte[] b, int index) {
		return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
	}

	/**
	 * 转换short为byte
	 * 
	 * @param b
	 * @param s
	 *            需要转换的short
	 * @param index
	 */
	public static void putShort(byte b[], short s, int index) {
		b[index + 1] = (byte) (s >> 8);
		b[index + 0] = (byte) (s >> 0);
	}

	private static byte[] shortToByteArray(short s) {
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			shortBuf[i] = (byte) ((s >>> offset) & 0xff);
		}
		return shortBuf;
	}

	// 网络字节逆序
	public static byte[] ReversEndian(byte b[], int count, boolean big) {
		byte by;
		byte data[] = new byte[count];
		for (int i = 0; i < count; i++) {
			data[i] = b[i];
		}
		if (big == false) {
			for (int i = 0; i < count; i++) {
				by = b[i];
				data[count - i - 1] = by;
			}
		}
		return data;
	}

	private void receiveBroadcast() {
		// 创建一个byte类型的数组，用于存放接收到得数据
		byte data[] = new byte[4 * 1024];
		// 创建一个DatagramPacket对象，并指定DatagramPacket对象的大小
		DatagramPacket packet = new DatagramPacket(data, data.length);
		// 读取接收到得数据
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 把客户端发送的数据转换为字符串。
		// 使用三个参数的String方法。参数一：数据包 参数二：起始位置 参数三：数据包长
		Log.i("andy", "ip" + socket.getInetAddress());
		result = new String(packet.getData(), packet.getOffset(), packet.getLength());

		if (result.length() > 0) {
			isReceiving = false;
		}
	}

	public static void sendBroadcast(byte[] data) {
		try {
			addr = InetAddress.getByName("255.255.255.255");
			// 创建报文，包括报文内容，内容长度，报文地址（这里全1地址即为广播），端口号（接受者需要使用该端口）
			final DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setAddress(addr);
			packet.setPort(8086);
			// 发送报文
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 判断一下当前Android手机是否处于Wi-Fi热点模式下 如果是 对应的广播地址是："192.168.43.255"
	 * 
	 * @param context
	 * @return
	 */
	protected static Boolean isWifiApEnabled(Context context) {
		try {
			WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			Method method = manager.getClass().getMethod("isWifiApEnabled");
			return (Boolean) method.invoke(manager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("resource")
	public String receive() throws IOException {

		Socket client = new Socket(addr, 8086);
		BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String txt = reader.readLine();

		return txt;
	}

	/**
	 * 更新UI
	 */
	@SuppressLint("HandlerLeak")
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 后台处理耗时操作
	 * 
	 * @author Andy
	 *
	 */
	class myThread implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {

				Message message = new Message();
				message.what = 1;

				SendBroadcastActivity.this.myHandler.sendMessage(message);

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
