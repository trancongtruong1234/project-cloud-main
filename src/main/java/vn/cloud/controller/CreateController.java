package vn.cloud.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jcraft.jsch.JSchException;

import vn.cloud.config.Config;
import vn.cloud.connection.DBconnect;
import vn.cloud.dao.HomeDao;
import vn.cloud.model.LoginModel;
import vn.cloud.model.ServerModel;

@WebServlet(urlPatterns = {"/create"})
public class CreateController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/htm");
		resp.setCharacterEncoding("UTF-8");
		req.setCharacterEncoding("UTF-8");
		HttpSession session = req.getSession();
		LoginModel info = (LoginModel) session.getAttribute("info");
		
		// Lấy thông tin các server và lưu vào list server
		String sql = "select * from servers;";
		ResultSet rst;
		ArrayList<ServerModel> listserver = new ArrayList<>();
		try {
			// kết nối sql
			Connection conn = new DBconnect().getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			rst = ps.executeQuery();
			
		    while (rst.next()) {
		    	ServerModel server = new ServerModel(rst.getInt("id_server"), rst.getString("ip_server"));
		    	listserver.add(server);
		    }
//		    for (int i = 0; i < listserver.size(); i++) {
//
//		        System.out.println(listserver.get(i));
//		        System.out.println(listserver.get(i));
//		    }
		} catch (Exception e) {

		}
		req.setAttribute("listserver", listserver);
		
		session.setAttribute("listserver", listserver);
		
		if(info.getRole() == 0)
		{
			RequestDispatcher rq = req.getRequestDispatcher("/views/create.jsp");
			rq.forward(req, resp);
		}
		else
		{
			resp.sendRedirect("page404");
		}
		
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/htm");
		resp.setCharacterEncoding("UTF-8");
		req.setCharacterEncoding("UTF-8");
		String os = req.getParameter("os");
		String ram  = req.getParameter("ram");
		String cpu = req.getParameter("cpu");
		HomeDao hd = new HomeDao();
		HttpSession session = req.getSession();
		LoginModel info = (LoginModel) session.getAttribute("info");
		String port = hd.maxPort();
		String cname = "user" +Integer.toString(info.getId()) +"-" + os + "-" + port;
		String ec2ip ="";
		String server = req.getParameter("server");
		int _id_server=Integer.parseInt(server);
		//System.out.print(server);
		// Lấy thông tin của list server 
		
		ArrayList<ServerModel> listserver = (ArrayList<ServerModel>)session.getAttribute("listserver");
		for (ServerModel _server  : listserver) {
			  int id_server=_server.getId_server();
			  //String _id_server =_String.valueOf(id_server);
		      if(id_server==_id_server) {
		    	  String ip_server=_server.getIp_server();
		    	  ec2ip=ip_server;
		    	  System.out.print(id_server);
		    	  System.out.print(_id_server);
		    	  System.out.print(ec2ip);
		    	  break;
		      }
		    }
		//System.out.print(ec2ip);
//		if(server.equals("1"))
//		{
//			ec2ip = Config.ipServer1;
//		}
//		if(server.equals("2"))
//		{
//			ec2ip = Config.ipServer2;
//		}
//		if(server.equals("3"))
//		{
//			ec2ip = Config.ipServer3;
//		}
		if(os.equals("Ubuntu"))
		{
			try {
				hd.createContainer(cname,"sonvo123/os:ubuntu", ram, cpu, port,ec2ip ,info.getId());
			} catch (JSchException e) {
				e.printStackTrace();
			}
		}
		if(os.equals("Centos"))
		{
			try {
				hd.createContainer(cname,"sonvo123/os:centos", ram, cpu, port,ec2ip,info.getId());
			} catch (JSchException e) {
				e.printStackTrace();
			}
		}
		hd.insertCreate(cname, info.getId(), ram, cpu, port);
		resp.sendRedirect("home?server="+ server);
		//resp.sendRedirect("home?server=1");
	}
}