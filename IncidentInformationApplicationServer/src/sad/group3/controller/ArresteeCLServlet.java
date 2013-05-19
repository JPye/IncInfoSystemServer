package sad.group3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import sad.group3.domain.Arrestee;
import sad.group3.domain.ArresteeSearch;
import sad.group3.service.ArresteeService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class ArresteeCLServlet extends HttpServlet {

	private static final long serialVersionUID = -539961096623198426L;
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
			ArresteeSearch arresteeSearch = gson.fromJson(searchRequestJson,
					ArresteeSearch.class);

			String searchType = request.getParameter("search");

			if (searchType.equals("list")) {
				List<Arrestee> arresteeList = ArresteeService
						.searchArrestees(arresteeSearch);
				String arresteeListStr = gson.toJson(arresteeList);
				System.out.println("Arrestee List Json:\n"+arresteeListStr);
				out.print(DES.encryptDES(arresteeListStr, DES.KEY));
			} else if (searchType.equals("detail")) {
				String ap_num = arresteeSearch.getAp_num();
				Arrestee arrestee = ArresteeService
						.searchArresteeDetail(ap_num);
				String arresteeJSON = gson.toJson(arrestee);
				System.out.println("Arrestee Detail Json:\n" + arresteeJSON);
				out.print(DES.encryptDES(arresteeJSON, DES.KEY));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
}
