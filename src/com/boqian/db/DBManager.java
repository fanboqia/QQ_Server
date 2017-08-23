package com.boqian.db;

import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 用于数据库链接配置
 * @author boqianfan
 *
 */
public class DBManager {
	public static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
	public static final String URL = "jdbc:mysql://localhost:3306/frank_qq";
	public static final String ACCOUNT_NAME = "root";
	public static final String PASSWORD = "root";	
	public static DataSource dataSource = null; //****** Change DataSource to ComboPooledDataSource*****....
	
	
	//准备加载数据源 c3p0
	static{
		try {
			ComboPooledDataSource pooledDataSource = new ComboPooledDataSource();
			pooledDataSource.setDriverClass(DRIVER_NAME);
			pooledDataSource.setUser(ACCOUNT_NAME);
			pooledDataSource.setPassword(PASSWORD);
			pooledDataSource.setJdbcUrl(URL);
			pooledDataSource.setMaxPoolSize(30);
			pooledDataSource.setMinPoolSize(5);
			dataSource = pooledDataSource;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("数据链接池加载失败！");
		}
	}
	
	//大家通过此方法获得connection对象
	public static Connection getConnection(){
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}
	
//	public static Connection getConnection() throws SQLException {
//		try {
//			Class.forName(DRIVER_NAME);
//			return DriverManager.getConnection(URL, ACCOUNT_NAME, PASSWORD);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new SQLException();
//		}
//	}
	
//	public static Connection getConnection(){
//		Connection connection = null;
//		try{
//			Class.forName("com.mysql.jdbc.Driver");		
//			//connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jsp_db","root","root");
//			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/frank_qq","root","root");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return connection;
//	}
}
