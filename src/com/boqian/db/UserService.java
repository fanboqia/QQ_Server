package com.boqian.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;

import javax.sql.DataSource;

import java.io.IOError;
import java.io.IOException;

//import com.boqian.server.UserInfo;

import java.sql.Connection;
import java.sql.DriverManager;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import com.boqian.db.UserInfo;

public class UserService {

	/**
	 * @param email
	 * @param password
	 * @return
	 * @throws UserNameNotFoundException
	 *             账户不存在
	 * @throws PassWordException
	 *             密码错误
	 * @throws statusException
	 *             账户被锁定
	 * @throws SQLException
	 *             数据库异常
	 */

	public String login(String key, String password, String sql)
			throws UserNameNotFoundException, PassWordException, statusException, SQLException {

		Connection connection = null;

		try {
			connection = DBManager.getConnection();
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setString(1, key);
			ResultSet rSet = pst.executeQuery();

			if (rSet.next()) {

				if (rSet.getInt("status") == 0) {

					if (rSet.getString("password").equals(password)) { // 询问密码是否相同
						return rSet.getString(1);
					} else {
						throw new PassWordException();
					}

				} else {
					throw new statusException();
				}
			} else {
				throw new UserNameNotFoundException();
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			connection.close();
		}
	}

	public String loginForEmail(String email, String password)
			throws UserNameNotFoundException, PassWordException, statusException, SQLException {
		return login(email, password, "SELECT * FROM users where email = ?");
	}

	/**
	 * 获得自己的好友列表信息
	 * 
	 * @param uid
	 *            自己的编号
	 * @return 好友列表信息
	 */
	public Vector<UserInfo> getHaoYouLieBiao(String uid) throws SQLException {
		Connection connection = null;

		try {
			connection = DBManager.getConnection();
			// ************删除最后的*********;
			System.out.println("好友列表数据裤访问");
			PreparedStatement pst = connection.prepareStatement(
					"select u.img, u.uid, u.netname, u.info from hy h inner join users u on u.uid = h.hyuid and h.uid = ?");
			pst.setString(1, uid);
			ResultSet rSet = pst.executeQuery();

			Vector<UserInfo> userInfos = new Vector<>();
			while (rSet.next()) {
				UserInfo userInfo = new UserInfo();
				userInfo.setImg(rSet.getString(1));
				userInfo.setUid(rSet.getString(2));
				userInfo.setNetname(rSet.getString(3));
				userInfo.setInfo(rSet.getString(4));
				userInfos.add(userInfo);
			}

			return userInfos;

		} catch (SQLException e) {
			throw e;
		} finally {
			connection.close();
		}
	}

	// 个人资料查询，好友资料查询
	public UserInfo2 getUserInfo(String uid) throws SQLException {
		Connection connection = null;

		try {
			connection = DBManager.getConnection();
			PreparedStatement pst = connection.prepareStatement("select * from users where uid = ?");
			pst.setString(1, uid);
			ResultSet rSet = pst.executeQuery();

			UserInfo2 userInfo2 = new UserInfo2();
			if (rSet.next()) {
				userInfo2.setUid(rSet.getString("uid"));
				userInfo2.setPhonenumber(rSet.getString("phonenumber"));
				userInfo2.setEmail(rSet.getString("email"));
				userInfo2.setNetname(rSet.getString("netname"));
				userInfo2.setInfo(rSet.getString("info"));
				userInfo2.setName(rSet.getString("name"));
				userInfo2.setBack(rSet.getString("back"));
				userInfo2.setSex(rSet.getString("sex"));
				userInfo2.setYy(rSet.getInt("yy"));
				userInfo2.setMm(rSet.getInt("mm"));
				userInfo2.setDd(rSet.getInt("dd"));
				userInfo2.setImg(rSet.getString("img"));
			}

			return userInfo2;

		} catch (SQLException e) {
			throw e;
		} finally {
			connection.close();
		}
	}

	/**
	 * 
	 * @param phone
	 * @param password
	 * @return
	 * @throws UserNameNotFoundException
	 * @throws PassWordException
	 * @throws statusException
	 * @throws SQLException
	 *             数据库异常
	 */
	public String loginForPhone(String phone, String password)
			throws UserNameNotFoundException, PassWordException, statusException, SQLException {
		return login(phone, password, "SELECT * FROM users where phonenumber = ?");
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @throws UserNameException
	 * @throws SQLException
	 */
	public void regUser(String username, String password) throws UserNameException, SQLException {

		Connection connection = null;

		try {
			connection = DBManager.getConnection();
			// ************删除最后的*********;
			System.out.println("注册用户信息");
			PreparedStatement pst = connection
					.prepareStatement("select * from users where phonenumber = ? or email = ?");
			pst.setString(1, username);
			pst.setString(2, username);
			ResultSet rSet = pst.executeQuery();

			if (rSet.next()) {
				throw new UserNameException();
			}

			if (username.trim().length() == 11 && !username.contains("@")) {//判断是不是手机号
				pst = connection.prepareStatement("insert into users(uid,phonenumber,password,createtime)values(?,?,?,SYSDATE())");		
			}else{
				pst = connection.prepareStatement("insert into users(uid,email,password,createtime)values(?,?,?,SYSDATE())");
			}
			pst.setString(1,System.currentTimeMillis()+"R"+(int)(Math.random()*10000));
			pst.setString(2, username);
			pst.setString(3, password);
			if(pst.executeUpdate() <= 0){
				throw new SQLException();
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			connection.close();
		}
	}

	// public static void main(String []args){
	// try {
	// new UserDao().loginForEmail("boqianfan@gmail.com", "123456");
	// System.out.println("成功");
	// } catch (UserNameNotFoundException | PassWordException | statusException
	// | SQLException e) {
	// e.printStackTrace();
	// }
	// try {
	// new UserDao().loginForPhone("13871136466", "123456");
	// } catch (UserNameNotFoundException | PassWordException | statusException
	// | SQLException e) {
	// e.printStackTrace();
	// }
	//
	// }
}