package sad.group3.service;

import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import sad.group3.domain.Narrative;
import sad.group3.utils.SqlHelper;

public class NarrativeService {
	public static List<Narrative> getRelatedNarratives(String incID) {
		List<Narrative> narratives = new ArrayList<Narrative>();
		String sql = "select narr.officer_inv_num as narr_num,to_char(narr_date,'MM-dd-YYYY HH24:MI:SS') as narrdate,o.o_id,o_firstname||' '||o_lastname as fullname from narrative narr, officer_involve_in oin,officer o where narr.officer_inv_num=oin.officer_inv_num and oin.o_id=o.o_id and inc_id=? order by narr_date";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { incID });
			while (rs.next()) {
				Narrative narrative = new Narrative();
				narrative.setNarrNum(rs.getInt("narr_num") + "");
				narrative.setNarrDate(rs.getString("narrdate"));
				narrative.setNarrOfficerID(rs.getString("o_id"));
				narrative.setNarrOfficerName(rs.getString("fullname"));
				narratives.add(narrative);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return narratives;
	}

	public static Narrative searchNarrativeDetail(String narrNum) {
		String sql = "select narr.officer_inv_num as narr_num,to_char(narr_date,'MM-DD-YYYY HH24:MI:SS') as narrdate,o_firstname||' '||o_lastname as author,o.o_id, narr_content from officer o,officer_involve_in oin,narrative narr where o.o_id=oin.o_id and oin.officer_inv_num=narr.officer_inv_num and narr.officer_inv_num=?";
		ResultSet rs = null;
		Narrative narrative = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { narrNum });
			if (rs.next()) {
				narrative = new Narrative();
				narrative.setNarrNum(rs.getString("narr_num"));
				narrative.setNarrDate(rs.getString("narrdate"));
				narrative.setNarrOfficerName(rs.getString("author"));
				narrative.setNarrOfficerID(rs.getString("o_id"));
				// narrative content Clob
				Clob contentClob = rs.getClob("narr_content");
				Reader inStrean = contentClob.getCharacterStream();
				char[] content = new char[(int) contentClob.length()];
				inStrean.read(content);
				narrative.setNarrContent(new String(content));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return narrative;
	}

	public static void main(String[] args) {
		// List<Narrative> narratives = NarrativeService
		// .getRelatedNarratives("2012002112");
		// System.out.println(narratives.size());
		// System.out.println(narratives.get(0).getNarrNum());

		Narrative narrative = NarrativeService.searchNarrativeDetail("1");
		System.out.println(narrative.getNarrContent());
		System.out.println(new Gson().toJson(narrative));
	}
}
