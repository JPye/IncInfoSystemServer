package sad.group3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import sad.group3.domain.Incident;
import sad.group3.domain.IncidentSearch;
import sad.group3.service.IncidentService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class IncidentCLServlet extends HttpServlet {
	private static final long serialVersionUID = -7172312970153644235L;
	private Gson gson = new Gson();

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		try {
			InputStream is = request.getInputStream();
			String searchRequestStr = new String(StreamTool.read(is));
			System.out.println(searchRequestStr
					+ "--------------------------------------");
			String searchRequestJson = DES
					.decryptDES(searchRequestStr, DES.KEY);
			System.out.println(searchRequestJson
					+ "--------------------------------------");
			IncidentSearch searchRequest = gson.fromJson(searchRequestJson,
					IncidentSearch.class);

			String searchType = request.getParameter("search");

			if (searchType.equals("list")) {
				List<Incident> incidentList = IncidentService
						.searchIncidents(searchRequest);
				String incidentListStr = gson.toJson(incidentList);

				System.out.println(DES.encryptDES(incidentListStr, DES.KEY));

				out.print(DES.encryptDES(incidentListStr, DES.KEY));
			} else if (searchType.equals("detail")) {
				String inc_id = searchRequest.getInc_id();
				Incident incident = IncidentService
						.searchIncidentDetail(inc_id);
				String incidentJSON = gson.toJson(incident);
				System.out.println("Incident Detail Json:\n" + incidentJSON);
				out.print(DES.encryptDES(incidentJSON, DES.KEY));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}

	public static void main(String[] args) {
		IncidentSearch incidentSearch = new IncidentSearch();
		Gson gson = new Gson();
		try {
			List<Incident> list = IncidentService
					.searchIncidents(incidentSearch);
			System.out.println(list.size());
			String json = gson.toJson(list);
			System.out.println(gson.toJson(list));
			System.out.println("--------------------------------");
			ArrayList<Incident> al = gson.fromJson(json,
					new TypeToken<List<Incident>>() {
					}.getType());
			for (Incident incident : al) {
				System.out.println(incident.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
