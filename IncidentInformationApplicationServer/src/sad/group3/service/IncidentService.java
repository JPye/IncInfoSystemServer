package sad.group3.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import sad.group3.domain.Incident;
import sad.group3.domain.IncidentSearch;
import sad.group3.domain.Officer;
import sad.group3.utils.SqlHelper;

public class IncidentService {

	public static List<Incident> searchIncidents(IncidentSearch searchRequest)
			throws Exception {
		List<Incident> incidents = new ArrayList<Incident>();

		String sql = "select inc_id,to_char(inc_date,'MM-dd-YYYY HH24:MI:SS') as incident_date,inct.inctype_desc as inc_type,inc_stname,inc_stnum,inc_aptnum "
				+ "from incident inc,incident_type inct,district dist  "
				+ "where inc.inctype_num=inct.inctype_num and inc.dist_num=dist.dist_num";
		if (searchRequest.getDist_Num() != null
				&& !searchRequest.getDist_Num().equals("")) {
			String sector="";
			if(searchRequest.getDist_Num().trim().equals("0")){
				sector="Northeast Sector";
			}
			if(searchRequest.getDist_Num().trim().equals("1")){
				sector="Southeast Sector";
			}
			if(searchRequest.getDist_Num().trim().equals("2")){
				sector="Southwest Sector";
			}
			if(searchRequest.getDist_Num().trim().equals("3")){
				sector="Northwest Sector";
			}
			sql += " and dist.sector_name='" + sector+"'";
		}
		if (searchRequest.getInc_AptNum() != null
				&& !searchRequest.getInc_AptNum().equals("")) {
			sql += " and inc_aptnum='" + searchRequest.getInc_AptNum().trim() + "'";
		}
		if (searchRequest.getFrom_inc_date() != null
				&& !searchRequest.getFrom_inc_date().equals("")) {
			sql += " and inc_date>=to_date('"
					+ searchRequest.getFrom_inc_date().trim() + "','MM-dd-YYYY')";
		}
		if (searchRequest.getTo_inc_date() != null
				&& !searchRequest.getTo_inc_date().equals("")) {
			sql += " and inc_date<=to_date('" + searchRequest.getTo_inc_date().trim()
					+ "','MM-dd-YYYY')";
		}
		if (searchRequest.getInc_StName() != null
				&& !searchRequest.getInc_StName().equals("")) {
			sql += " and lower(inc_stname) like lower('"
					+ searchRequest.getInc_StName().trim() + "%')";
		}
		if (searchRequest.getInc_StNum() != null
				&& !searchRequest.getInc_StNum().equals("")) {
			sql += " and inc_stnum='" + searchRequest.getInc_StNum().trim() + "'";
		}
		if (searchRequest.getIncType_Num() != null
				&& !searchRequest.getIncType_Num().equals("")) {
			sql += " and inc.inctype_num=" + searchRequest.getIncType_Num().trim();
		}
		if (searchRequest.getInc_Source() != null
				&& !searchRequest.getInc_Source().equals("")) {
			String source="";
			if(searchRequest.getInc_Source().trim().equals("0")){
				source="911";
			}
			if(searchRequest.getInc_Source().trim().equals("1")){
				source="Patrol";
			}
			if(searchRequest.getInc_Source().trim().equals("2")){
				source="Complaint";
			}
			sql += " and inc_source='" + source + "'";
		}
		if (searchRequest.getInc_Resolved() != null
				&& !searchRequest.getInc_Resolved().equals("")) {
			String resolved="";
			if(searchRequest.getInc_Resolved().trim().equals("0")){
				resolved="Y";
			}
			if(searchRequest.getInc_Resolved().trim().equals("1")){
				resolved="N";
			}
			sql += " and inc_resolved='" + resolved
					+ "'";
		}
		sql+=" order by inc_date";
		// -------------------------------------------------------------------------------------------
		System.out.println(sql);
		// -------------------------------------------------------------------------------------------
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, null);
			while (rs.next()) {
				Incident incident = new Incident();
				incident.setId(rs.getString("inc_id"));
				incident.setDate(rs.getString("incident_date"));
				incident.setIncidentType(rs.getString("inc_type"));
				incident.setApartmentNum(rs.getString("inc_aptnum"));
				incident.setStreetName(rs.getString("inc_stname"));
				incident.setStreetNum(rs.getString("inc_stnum"));
				incidents.add(incident);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}

