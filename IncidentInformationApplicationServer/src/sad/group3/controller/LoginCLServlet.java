package sad.group3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import sad.group3.domain.Officer;
import sad.group3.service.LoginService;
import sad.group3.utils.DES;
import sad.group3.utils.StreamTool;

public class LoginCLServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Gson gson = new Gson();

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		ServletContext servletContext = this.getServletContext();

		try {
			InputStream is = request.getInputStream();
			String officerStr = new String(StreamTool.read(is));
			Officer officer = gson.fromJson(DES.decryptDES(officerStr, DES.KEY),
					Officer.class);
			if (LoginService.loginCheck(officer)) {
				HashMap<String, Boolean> hm = (HashMap<String, Boolean>) servletContext
						.getAttribute("userlist");
//				request.getSession().setAttribute("login_officer", officer.getId());
				
				// 判断是否有hm存在，没有则创建
				if (hm == null) {
					hm = new HashMap<String, Boolean>();
					servletContext.setAttribute("userlist", hm);
					hm.put(officer.getId(), true);
					responseToAndroid(out, "Success",officer.getId());
					return;
				}
				//判断以前是否登陆过,当前是否处于登录状态
				if (hm.get(officer.getId())!=null&&(boolean)hm.get(officer.getId())) {
					responseToAndroid(out, "Warning",officer.getId());
					return;
				}
				//其余状况
				hm.put(officer.getId(), true);
				responseToAndroid(out, "Success",officer.getId());
			} else {
				responseToAndroid(out, "Fail",officer.getId());
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void responseToAndroid(PrintWriter out, String msg,String officerID) throws Exception {
		System.out.println(officerID+" Login "+msg);
		out.print(DES.encryptDES(msg, DES.KEY));
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}

}
