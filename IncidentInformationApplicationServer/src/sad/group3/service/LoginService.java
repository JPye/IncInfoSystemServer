package sad.group3.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import sad.group3.domain.Officer;
import sad.group3.utils.SqlHelper;

public class LoginService {

	// 用户登录
	public synchronized static boolean loginCheck(Officer officer) {
		boolean b = false;
		// 使用SqlHelper完成查询
		String sql = "select * from officer where o_id=? and o_password=?";
		String[] parameters = {officer.getId(), officer.getPwd()};
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, parameters);
			if (rs.next()) {
				b = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, (PreparedStatement) SqlHelper.getPs(),
					SqlHelper.getCt());
		}
		return b;
	}
	
	public static void main(String[] args) {
		//Login Test,Successful
		Officer officer=new Officer("B846", null, null, "e10adc3949ba59abbe56e057f20f883e");
		System.out.println(LoginService.loginCheck(officer));
	}
}
