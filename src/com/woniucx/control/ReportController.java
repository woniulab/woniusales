package com.woniucx.control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.woniucx.core.CommonUtils;
import com.woniucx.model.Customer;
import com.woniucx.model.Sell;
import com.woniucx.model.SellSum;

public class ReportController extends Controller {

	public void index() {
		if (getSessionAttr("islogin") != "true")
			redirect("/");
		else
			render("/page/report.html");
	}
	
	public void totalselltoday() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select sum(totalprice) amount from sellsum where createtime>='"+startTime+"' and createtime <='"+endTime+"'";
		String totalSellAmount = SellSum.dao.findFirst(sql).getStr("amount");
		renderText(totalSellAmount + " 元");
	}
	
	public void totalsellweek() {
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DAY_OF_WEEK, -7);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String startTime = df.format(c.getTime());
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select sum(totalprice) amount from sellsum where createtime>='"+startTime+"' and createtime <='"+endTime+"'";
		String totalSellAmount = SellSum.dao.findFirst(sql).getStr("amount");
		renderText(totalSellAmount + " 元");
	}
	
	public void totalsellmonth() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select sum(totalprice) amount from sellsum where createtime>='"+startTime+"' and createtime <='"+endTime+"'";
		String totalSellAmount = SellSum.dao.findFirst(sql).getStr("amount");
		renderText(totalSellAmount + " 元");
	}
	
	public void totalsellyear() {
		String startTime = CommonUtils.generateDateTime("yyyy-01-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select sum(totalprice) amount from sellsum where createtime>='"+startTime+"' and createtime <='"+endTime+"'";
		String totalSellAmount = SellSum.dao.findFirst(sql).getStr("amount");
		renderText(totalSellAmount + " 元");
	}
	
	public void totalsellall() {
		String sql = "select sum(totalprice) amount from sellsum";
		String totalSellAmount = SellSum.dao.findFirst(sql).getStr("amount");
		renderText(totalSellAmount + " 元");
	}
	
	public void customercount() {
		String sql = "select count(*) counts from customer";
		String customerCount = Customer.dao.findFirst(sql).getStr("counts");
		renderText(customerCount + " 人");
	}
	
	public void customerbuyonce() {
		String sql = "select count(customerid) from sellsum group by customerid having count(customerid)=1";
		List<SellSum> list = SellSum.dao.find(sql);
		renderText(String.valueOf(list.size()) + " 人");
	}
	
	public void customerbuytwice() {
		String sql = "select count(customerid) from sellsum group by customerid having count(customerid)=2";
		List<SellSum> list = SellSum.dao.find(sql);
		renderText(String.valueOf(list.size()) + " 人");
	}
	
	public void customerbuymany() {
		String sql = "select count(customerid) from sellsum group by customerid having count(customerid)>2";
		List<SellSum> list = SellSum.dao.find(sql);
		renderText(String.valueOf(list.size()) + " 人");
	}
	
	public void myselltoday() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select sum(totalprice) amount from sellsum where userid=? and createtime>='"+startTime+"' and createtime <='"+endTime+"'";
		String mySellAmount = SellSum.dao.findFirst(sql, getSessionAttr("userid").toString()).getStr("amount");
		renderText(mySellAmount + " 元");
	}
	
	public void mysellmonth() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select sum(totalprice) amount from sellsum where userid=? and createtime>='"+startTime+"' and createtime <='"+endTime+"'";
		String mySellAmount = SellSum.dao.findFirst(sql, getSessionAttr("userid").toString()).getStr("amount");
		renderText(mySellAmount + " 元");
	}
	
	public void selldetailtoday() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid and s.createtime>='"+startTime+"' and s.createtime <='"+endTime+"' order by s.sellid desc";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	
	/*
	public void selldetailweek() {
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DAY_OF_WEEK, -7);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String startTime = df.format(c.getTime());
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid and s.createtime>='"+startTime+"' and s.createtime <='"+endTime+"' order by s.sellid desc";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	*/
	
	public void selldetailmonth() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid and s.createtime>='"+startTime+"' and s.createtime <='"+endTime+"' order by s.sellid desc";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	
	public void selldetailall() {
		int page = (getParaToInt("page")-1)*50;
		String sql = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid order by s.sellid desc limit " + page + ",50";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	
	public void sellsumtoday() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid and s.createtime>='"+startTime+"' and s.createtime<='"+endTime+"' order by s.sellsumid desc";
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
	
	/*
	public void sellsumweek() {
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DAY_OF_WEEK, -7);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String startTime = df.format(c.getTime());
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid and s.createtime>='"+startTime+"' and s.createtime<='"+endTime+"' order by s.sellsumid desc";
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
	*/
	
	public void sellsummonth() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid and s.createtime>='"+startTime+"' and s.createtime<='"+endTime+"' order by s.sellsumid desc";
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
	
	public void sellsumall() {
		int page = (getParaToInt("page")-1)*50;
		String sql = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid order by s.sellsumid desc limit " + page + ",50";
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
	
	public void selltypemonth() {
		String startTime = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select goodstype,sum(buyquantity) buyquantity,floor(sum(subtotal)) selltotal from sell where createtime>='"+startTime+"' and createtime<='"+endTime+"' group by goodstype";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	
	/*
	public void selltypeyear() {
		String startTime = CommonUtils.generateDateTime("yyyy-01-01 00:00:00");
		String endTime = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
		String sql = "select goodstype,sum(buyquantity) buyquantity,floor(sum(subtotal)) selltotal from sell where createtime>='"+startTime+"' and createtime<='"+endTime+"' group by goodstype";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	*/
	
	public void selltypeall() {
		String sql = "select goodstype,sum(buyquantity) buyquantity,floor(sum(subtotal)) selltotal from sell group by goodstype";
		List<Sell> list = Sell.dao.find(sql);
		renderJson(list);
	}
	
	public void returndetail() {
		int page = (getParaToInt("page")-1)*30;
		String sql = "select r.*, c.customerphone from `return` r left join customer c on r.customerid=c.customerid limit " + page + ",30";
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
}	
