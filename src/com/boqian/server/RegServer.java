package com.boqian.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.map.HashedMap;

import com.boqian.db.UserNameException;
import com.boqian.db.UserService;

import net.sf.json.JSONObject;

/**
 * 注册服务器
 * 
 * @author boqianfan 1.注册申请 2.验证吗申请
 */
public class RegServer implements Runnable {

	private Socket socket = null;

	public RegServer(Socket socket) {
		this.socket = socket;
	}

	// 所有的验证码都存储在这,手机号，验证码绑定
	private static HashMap<String, String> hashMap = new HashMap<String, String>();

	@Override
	public void run() {

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			// 等待客户端发送消息
			byte[] bytes = new byte[1024];
			int len = inputStream.read(bytes);
			String json_str = new String(bytes, 0, len);
			JSONObject json = JSONObject.fromObject(json_str);
			String type = json.getString("type");
			if (type.equals("code")) {// 验证码请求

				String username = json.getString("username");

				Random random = new Random();
				StringBuffer code = new StringBuffer();
				for (int i = 0; i < 6; i++) {
					code.append(random.nextInt(10));
				}

				if (username.trim().length() == 11) {
					try {
						Long.parseLong(username);
						hashMap.put(username, code.toString());
						SendCode.send(username, code.toString());
						outputStream.write("{\"status\":0,\"msg\":\"验证码发送成功！\"}".getBytes());
						outputStream.flush();

					} catch (Exception e) {
						outputStream.write("{\"status\":1,\"msg\":\"验证码发送失败！\"}".getBytes());
						outputStream.flush();
					}
				} else {
					if (username.indexOf("@") >= 0) {

						hashMap.put(username, code.toString());
						SendCode.sendEmail(username, code.toString());
						outputStream.write("{\"status\":0,\"msg\":\"验证码发送成功！\"}".getBytes());
						outputStream.flush();

					} else {
						outputStream.write("{\"status\":1,\"msg\":\"验证码发送失败！\"}".getBytes());
						outputStream.flush();
					}
				}

			} else if (type.equals("reg")) {// 注册请求
				
				String username = json.getString("username");
				String code = json.getString("code");
				String password = json.getString("password");
				String code1 = hashMap.get(username);
				
				//删除上次验证码
				if(code1!=null)
				{
					hashMap.remove(username);
				}
				
				if(code1.equals(code)){// 询问验证码是否正确
					
					try {
						new UserService().regUser(username, password);
					} catch (UserNameException e) {
						outputStream.write("{\"status\":1,\"msg\":\"用户名已存在！\"}".getBytes());
						outputStream.flush();
						return;
					} catch (SQLException e) {
						outputStream.write("{\"status\":3,\"msg\":\"未知错误！\"}".getBytes());
						outputStream.flush();
						return;
					}
					
					outputStream.write("{\"status\":0,\"msg\":\"注册成功！可以登陆了!\"}".getBytes());
					outputStream.flush();
					
				}else{
					outputStream.write("{\"status\":2,\"msg\":\"验证码错误请重新获得！\"}".getBytes());
					outputStream.flush();
				}
				

			}

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				inputStream.close();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void openServer() throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(30);
		ServerSocket serverSocket = new ServerSocket(4002);

		while (true) {
			Socket socket = serverSocket.accept();
			executorService.execute(new RegServer(socket));
		}
	}
}
