package sad.group3.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/*
 * 这是一个操作数据库的工具类
 */
public class SqlHelper {
	private static String url = null;
	private static String username = null;
	private static String passwd = null;
	private static String driver = null;

	private static Connection ct = null;
	private static PreparedStatement ps = null;
	private static ResultSet rs = null;

	private static Properties pp = null;
	private static InputStream is = null;

	static {
		try {
			pp = new Properties();
			// 当我们使用java web时，读取文件要使用类加载器[因为类加载器读取资源的时候，默认的主目录是src]
			is = SqlHelper.class.getClassLoader().getResourceAsStream(
					"sad/group3/utils/dbinfo.properties");
			pp.load(is);
			url = pp.getProperty("url");
			username = pp.getProperty("username");
			passwd = pp.getProperty("passwd");
			driver = pp.getProperty("driver");
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			is = null;
		}
	}

	public static Connection getConnection() throws SQLException {
		try {
			ct = DriverManager.getConnection(url, username, passwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ct;
	}

	public static ResultSet executeNormalQuery(String sql, String[] parameters) {
		try {
			ct = getConnection();
			ps = ct.prepareStatement(sql);
			if (parameters != null && !parameters.equals("")) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}
			rs = ps.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 不可关闭，关闭后外部resultset将不能读取数据
		}
		return rs;
	}

	public static int executeInsert(String sql, String[] parameters) {
		int id = 0;
		try {
			ct = getConnection();
			int[] columnIndex = { 1 };
			ps = ct.prepareStatement(sql, columnIndex);
			if (parameters != null && !parameters.equals("")) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}
			ps.executeUpdate();
			ct.commit();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			// 注意没有关闭！
		}
		return id;
	}

	// 执行删除，更新，添加操作
	public static boolean executeUpdate(String sql, String[] parameters) {
		boolean flag = true;
		try {
			ct = getConnection();
			ps = ct.prepareStatement(sql);
			if (parameters != null && !parameters.equals("")) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}
			ps.executeUpdate();
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			close(rs, ps, ct);
		}
		return flag;
	}

	public static int executeUpdateAll(String sql1, String[] parameters,
			String sql2, String p_id, String sql3, String[] u_id) {
		int flag = 0;
		try {
			ct = getConnection();
			ct.setAutoCommit(false);
			ps = ct.prepareStatement(sql1);
			if (parameters != null && !parameters.equals("")) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}
			ps.executeUpdate();

			ps = ct.prepareStatement(sql2);
			ps.setInt(1, Integer.parseInt(p_id));
			ps.executeUpdate();

			ps = ct.prepareStatement(sql3);
			for (int i = 0; i < u_id.length; i++) {
				ps.setInt(1, Integer.parseInt(p_id));
				ps.setInt(2, Integer.parseInt(u_id[i]));
				ps.addBatch();
			}
			ps.executeBatch();
			ct.commit();

			flag = 1;
		} catch (Exception e) {
			try {
				ct.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1.getMessage());
			}
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			close(rs, ps, ct);
		}
		return flag;
	}

	public static PreparedStatement getPs() {
		return ps;
	}

	public static ResultSet getRs() {
		return rs;
	}

	public static Connection getCt() {
		return ct;
	}

	public static ArrayList<Object> executeQuery(String sql, String[] params) {
		ArrayList<Object> al = new ArrayList<Object>();
		try {
			ct = getConnection();
			ps = ct.prepareStatement(sql);
			// 给sql的问号赋值
			for (int i = 0; i < params.length; i++) {
				ps.setString(i + 1, params[i]);
			}
			rs = ps.executeQuery();
			// 这句话非常有用
			ResultSetMetaData rsmd = rs.getMetaData();
			// 通过rsmd可以得到该结果集有多少列
			int columnNum = rsmd.getColumnCount();
			// 循环从rs中取出数据，放入al中
			while (rs.next()) {
				Object[] objects = new Object[columnNum];
				for (int j = 0; j < columnNum; j++) {
					objects[j] = rs.getObject(j + 1);
				}
				al.add(objects);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			close(rs, ps, ct);
		}

		return al;
	}

	public static void close(ResultSet rs, PreparedStatement ps, Connection ct) {
		// 6释放资源
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (ct != null)
						ct.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {

	}

}
