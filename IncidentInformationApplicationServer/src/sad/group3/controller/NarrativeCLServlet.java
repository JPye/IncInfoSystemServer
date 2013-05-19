package sad.group3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import sad.group3.domain.Narrative;
import sad.group3.service.NarrativeService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class NarrativeCLServlet extends HttpServlet {

	private static final long serialVersionUID = 5430319324451854629L;
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
			Narrative narrativeSearch = gson.fromJson(searchRequestJson,
					Narrative.class);

			Narrative narrative = NarrativeService
					.searchNarrativeDetail(narrativeSearch.getNarrNum());
			String narrativeJSON = gson.toJson(narrative);
			System.out.println("Narrative Detail Json:\n" + narrativeJSON);
			out.print(DES.encryptDES(narrativeJSON, DES.KEY));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}

}
