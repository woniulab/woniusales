package com.woniucx.control;

import java.util.List;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.woniucx.core.CommonUtils;
import com.woniucx.model.Customer;
import com.woniucx.model.Goods;

public class CustomerController extends Controller {

	public void index() {
		if (getSessionAttr("islogin") != "true")
			redirect("/");
		else
			render("/page/customer.html");
	}
	
	public void query() {
		String customerPhone = getPara("customerphone");
		if (customerPhone != "") {
			List<Customer> list = Customer.dao.find("select * from customer where customerphone=?", customerPhone);
			renderJson(list);
			return;
		}
	}
	
	public void phone() {
		String chars = getPara("query");
		List<Customer> list = Customer.dao.find("select customerphone from customer where customerphone like '%" + chars + "%' limit 0,10");
		renderJson(list);
	}
	
	public void add() {
		String customerName = getPara("customername");
		String customerPhone = getPara("customerphone");
		String childSex = getPara("childsex");
		String childDate = getPara("childdate");
		int creditKids = getParaToInt("creditkids");
		int creditCloth = getParaToInt("creditcloth");
		
		if (customerName.length() < 1) {
			customerName = "未知";
		}
		
		if (childDate.length() < 1) {
			childDate = "0000-00-00";
		}
		
		List<Customer> list = Customer.dao.find("select customerid from customer where customerphone=?", customerPhone);
		if (list.size() >= 1) {
			renderText("already-added");
			return;
		}
		
		String nowtime = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		
		Customer customer = new Customer();
		customer.set("customername", customerName);
		customer.set("customerphone", customerPhone);
		customer.set("childsex", childSex);
		customer.set("childdate", childDate);
		customer.set("creditkids", creditKids);
		customer.set("creditcloth", creditCloth);
		customer.set("credittotal", creditKids + creditCloth);
		customer.set("userid", getSessionAttr("userid"));
		customer.set("createtime", nowtime);
		customer.set("updatetime", nowtime);
		boolean isAdded = customer.save();
		
		if (isAdded) {
			renderText("add-successful");
			return;
		}
		else {
			renderText("add-failed");
			return;
		}
	}
	
	public void search() {
		String customerPhone = getPara("customerphone");
		int page = (getParaToInt("page")-1)*30;
		
		String sql = "select c.*, sum(s.totalprice) totals,count(s.customerid) counts from"
				+ " customer c left join sellsum s on c.customerid=s.customerid where c.customerphone like '%"
				+ customerPhone + "%' group by c.customerid limit " + page + ",30";
		
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
	
	public void edit() {
		String customerId = getPara("customerid");
		String customerPhone = getPara("customerphone");
		String customerName = getPara("customername");
		String childSex = getPara("childsex");
		String childDate = getPara("childdate");
		int creditKids = getParaToInt("creditkids");
		int creditCloth = getParaToInt("creditcloth");
		int creditTotal = creditKids + creditCloth;
		
		// 这是修改时存在的BUG，如果用户修改的是电话号码，则无法通过新号码找到之间的信息，所以应该直接将本修改项对应的customerId传输到此
		//int customerid = Customer.dao.findFirst("select customerid from customer where customerphone=?", customerPhone).getInt("customerid");
		boolean isUpdated = Customer.dao.findById(customerId)
							.set("customerphone", customerPhone)
							.set("customername", customerName)
							.set("childsex", childSex)
							.set("childdate", childDate)
							.set("creditkids", creditKids)
							.set("creditcloth", creditCloth)
							.set("credittotal", creditTotal).update();
		if (isUpdated) {
			renderText("edit-successful");
		}
		else {
			renderText("edit-failed");
		}
	}
}
