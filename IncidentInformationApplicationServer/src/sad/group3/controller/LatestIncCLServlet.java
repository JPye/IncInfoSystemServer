package sad.group3.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import sad.group3.domain.Incident;
import sad.group3.service.IncidentService;
import sad.group3.utils.DES;

public class LatestIncCLServlet extends HttpServlet {

	private static final long serialVersionUID = -1610261748789792627L;
	private Gson gson=new Gson();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try {
				List<Incident> incidentList = IncidentService.getLastestIncidents();
				String incidentListStr = gson.toJson(incidentList);
				System.out.println(DES.encryptDES(incidentListStr, DES.KEY));
				out.print(DES.encryptDES(incidentListStr, DES.KEY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}

}
