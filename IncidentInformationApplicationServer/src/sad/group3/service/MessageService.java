package sad.group3.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import sad.group3.domain.Incident;
import sad.group3.domain.Message;
import sad.group3.utils.DES;
import sad.group3.utils.SqlHelper;

public class MessageService {
	public static List<Message> getRelatedMsgs(String incID){
		List<Message> messages=new ArrayList<Message>();
		String sql = "select msg_num,to_char(msg_date,'MM-dd-YYYY HH24:MI:SS') as msgdate,o.o_id,o_firstname||' '||o_lastname as fullname from message msg, officer_involve_in oin,officer o where msg.officer_inv_num=oin.officer_inv_num and oin.o_id=o.o_id and inc_id=? order by msg_date";
		ResultSet rs = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { incID });
			while (rs.next()) {
				Message message = new Message();
				message.setMsgNum(rs.getInt("msg_num")+"");
				message.setMsgDate(rs.getString("msgdate"));
				message.setMsgOfficerID(rs.getString("o_id"));
				message.setMsgOfficerName(rs.getString("fullname"));
				messages.add(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	public static Message searchMessageDetail(String msgNum) {
		String sql = "select oin.inc_id,msg_num,to_char(msg_date,'MM-dd-YYYY HH24:MI:SS') as msgdate,o.o_id,o_firstname||' '||o_lastname as fullname,msg_content from message msg, officer_involve_in oin,officer o where msg.officer_inv_num=oin.officer_inv_num and oin.o_id=o.o_id and msg_num=?";	
		ResultSet rs = null;
		Message message = null;
		try {
			rs = SqlHelper.executeNormalQuery(sql, new String[] { msgNum });
			if (rs.next()) {
				message = new Message();
				message.setIncID(rs.getString("inc_id"));
				message.setMsgNum(rs.getInt("msg_num")+"");
				message.setMsgDate(rs.getString("msgdate"));
				message.setMsgOfficerID(rs.getString("o_id"));
				message.setMsgOfficerName(rs.getString("fullname"));
				message.setMsgContent(rs.getString("msg_content"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(rs, SqlHelper.getPs(), SqlHelper.getCt());
		}
		return message;
	}
	
	public static String addNewMessage(Message messageSearch) {
		String sql = "insert into message values(message_seq.nextval,?,sysdate,?)";	
		String result="Add New Message Fail!";
		try {
			boolean flag=SqlHelper.executeUpdate(sql, new String[]{messageSearch.getOfficerInvNum(),messageSearch.getMsgContent()});
			if(flag==true){
				Incident incident=IncidentService.searchIncidentDetail(messageSearch.getIncID());
				Gson gson=new Gson();
				result=gson.toJson(incident);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SqlHelper.close(SqlHelper.getRs(), SqlHelper.getPs(), SqlHelper.getCt());
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
//		List<Message> messages=MessageService.getRelatedMsgs("2012002112");
//		System.out.println(messages.size());
		

		
//		Message message=new Message();
//		message.setOfficerInvNum("28");
//		message.setMsgContent("Add Msg 1");
//		System.out.println(MessageService.addNewMessage(message));
		
		Message message=new Message();
		message.setMsgNum("11");
		Gson gson =new Gson();
		System.out.println(gson.toJson(message));
		System.out.println(DES.encryptDES(gson.toJson(message),DES.KEY));
		System.out.println(DES.decryptDES("QWNwyBwRJ2c=", DES.KEY));
		Message msg=MessageService.searchMessageDetail(message.getMsgNum());
		System.out.println(msg.getMsgContent());
	}




}
