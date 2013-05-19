package sad.group3.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class LogoutCLServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
//		System.out.println(request.getSession().getId()+"--------------------");
//		if(request.getSession().getAttribute("login_officer")==null){
//			try {
//				out.print(DES.encryptDES("Session Expire", DES.KEY));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return;
//		}

		ServletContext servletContext = this.getServletContext();
		HashMap<String, Boolean> hm = (HashMap<String, Boolean>) servletContext
				.getAttribute("userlist");

		try {
			String oidStr = new String(StreamTool.read(request.getInputStream()));
			String o_id = DES.decryptDES(oidStr, DES.KEY);
			if(o_id!=null){
				hm.put(o_id, false);
			}
			System.out.println(o_id+" Logout Success");
			out.print(DES.encryptDES("Success", DES.KEY));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
