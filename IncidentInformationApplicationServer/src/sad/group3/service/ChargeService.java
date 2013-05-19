package sad.group3.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import sad.group3.domain.Charge;
import sad.group3.utils.SqlHelper;

public class ChargeService {

	public static List<Charge> getRelatedCharges(String apNum) {
		List<Charge> charges = new ArrayList<Charge>();
		String sql = " select inc_id,ct.ct_num,ct_desc from charge_type ct,person_criminal_charge pcc,ap_involve_in apin where ct.ct_num=pcc.ct_num and pcc.inv_num=apin.inv_num and ap_num=? order by inc_id";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { apNum });
			while (rs.next()) {
				Charge charge = new Charge();
				charge.setIncID(rs.getString("inc_id"));
				charge.setChargeNum(rs.getInt("ct_num") + "");
				charge.setChargeDesc(rs.getString("ct_desc"));
				charges.add(charge);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		
		System.out.println(sql);
		
		return charges;
	}

	public static List<Charge> searchCharges(String chargeDesc) {
		List<Charge> charges = new ArrayList<Charge>();
		String sql = "select ct.ct_num,ct_desc,count(distinct person.ap_num) as total from charge_type ct left join (select pc.inv_num,apin.ap_num,pc.ct_num from person_criminal_charge pc, ap_involve_in apin where pc.inv_num=apin.inv_num) person on ct.ct_num=person.ct_num where lower(ct_desc) like lower(?) group by ct.ct_num,ct_desc order by ct.ct_num";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[]{"%"+chargeDesc+"%"});
			while (rs.next()) {
				Charge charge = new Charge();
				charge.setChargeNum(rs.getString("ct_num"));
				charge.setChargeDesc(rs.getString("ct_desc"));
				charge.setArresteeQty(rs.getInt("total") + "");
				charges.add(charge);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		
		System.out.println(sql);
		
		return charges;
	}

	public static void main(String[] args) {
		// List<Charge> charges=ChargeService.getRelatedCharges("1");
		// System.out.println(charges.size());
		List<Charge> charges = ChargeService.searchCharges("arm");
		System.out.println(charges.size());
	}

}
