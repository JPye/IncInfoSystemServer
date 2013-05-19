package sad.group3.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import sad.group3.domain.Report;
import sad.group3.service.ReportService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class ChartCLServlet extends HttpServlet {

	private static final long serialVersionUID = -3479792384836557864L;
	private Gson gson = new Gson();
	private ReportService reportService = new ReportService();

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
			Report reportSearch = gson
					.fromJson(searchRequestJson, Report.class);
			String reportType = reportSearch.getReportType();

//			 Report reportSearch = new Report();
//			 reportSearch.setYear("2012");
//			 String reportType = "IncQtySector";

			if (reportType.equals("IncQtySector")) {
				Report report = reportService.getSectorReport(request,
						reportSearch.getYear(), new PrintWriter(new File(System.getProperty("java.io.tmpdir")+"/temp.txt")));
				String reportJSON = gson.toJson(report);
				System.out.println("Sector Report List Json:\n" + reportJSON);
				out.print(DES.encryptDES(reportJSON, DES.KEY));
			} else if (reportType.equals("IncQtyMonth")) {
				Report report = reportService.getMonthReport(request,
						reportSearch.getYear(),  new PrintWriter(new File(System.getProperty("java.io.tmpdir")+"/temp.txt")));
				String reportJSON = gson.toJson(report);
				System.out.println("Month Report List Json:\n" + reportJSON);
				out.print(DES.encryptDES(reportJSON, DES.KEY));
			} else if (reportType.equals("CallQtyPeriod")) {
				Report report = reportService.getTimeReport(request,
						reportSearch.getYear(),  new PrintWriter(new File(System.getProperty("java.io.tmpdir")+"/temp.txt")));
				String reportJSON = gson.toJson(report);
				System.out.println("Month Report List Json:\n" + reportJSON);
				out.print(DES.encryptDES(reportJSON, DES.KEY));
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
