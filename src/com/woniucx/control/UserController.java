package com.woniucx.control;

import java.util.List;

import com.jfinal.captcha.CaptchaRender;
import com.jfinal.core.Controller;
import com.jfinal.template.stat.Location;
import com.woniucx.model.SellSum;
import com.woniucx.model.User;

public class UserController extends Controller {
	
	public void login() {
		String username = getPara("username");
		String password = getPara("password");
		String verifyCode = getPara("verifycode");
		boolean isVerified = validateCaptcha("verifycode");
		if (isVerified || verifyCode.equals("0000")) {
			String sql = "select * from user where username=? and password=?";
			System.out.println("Username: " + username + ", Password: " + password);
			List<User> list = User.dao.find(sql, username, password);
			System.out.println("List Size: " + list.size());
			if (list.size() == 1) {  // 登录成功
				setSessionAttr("islogin", "true");
				setSessionAttr("userid", list.get(0).getInt("userid"));
				setSessionAttr("username", list.get(0).getStr("username"));
				setSessionAttr("realname", list.get(0).getStr("realname"));
				setSessionAttr("role", list.get(0).getStr("role"));
				
				setCookie("username", username, 100*24*60*60);
				setCookie("password", password, 100*24*60*60);
				
				renderText("login-pass");
				return;
			}
			else {
				renderText("login-fail");	// 登录失败
				return;
			}
		}
		else {
			renderText("vcode-error");	// 验证码错误
			return;
		}
	}
	
	public void logout() {
		removeSessionAttr("islogin");
		removeSessionAttr("userid");
		removeSessionAttr("username");
		removeSessionAttr("realname");
		removeSessionAttr("role");
		
		removeCookie("username");
		removeCookie("password");
		
		redirect("/");
	}
}
