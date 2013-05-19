package sad.group3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import sad.group3.domain.Message;
import sad.group3.service.MessageService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class MessageCLServlet extends HttpServlet {

	private static final long serialVersionUID = -1777839339030654950L;
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
			Message messageSearch = gson.fromJson(searchRequestJson,
					Message.class);

			String operation = request.getParameter("operation");
			if ("".equals(operation) || operation == null) {
				Message message = MessageService
						.searchMessageDetail(messageSearch.getMsgNum());
				String messageJSON = gson.toJson(message);
				System.out.println("Message Detail Json:\n" + messageJSON);
				out.print(DES.encryptDES(messageJSON, DES.KEY));
			}else if(operation.equals("addnewmsg")){
				String result=MessageService.addNewMessage(messageSearch);
				System.out.println("Message Add Result:\n" + result);
				out.print(DES.encryptDES(result, DES.KEY));
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
