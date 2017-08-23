package com.boqian.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.server.ServerCloneException;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.boqian.db.PassWordException;
import com.boqian.db.UserInfo;
import com.boqian.db.UserInfo2;
import com.boqian.db.UserService;
import com.boqian.db.UserNameNotFoundException;
import com.boqian.db.statusException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * 登陆服务器主要负责登陆 线程池
 * 
 **/

public class LoginServer implements Runnable {

	private Socket socket = null;

	public LoginServer(Socket socket) {
		this.socket = socket;
		try {
			this.socket.setSoTimeout(600000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// 线程方法
	@Override
	public void run() {

		// 登陆操作
		String uid = null;

		InputStream in = null;
		OutputStream out = null;
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();

			// 等待客户端信息
			byte[] bytes = new byte[1024];
			int len = in.read(bytes);
			// 1 TO LEN....
			String json_string = new String(bytes, 0, len);
			////////////////
			JSONObject jsonObject = JSONObject.fromObject(json_string);
			String username = jsonObject.getString("username");
			String password = jsonObject.getString("password");

			boolean type = false;
			// 判断是不是手机号
			if (username.trim().length() == 11 && (username.indexOf("@") == -1)) {
				try {
					// 判断号码是不是纯数字
					Long.parseLong(username);
					type = true;
				} catch (NumberFormatException e) {
					out.write("{\"status\":4,\"msg\":\"未知错误！\"}".getBytes());
					out.flush();
					return;
				}
			} else {// 非手机号
				type = false;
			}

			try {
				if (type == true) {// 手机号码
					uid = new UserService().loginForPhone(username, password);
					// 登记登陆信息
					UserOnlineList.getUserOnlineList().regOnline(uid, socket, null, username);
				} else {// 其他登陆方式
					uid = new UserService().loginForEmail(username, password);
					// 登记登陆信息
					UserOnlineList.getUserOnlineList().regOnline(uid, socket, username, null);
				}

				// 登陆成功回复客户端消息
				out.write("{\"status\":0,\"msg\":\"登陆成功！\"}".getBytes());
				out.flush();

				// 登陆成功业务
				// 陆续接收客户端发送的指令要求
				while (true) {
					bytes = new byte[2048];
					len = in.read(bytes);
					// *******1 to len*****?????????//I dont think so... so
					// weird... second time
					String command = new String(bytes, 0, len);
					System.out.println("进入服务器端登陆成功服务");
					if (command.equals("U0001")) { // 更新好友列表
						System.out.println("开始更新好友列表!");
						Vector<UserInfo> userInfos = new UserService().getHaoYouLieBiao(uid);
						out.write(JSONArray.fromObject(userInfos).toString().getBytes());
						out.flush();

					} else if (command.equals("U0002")) { // 更新好友在线
						// 响应
						out.write(1);
						out.flush();
						// 获得好友的列表编号
						len = in.read(bytes);// 123,123124,12312312
						String ids = new String(bytes, 0, len);
						String[] ids_str = ids.split(",");

						StringBuffer stringBuffer = new StringBuffer();
						for (String string : ids_str) {
							if (UserOnlineList.getUserOnlineList().isUserOnline(string)) {
								stringBuffer.append(string);
								stringBuffer.append(",");
							}
						}
						if (stringBuffer.length() == 0) {
							// 没有好友在线信息
							out.write("notFound".getBytes());
							out.flush();
						} else {
							// 回执好友在线信息
							out.write(stringBuffer.toString().getBytes());
							out.flush();
						}

					} else if (command.equals("U0003")) { // 更新个人资料

						// 传递个人资料
						UserInfo2 userinfo2 = new UserService().getUserInfo(uid);
						out.write(JSONObject.fromObject(userinfo2).toString().getBytes());
						out.flush();

					} else if (command.equals("E0001")) { // 修改个人资料

					} else if (command.equals("EXIT")) { // 退出用户登陆
						UserOnlineList.getUserOnlineList().logOut(uid);
						return;
					}
				}

			} catch (UserNameNotFoundException e) {
				// e.printStackTrace();
				out.write("{\"status\":2,\"msg\":\"账户名错误！\"}".getBytes());
				out.flush();
				return;
			} catch (PassWordException e) {
				// e.printStackTrace();
				out.write("{\"status\":1,\"msg\":\"用户密码错误！\"}".getBytes());
				out.flush();
				return;
			} catch (statusException e) {
				// e.printStackTrace();
				out.write("{\"status\":3,\"msg\":\"账户锁定！\"}".getBytes());
				out.flush();
				return;
			} catch (SQLException e) {
				// e.printStackTrace();
				out.write("{\"status\":4,\"msg\":\"未知错误！\"}".getBytes());
				out.flush();
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 结束后将连接关闭
			try {
				UserOnlineList.getUserOnlineList().logOut(uid);
				in.close();
				out.close();
				socket.close();
			} catch (Exception e2) {
			}
		}
	}

	public static void openServer() throws Exception {
		// 线程池
		ExecutorService executorService = Executors.newFixedThreadPool(2000);
		// 开启4001端口用于登陆业务
		ServerSocket serverSocket = new ServerSocket(4001);

		// 无限服务
		while (true) {
			Socket socket = serverSocket.accept();
			socket.setSoTimeout(10000);
			executorService.execute(new LoginServer(socket));
		}
	}

	public static void main(String[] args) {
		try {
			openServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
