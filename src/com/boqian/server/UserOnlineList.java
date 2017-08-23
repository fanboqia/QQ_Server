package com.boqian.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

/**在线用户列表**/

/**
 * @author boqianfan
 *
 */
public class UserOnlineList {
	private UserOnlineList(){
		
	}
	
	private static UserOnlineList userOnlineList = new UserOnlineList();
	
	public static UserOnlineList getUserOnlineList(){
		return userOnlineList;
	}
	
	//所有在线账户登记在集合中
	
	//string =  用户编号
	private HashMap<String, UserInfo> hashMap = new HashMap<String, UserInfo>();
	
	//注册在线用户
	public void regOnline(String uid, Socket socket, String email, String phoneNumber){
		UserInfo userInfo =hashMap.get(uid);
		
		//判断其他客户端是否有一样的客户，如果有强行下线
		if(userInfo!= null){
			//强关
			try {
				userInfo.getSocket().getOutputStream().write(4);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				userInfo.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//注册在线用户信息
		userInfo = new UserInfo();
		userInfo.setUid(uid);
		userInfo.setEmail(email);
		userInfo.setPhone(phoneNumber);
		userInfo.setSocket(socket);	
		hashMap.put(uid, userInfo);	//登记在线
	}
	
	
	/**
	 * 更新客户端的UDP信息
	 * @param uid   用户编号
	 * @param ip	udp ip地址
	 * @param port  udp 端口
	 * @throws NullPointerException 空指针异常
	 */
	public void updateOnlineUDP(String uid, String ip, int port) throws NullPointerException{
		UserInfo userInfo = hashMap.get(uid);
		userInfo.setUdpid(ip);
		userInfo.setUdpport(port);
	}
	
	//判断用户是否在线
	//true 为在线
	//false为不在线
	public boolean isUserOnline(String uid){
		return hashMap.containsKey(uid);
	}
	
	/**
	 * 获得在线人的信息
	 * @param uid
	 * @return
	 */	
	public UserInfo getUserOnlineInfo(String uid){
		return hashMap.get(uid);
	}
	
	public void logOut(String uid){
		hashMap.remove(uid);
	}
	
	/**
	 * 获得所有在线信息
	 * @return
	 */
	public Set<String> getAllUserInfo(){
		return hashMap.keySet();
	}
}