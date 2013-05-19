package sad.group3.service;

import java.sql.Blob;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import sad.group3.domain.Arrestee;
import sad.group3.domain.ArresteeSearch;
import sad.group3.utils.SqlHelper;
import sad.group3.utils.StreamTool;

public class ArresteeService {

	public static List<Arrestee> searchArrestees(ArresteeSearch arresteeSearch) {
		List<Arrestee> arrestees = new ArrayList<Arrestee>();
		String sql = "select ap_num,ap_firstname,ap_lastname,ap_gender,to_char(ap_birthdate,'MM-DD-YYYY') as birthdate,ap_photo from arrested_person ap,race r where ap.r_num=r.r_num";
		if (arresteeSearch.getAp_firstname() != null
				&& !arresteeSearch.getAp_firstname().equals("")) {
			sql += " and lower(ap_firstname) like lower('"
					+ arresteeSearch.getAp_firstname() + "%')";
		}
		if (arresteeSearch.getAp_lastname() != null
				&& !arresteeSearch.getAp_lastname().equals("")) {
			sql += " and lower(ap_lastname) like lower('" + arresteeSearch.getAp_lastname()
					+ "%')";
		}
		if (arresteeSearch.getAp_gender() != null
				&& !arresteeSearch.getAp_gender().equals("")) {
			sql += " and ap_gender='" + arresteeSearch.getAp_gender() + "'";
		}
		if (arresteeSearch.getRace_num() != null
				&& !arresteeSearch.getRace_num().equals("")) {
			sql += " and r.r_num=" + arresteeSearch.getRace_num();
		}
		if (arresteeSearch.getAp_phonenum() != null
				&& !arresteeSearch.getAp_phonenum().equals("")) {
			String phoneNum=arresteeSearch.getAp_phonenum();
			StringBuffer actualPhoneNum=new StringBuffer();
			for(int i=1;i<=phoneNum.length();i++){
				if(i==4||i==7){
					actualPhoneNum.append("-").append(phoneNum.charAt(i-1));
				}else {
					actualPhoneNum.append(phoneNum.charAt(i-1));
				}
			}
			sql += " and ap_phonenum='" + actualPhoneNum + "'";
		}
		// -------------------------------------------------------------------------------------------
		System.out.println(sql);
		// -------------------------------------------------------------------------------------------
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, null);
			while (rs.next()) {
				Arrestee arrestee = new Arrestee();
				arrestee.setArresteeNum(rs.getInt("ap_num") + "");
				arrestee.setFirstName(rs.getString("ap_firstname"));
				arrestee.setLastName(rs.getString("ap_lastname"));
				arrestee.setGender(rs.getString("ap_gender"));
				arrestee.setBirthdate(rs.getString("birthdate"));
				// photo Blob
				Blob photoBlob = rs.getBlob("ap_photo");
				byte[] photoByte = StreamTool.read(photoBlob.getBinaryStream());
				arrestee.setPhoto(photoByte);
				arrestees.add(arrestee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}

		return arrestees;
	}

