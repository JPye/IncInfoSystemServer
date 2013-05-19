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
import sad.group3.domain.Charge;
import sad.group3.service.ArresteeService;
import sad.group3.service.ChargeService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class ChargeCLServlet extends HttpServlet {

	private static final long serialVersionUID = -2601678769138539772L;
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
			Charge chargeSearch = gson
					.fromJson(searchRequestJson, Charge.class);

			String searchType = request.getParameter("search");
			if (searchType.equals("chargelist")) {
				List<Charge> chargeList = ChargeService
						.searchCharges(chargeSearch.getChargeDesc());
				String chargeListStr = gson.toJson(chargeList);
				System.out.println(DES.encryptDES(chargeListStr, DES.KEY));
				out.print(DES.encryptDES(chargeListStr, DES.KEY));
			} else if (searchType.equals("charge_arresteelist")) {
				List<Arrestee> arresteeList=ArresteeService.getChargedArrestees(chargeSearch.getChargeNum());
				String arresteeListStr = gson.toJson(arresteeList);
				System.out.println(DES.encryptDES(arresteeListStr, DES.KEY));
				out.print(DES.encryptDES(arresteeListStr, DES.KEY));
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
