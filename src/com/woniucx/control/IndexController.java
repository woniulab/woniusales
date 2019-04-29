package com.woniucx.control;

import java.util.List;

import com.jfinal.core.Controller;
import com.woniucx.model.User;

public class IndexController extends Controller {
	
	public void index() {
		if (getSessionAttr("islogin") != "true") {
			String username = getCookie("username");
			String password = getCookie("password");
			
			// 如果用户名在Cookie中存在，则深度直接登录
			if (username != null && username.length() > 0) {
				String sql = "select * from user where username=? and password=?";
				List<User> list = User.dao.find(sql, username, password);
				if (list.size() == 1) {  // 登录成功
					setSessionAttr("islogin", "true");
					setSessionAttr("userid", list.get(0).getInt("userid"));
					setSessionAttr("username", list.get(0).getStr("username"));
					setSessionAttr("realname", list.get(0).getStr("realname"));
					setSessionAttr("role", list.get(0).getStr("role"));
					
					render("/page/sell.html");
				}
				else {
					render("/page/index.html");
				}
			}
			else {
				render("/page/index.html");
			}
		}
		else {
			render("/page/sell.html");
		}
	}
	
	public void vcode() {
		renderCaptcha();
	}
	
	public void init() {
		renderText("");
	}
}
