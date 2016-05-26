package com.example.smartlink;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.hiflying.smartlink.ISmartLinker;
import com.hiflying.smartlink.OnSmartLinkListener;
import com.smartlink.SDK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

/**
 * SmartLinker发送组播
 * 
 * @author Andy
 *
 */

public class SnifferSmartLinker implements ISmartLinker {

	private static final String TAG = "SnifferSmartLinker";

	/**
	 * 发送组播的端口号
	 */
	private static int MULTICAST_PORT = 36666;

	private boolean isConnecting = false;

	private static SnifferSmartLinker snifferSmartLinker;

	private String ssid, password;

	private MulticastSocket multicastSocket;

	private String newIp;

	/**
	 * 数据校验值
	 */
	private int crc;

	public SnifferSmartLinker() {
		isConnecting = false;
	}

	public static SnifferSmartLinker getInstance() {
		if (snifferSmartLinker == null) {
			snifferSmartLinker = new SnifferSmartLinker();
		}
		return snifferSmartLinker;
	}

	/**
	 * 得到校验后的字节数组
	 * 
	 * @param ssid
	 * @param password
	 * @param newIp
	 *            ： ip mask gateway信息
	 * @return
	 */
	@SuppressLint("UseValueOf")
	private byte[] getMyBytes(String ssid, String password, String newIp) {

		byte[] bytes = new byte[22 + 12];
		String content = ssid + "\0" + password + "\0";

		Log.i(TAG, "content============" + content);
		byte[] head = new byte[4];
		head[0] = 0;
		head[1] = 1;
		head[2] = (byte) (4 + content.getBytes().length + 12);
		head[3] = 0;
		byte[] contentByte = content.getBytes();

		// 将ip信息放入byte[]中
		byte[] ip = new byte[12];
		String[] ipStr = newIp.split("\\.");
		for (int i = 0; i < ip.length; i++) {

			ip[i] = (byte) Integer.parseInt(ipStr[i]);
		}

		bytes = new byte[head.length + contentByte.length + ip.length];
		// 拷贝头信息到bytes
		System.arraycopy(head, 0, bytes, 0, head.length);
		// 拷贝ssid和密码信息到bytes
		System.arraycopy(contentByte, 0, bytes, head.length, contentByte.length);
		// 拷贝ip信息到bytes
		System.arraycopy(ip, 0, bytes, head.length + contentByte.length, ip.length);

		// 将除去第一位的byte信息放入cutbyte中
		byte[] cutbyte = new byte[bytes.length - 1];
		cutbyte = cutOutByte(bytes, 1, cutbyte.length);

		byte[] out = new byte[2];
		// 进行crc校验
		crc = SDK.SlaveCrc8(cutbyte, cutbyte.length, out);
		Log.d(TAG, "C++ Version:" + out[0]);
		// 将检验后的信息放入bytes[0]
		bytes[0] = out[0];
		return bytes;
	}

	/**
	 * 截取字节
	 * 
	 * @param b
	 * @param start
	 * @param len
	 * @return
	 */
	public byte[] cutOutByte(byte[] b, int start, int len) {
		if (b.length == 0 || len == 0) {
			return null;
		}
		byte[] retb = new byte[len];
		for (int i = 0; i < len; i++) {
			retb[i] = b[start + i];
		}
		return retb;
	}

	@Override
	public void setOnSmartLinkListener(OnSmartLinkListener paramOnSmartLinkListener) {
	}

	/**
	 * 发送组播
	 */
	@Override
	public void start(Context paramContext, String password, String... ssid) throws Exception {

		Log.e(TAG, ssid + ":" + password);

		if ((ssid != null) && (ssid.length > 0)) {
			this.ssid = ssid[0];
			this.newIp = ssid[1];
		}
		this.password = password;

		//设定多播报文的数据
		byte[] data = getMyBytes(this.ssid, this.password, this.newIp);

		isConnecting = true;

		/**
		 * noIPData是去掉ip信息的byte[]
		 */
		byte[] noIPData = new byte[data.length - 12];
		int noIPDataLen = noIPData.length;

		for (int i = 0; i < noIPData.length; i++) {
			noIPData[i] = data[i];
		}

		// byte[] outByte = new byte[2];
		// byte[] cutByte = cutOutByte(data, 1, new byte[-1 +
		// data.length].length);
		// int crc = SDK.SlaveCrc8(cutByte, cutByte.length, outByte);

		// 将ip信息变为都是数字
		String[] ipStr = newIp.split("\\.");
		int ipLen = ipStr.length;

		while (isConnecting) {

			Log.d(TAG, "Connecting..." + data.toString());
			// 生成套接字并绑定36666端口
			multicastSocket = new MulticastSocket(MULTICAST_PORT);
			// if (multicastSocket == null) {
			// multicastSocket = new MulticastSocket(MULTICAST_PORT);
			// Log.d(TAG, "new MulticastSocket()");
			// }
			multicastSocket.setLoopbackMode(true);
			// 设定多播IP
			InetAddress group;
			DatagramPacket packet;
			for (int i = 0; i < noIPDataLen + ipLen; i++) {
				if (i == 0) {
					// 发送校验值
					group = InetAddress.getByName("239." + i + "." + crc + ".254");
					Log.d(TAG, i + ":" + crc);
				} else if (i > 0 && i < noIPDataLen) {
					// 发送ssid和密码
					group = InetAddress.getByName("239." + i + "." + noIPData[i] + ".254");
					Log.d(TAG, i + ":" + noIPData[i]);
				} else {
					// 发送ip信息
					group = InetAddress.getByName("239." + i + "." + Integer.parseInt(ipStr[i - noIPDataLen]) + ".254");
					Log.d(TAG, i + ":" + Integer.parseInt(ipStr[i - noIPDataLen]));
				}
				//加入多播组，发送方和接受方处于同一组时，接收方可抓取多播报文信息
				multicastSocket.joinGroup(group);
				//设定UDP报文（内容，内容长度，多播组，端口）
				packet = new DatagramPacket("".getBytes(), "".getBytes().length, group, MULTICAST_PORT);
				//发送报文
				multicastSocket.send(packet);
				multicastSocket.leaveGroup(group);
			}

		}

	}

	@Override
	public void stop() {
		isConnecting = false;
		if (this.multicastSocket != null) {
			this.multicastSocket.close();
		}
		Log.i("andy", "SnifferSmartLinker.stop()方法执行了！");
	}

	@Override
	public boolean isSmartLinking() {
		return isConnecting;
	}

}