		return incidents;
	}

	public static Incident searchIncidentDetail(String inc_id) {
		String sql = "select inc_id,to_char(inc_date,'MM-dd-YYYY HH24:MI:SS') as incident_date,inct.inctype_desc,inc_stname,inc_stnum,inc_aptnum,inc.dist_num,dist.sector_name,inc_source,inc_resolved from incident inc,incident_type inct,district dist where inc.inctype_num=inct.inctype_num and inc.dist_num=dist.dist_num and inc_id=?";
		ResultSet rs = null;
		Incident incident = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { inc_id });
			if (rs.next()) {
				incident = new Incident();
				incident.setId(rs.getString("inc_id"));
				incident.setDate(rs.getString("incident_date"));
				incident.setIncidentType(rs.getString("inctype_desc"));
				incident.setApartmentNum(rs.getString("inc_aptnum"));
				incident.setStreetName(rs.getString("inc_stname"));
				incident.setStreetNum(rs.getString("inc_stnum"));
				incident.setDistrictNum(rs.getString("dist_num"));
				incident.setSector(rs.getString("sector_name"));
				incident.setSource(rs.getString("inc_source"));
				incident.setResolved(rs.getString("inc_resolved"));
			}

			if (incident != null) {
				incident
						.setOfficers(OfficerService.getInvolvedOfficers(inc_id));
				incident.setArrestees(ArresteeService
						.getInvolvedArrestees(inc_id));
				incident.setNarratives(NarrativeService
						.getRelatedNarratives(inc_id));
				incident.setMessages(MessageService.getRelatedMsgs(inc_id));
				incident.setCallers(CallerService.getRelatedCallers(inc_id,
						incident.getSource()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return incident;
	}

	public static List<Incident> getInvolvedIncidents(String apNum) {
		List<Incident> incidents = new ArrayList<Incident>();
		String sql = "select inc.inc_id,to_char(inc_date,'MM-dd-YYYY HH24:MI:SS') as incident_date,inct.inctype_desc,inc_stname,inc_stnum,inc_aptnum from incident inc,incident_type inct,arrested_person ap, ap_involve_in apin where inc.inctype_num=inct.inctype_num and ap.ap_num=apin.ap_num and apin.inc_id=inc.inc_id and ap.ap_num=?";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { apNum });
			while (rs.next()) {
				Incident incident = new Incident();
				incident.setId(rs.getString("inc_id"));
				incident.setDate(rs.getString("incident_date"));
				incident.setIncidentType(rs.getString("inctype_desc"));
				incident.setApartmentNum(rs.getString("inc_aptnum"));
				incident.setStreetName(rs.getString("inc_stname"));
				incident.setStreetNum(rs.getString("inc_stnum"));
				incidents.add(incident);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return incidents;
	}

	// ��ȡ�������incident��¼����������
	public static List<Incident> getLastestIncidents() {
		List<Incident> incidents = new ArrayList<Incident>();
		String sql = "select inc_id,to_char(inc_date,'MM-dd-YYYY HH24:MI:SS') as incident_date,inct.inctype_desc as inc_type,inc_stname,inc_stnum,inc_aptnum from (select * from incident order by incident.inc_date desc) inc,incident_type inct where rownum<=5";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, null);
			while (rs.next()) {
				Incident incident = new Incident();
				incident.setId(rs.getString("inc_id"));
				incident.setDate(rs.getString("incident_date"));
				incident.setIncidentType(rs.getString("inc_type"));
				incident.setApartmentNum(rs.getString("inc_aptnum"));
				incident.setStreetName(rs.getString("inc_stname"));
				incident.setStreetNum(rs.getString("inc_stnum"));
				incidents.add(incident);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}

		return incidents;
	}

	public static List<Incident> getMyTasks(Officer officer) {
		List<Incident> incidents = new ArrayList<Incident>();
		String sql = "select oin.officer_inv_num,inc.inc_id,to_char(inc_date,'MM-dd-YYYY HH24:MI:SS') as incident_date,inct.inctype_desc as inc_type,inc_stname,inc_stnum,inc_aptnum from (select * from incident order by incident.inc_date desc) inc,incident_type inct,officer_involve_in oin,officer o where inct.inctype_num=inc.inctype_num and oin.inc_id=inc.inc_id and oin.o_id=o.o_id and o.o_id=? and inc.inc_resolved='N'";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[]{officer.getId()});
			while (rs.next()) {
				Incident incident = new Incident();
				incident.setOfficerInvNum(rs.getString("officer_inv_num"));
				incident.setId(rs.getString("inc_id"));
				incident.setDate(rs.getString("incident_date"));
				incident.setIncidentType(rs.getString("inc_type"));
				incident.setApartmentNum(rs.getString("inc_aptnum"));
				incident.setStreetName(rs.getString("inc_stname"));
				incident.setStreetNum(rs.getString("inc_stnum"));
				incidents.add(incident);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}

		return incidents;
	}

	public static void main(String[] args) throws Exception {
		// IncidentSearch incidentSearch = new IncidentSearch(null,
		// "01-01-2012",
		// null, null, null, null, null, null, null, null);
		// Gson gson = new Gson();
		// System.out.println(gson.toJson(incidentSearch));
		// Incident incident=IncidentService.searchIncidentDetail("2012002112");
		// String json=gson.toJson(incident);
		// System.out.println(json);
		// Incident incident2=gson.fromJson(json, Incident.class);
		// System.out.println(incident2.getArrestees().get(1).getFirstName());

		// List<Incident> incidents = IncidentService.getInvolvedIncidents("1");
//		List<Incident> incidents = IncidentService.getLastestIncidents();
//		System.out.println(incidents.size());
		// System.out.println(incident.getIncidentType());
//		Officer officer=new Officer();
//		officer.setId("1234");
//		List<Incident> myTasks=IncidentService.getMyTasks(officer);
//		System.out.println(myTasks.size());
		
		IncidentSearch incidentSearch=new IncidentSearch();
		incidentSearch.setIncType_Num("2");
		List<Incident> incidents=IncidentService.searchIncidents(incidentSearch);
		for(Incident incident:incidents){
			System.out.println(incident.getIncidentType());
		}
	}

}
