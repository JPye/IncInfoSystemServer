package sad.group3.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import sad.group3.domain.Officer;
import sad.group3.utils.SqlHelper;

public class OfficerService {
	public static List<Officer> getInvolvedOfficers(String incID) {
		List<Officer> officers = new ArrayList<Officer>();
		String sql = "select o.o_id,o_lastname,o_firstname,oin.officer_inv_primary from officer o,officer_involve_in oin,incident inc where o.o_id=oin.o_id and oin.inc_id=inc.inc_id and inc.inc_id=?";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { incID });
			while (rs.next()) {
				Officer officer = new Officer();
				officer.setId(rs.getString("o_id"));
				officer.setLastName(rs.getString("o_lastname"));
				officer.setFirstName(rs.getString("o_firstname"));
				officer.setPrimary(rs.getString("officer_inv_primary"));
				officers.add(officer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return officers;
	}

	public static void main(String[] args) {
		List<Officer> officers = OfficerService.getInvolvedOfficers("2012002112");
		System.out.println(officers.size());
	}
}
