package sad.group3.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import sad.group3.domain.Caller;
import sad.group3.utils.SqlHelper;

public class CallerService {
	public static List<Caller> getRelatedCallers(String incID, String sourceType) {
		List<Caller> callers = new ArrayList<Caller>();
		String sql = null;
		ResultSet rs = null;

		if (sourceType.equals("Patrol")) {
			sql = "select patrol_call.o_id,o_firstname||' '||o_lastname as fullname,to_char(pc_date,'MM-dd-YYYY HH24:MI:SS') as calldate from officer,patrol_call where patrol_call.o_id=officer.o_id and inc_id=?";
			try {
				rs = SqlHelper.executeNormalQuery(sql, new String[] { incID });
				while (rs.next()) {
					Caller caller = new Caller();
					caller.setId(rs.getString("o_id"));
					caller.setCallDate(rs.getString("calldate"));
					caller.setFullName(rs.getString("fullname"));
					callers.add(caller);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
			}
			
		} else {
			
			sql = "select to_char(call_date,'MM-dd-YYYY HH24:MI:SS') as calldate,caller_firstname||' '||caller_lastname as fullname,caller_phonenum from caller,call_incident where caller.caller_num=call_incident.call_num and call_incident.inc_id=?";
			try {
				rs = SqlHelper.executeNormalQuery(sql, new String[] { incID });
				while (rs.next()) {
					Caller caller = new Caller();
					caller.setCallDate(rs.getString("calldate"));
					caller.setFullName(rs.getString("fullname"));
					caller.setPhoneNum(rs.getString("caller_phonenum"));
					callers.add(caller);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
			}
		}

		return callers;
	}

	public static void main(String[] args) {
		List<Caller> callers = CallerService.getRelatedCallers("2012015391","Patrol");
		System.out.println(callers.size());
	}

}