	public static List<Arrestee> getInvolvedArrestees(String incID) {
		List<Arrestee> arrestees = new ArrayList<Arrestee>();
		String sql = "select a.ap_num,ap_firstname,ap_lastname,to_char(arrested_date,'MM-dd-YYYY') as arrestdate from arrested_person a,ap_involve_in apin where  apin.ap_num=a.ap_num and apin.inc_id=?";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { incID });
			while (rs.next()) {
				Arrestee arrestee = new Arrestee();
				arrestee.setArresteeNum(rs.getInt("ap_num") + "");
				arrestee.setFirstName(rs.getString("ap_firstname"));
				arrestee.setLastName(rs.getString("ap_lastname"));
				arrestee.setBirthdate(rs.getString("arrestdate"));
				arrestees.add(arrestee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return arrestees;
	}

	public static Arrestee searchArresteeDetail(String apNum) {
		String sql = "select ap.ap_num,ap_firstname,ap_lastname,ap_gender,to_char(ap_birthdate,'MM-DD-YYYY') as birthdate,ap_phonenum,ap_height,ap_weight,r_desc,hc_desc,ap_photo,ap_othercharacteristics,license_to_carry.license_id from race r,hair_color hc,arrested_person ap left join license_to_carry on ap.ap_num=license_to_carry.ap_num where ap.hc_num=hc.hc_num and ap.r_num=r.r_num and ap.ap_num=?";
		ResultSet rs = null;
		Arrestee arrestee = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { apNum });
			if (rs.next()) {
				arrestee = new Arrestee();
				arrestee.setArresteeNum(rs.getString("ap_num"));
				arrestee.setFirstName(rs.getString("ap_firstname"));
				arrestee.setLastName(rs.getString("ap_lastname"));
				arrestee.setGender(rs.getString("ap_gender"));
				arrestee.setBirthdate(rs.getString("birthdate"));
				arrestee.setPhoneNum(rs.getString("ap_phonenum"));
				arrestee.setHeight(rs.getString("ap_height"));
				arrestee.setWeight(rs.getString("ap_weight"));
				arrestee.setRace(rs.getString("r_desc"));
				arrestee.setHairColor(rs.getString("hc_desc"));
				arrestee.setOtherCharacteristics(rs
						.getString("ap_othercharacteristics"));
				arrestee.setLicenseID(rs.getString("license_id"));
				// photo Blob
				Blob photoBlob = rs.getBlob("ap_photo");
				byte[] photoByte = StreamTool.read(photoBlob.getBinaryStream());
				arrestee.setPhoto(photoByte);
			}

			if (arrestee != null) {
				arrestee.setIncidents(IncidentService
						.getInvolvedIncidents(apNum));
				arrestee.setCharges(ChargeService.getRelatedCharges(apNum));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return arrestee;
	}

	// ���charge number����ȡ���б�charge��arrestee List
	public static List<Arrestee> getChargedArrestees(String chargeNum) {
		List<Arrestee> arrestees = new ArrayList<Arrestee>();
		String sql = "select ap.ap_num,ap_firstname,ap_lastname,ap_gender,to_char(ap_birthdate,'MM-DD-YYYY') as birthdate,ap_photo from arrested_person ap,(select distinct ap.ap_num from arrested_person ap,charge_type ct,person_criminal_charge pc,ap_involve_in apin where ct.ct_num=pc.ct_num and pc.inv_num=apin.inv_num and apin.ap_num=ap.ap_num and ct.ct_num=?) a where ap.ap_num=a.ap_num";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { chargeNum });
			while (rs.next()) {
				Arrestee arrestee = new Arrestee();
				arrestee.setArresteeNum(rs.getInt("ap_num") + "");
				arrestee.setFirstName(rs.getString("ap_firstname"));
				arrestee.setLastName(rs.getString("ap_lastname"));
				arrestee.setGender(rs.getString("ap_gender"));
				arrestee.setBirthdate(rs.getString("birthdate"));
				// photo Blob
				Blob photoBlob = rs.getBlob("ap_photo");
				byte[] photoByte = StreamTool.read(photoBlob.getBinaryStream());
				arrestee.setPhoto(photoByte);
				arrestees.add(arrestee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return arrestees;
	}

	public static void main(String[] args) throws Exception {
		// List<Arrestee>
		// arrestees=ArresteeService.getInvolvedArrestees("2012002112");
		// System.out.println(arrestees.size());

		 Arrestee arrestee = ArresteeService.searchArresteeDetail("2");
		 System.out.println(arrestee.getBirthdate());
		 System.out.println(arrestee.getIncidents().size());
		 System.out.println(arrestee.getCharges().size());

		// Gson gson = new Gson();
		// String jsonString = gson.toJson(arrestee);
		// System.out.println(jsonString);
		// Arrestee arrestee2 = gson.fromJson(jsonString, Arrestee.class);
		// System.out.println(arrestee2.getIncidents().get(0).getDate());
//		ArresteeSearch arresteeSearch=new ArresteeSearch();
//		arresteeSearch.setAp_num("M");
//		 List<Arrestee> arrestees=ArresteeService.searchArrestees(arresteeSearch);
//		 System.out.println(new Gson().toJson(arrestees.get(1)));
		// String jsonStr=DES.encryptDES(gson.toJson(arrestees), DES.KEY);
		// String json=DES.decryptDES(jsonStr, DES.KEY);
		// List<Arrestee> arrestees2=gson.fromJson(json, new
		// TypeToken<List<Arrestee>>(){}.getType());
		// System.out.println(arrestees2.get(0).getPhoto().length);
//		for (int i = 1; i <= 24; i++) {
//			Arrestee arrestee = ArresteeService.searchArresteeDetail(i + "");
//			System.out.println(i + " : " + arrestee.getLicenseID());
//		}
		
//		List<Arrestee> arrestees=ArresteeService.getChargedArrestees("1");
//		System.out.println(arrestees.size());

	}

}
