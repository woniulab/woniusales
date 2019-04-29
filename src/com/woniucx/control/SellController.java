package com.woniucx.control;

import java.util.List;
import com.jfinal.core.Controller;
import com.woniucx.core.CommonUtils;
import com.woniucx.model.Customer;
import com.woniucx.model.Goods;
import com.woniucx.model.Return;
import com.woniucx.model.Sell;
import com.woniucx.model.SellSum;
import com.woniucx.model.StoreSum;

public class SellController extends Controller {
	
	public void index() {
		if (getSessionAttr("islogin") != "true")
			redirect("/");
		else
			render("/page/sell.html");
	}
	
	public void barcode() {
		String sqlCreditRatio = "select creditratio from sellsum order by sellsumid desc limit 0,1";
		String creditRatio = SellSum.dao.findFirst(sqlCreditRatio).getStr("creditratio");
		
		String sqlDiscountRatio = "select discountratio from sell order by sellid desc limit 0,1";
		String discountRatio = Sell.dao.findFirst(sqlDiscountRatio).getStr("discountratio");
		System.out.println(discountRatio);
		
		String barCode = getPara("barcode");
		List<Goods> listGoods = Goods.dao.find("select barcode,goodsserial,goodsname,unitprice,createtime from goods where barcode=? order by goodsid desc limit 0,1", barCode);
		if (listGoods.size() > 0) {
			List<StoreSum> listSum = StoreSum.dao.find("select * from storesum where barcode=?", barCode);
			String goodssizeList = ""; 
			for (int i=0; i<listSum.size(); i++) {
				String goodsSize = listSum.get(i).getStr("goodssize");
				int remained = listSum.get(i).getInt("remained");
				goodssizeList += "<option value='" + goodsSize +"'>尺码:" + goodsSize + ",剩余:" + remained + "件</option>";
			}
			listGoods.get(0).set("createtime",goodssizeList + "##" + creditRatio + "##" + discountRatio);
			renderJson(listGoods);
		}
		else {
			renderJson("[]");
		}
	}
	
	// 更新销售汇总数据表，并将其sellsumid返回，以供销售明细表sell使用 
	public void summary() {
		if (getSessionAttr("islogin") != "true") {
			redirect("/");
			return;
		}
		
		String nowTime = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		
		String customerPhone = getPara("customerphone");
		String payMethod = getPara("paymethod");
		String totalPrice = getPara("totalprice");
		String creditRatio = getPara("creditratio");
		int oldCredit = getParaToInt("oldcredit");
		int creditSum = getParaToInt("creditsum");
		String ticketType = getPara("tickettype");
		String ticketSum = getPara("ticketsum");
		
		// 根据客户的电话号码查找到其customerid并更新到sellsum表中
		String sqlCustomerId = "select customerid,creditcloth,credittotal from customer where customerphone=?";
		int customerId;
		try {
			Customer customer = Customer.dao.findFirst(sqlCustomerId, customerPhone);
			customerId = customer.getInt("customerid");
			int creditTotal = customer.getInt("credittotal") + creditSum;
			int creditCloth = customer.getInt("creditcloth") + creditSum;
			// 更新该客户的积分
			Customer.dao.findById(customerId)
				.set("credittotal", creditTotal)
				.set("creditcloth", creditCloth)
				.set("updatetime", nowTime).update();
		}
		catch (Exception e) {
			// 如果没有找到该客户信息，则新增该客户信息
			Customer customer = new Customer();
			customer.set("customerphone", customerPhone);
			customer.set("customername", "未知");
			customer.set("childsex", "男");
			customer.set("childdate", CommonUtils.generateDateTime("yyyy-MM-dd"));
			customer.set("creditkids", 0);
			customer.set("creditcloth", oldCredit + creditSum);
			customer.set("credittotal", oldCredit + creditSum);
			customer.set("userid", getSessionAttr("userid"));
			customer.set("createtime", nowTime);
			customer.set("updatetime", nowTime);
			customer.save();
			customerId = customer.getInt("customerid");
			
			// 保存最后一次的初始奖励积分
			setSessionAttr("initcredit", oldCredit);
		}
				
		SellSum sellSum = new SellSum();
		sellSum.set("customerid", customerId);
		sellSum.set("userid", getSessionAttr("userid"));
		sellSum.set("paymethod", payMethod);
		sellSum.set("totalprice", totalPrice);
		sellSum.set("creditratio", creditRatio);
		sellSum.set("creditsum", creditSum);
		sellSum.set("tickettype", ticketType);
		sellSum.set("ticketsum", ticketSum);
		sellSum.set("createtime", nowTime);
		sellSum.save();
		
		String sellSumId = String.valueOf(sellSum.getInt("sellsumid"));
		renderText(sellSumId);
	}
	
	// 更新销售明细表sell，该表保存每一笔商品出库记录
	public void detail() {
		String nowTime = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		
		String sellSumId = getPara("sellsumid");
		SellSum sellSum = SellSum.dao.findFirst("select customerid from sellsum where sellsumid=?",sellSumId);
		String customerId = sellSum.getStr("customerid");
		
		String barCode = getPara("barcode");
		String goodsSerial = getPara("goodsserial");
		String goodsName = getPara("goodsname");
		String goodsSize = getPara("goodssize");
		String unitPrice = getPara("unitprice");
		String discountRatio = getPara("discountratio");
		String discountPrice = getPara("discountprice");
		int buyQuantity = getParaToInt("buyquantity");
		String subTotal = getPara("subtotal");
		
		// 根据商品信息查询商品分类
		List<Goods> list = Goods.dao.find("select goodstype from goods where barcode=? and goodsserial=?", barCode, goodsSerial);
		String goodsType = list.get(0).getStr("goodstype");
		
		Sell sell = new Sell();
		sell.set("sellsumid", sellSumId);
		sell.set("customerid", customerId);
		sell.set("barcode", barCode);
		sell.set("goodsserial", goodsSerial);
		sell.set("goodsname", goodsName);
		sell.set("goodstype", goodsType);
		sell.set("goodssize", goodsSize);
		sell.set("unitprice", unitPrice);
		sell.set("discountratio", discountRatio);
		sell.set("discountprice", discountPrice);
		sell.set("buyquantity", buyQuantity);
		sell.set("subtotal", subTotal);
		sell.set("createtime", nowTime);
		sell.save();
		
		// 同步更新库存数量表storesum
		String sqlStoreSum = "select storesumid,quantity,remained from storesum where barcode=? and goodsserial=? and goodssize=?";
		StoreSum storeSum = StoreSum.dao.findFirst(sqlStoreSum, barCode, goodsSerial, goodsSize);
		int storeSumId = storeSum.getInt("storesumid");
		// 这里曾经存在一个低级Bug，用quantity减去buyQuantity，库存永远都只会减少1
		// int lastQuantity = storeSum.getInt("quantity") - buyQuantity;
		int lastQuantity = storeSum.getInt("remained") - buyQuantity;
		StoreSum.dao.findById(storeSumId).set("remained", lastQuantity).set("updatetime", nowTime).update();
		
		renderText("pay-successful");
	}
	
	// 退货确认信息查询
	public void prereturn() {
		int sellId = getParaToInt("sellid");
		Sell sell = Sell.dao.findById(sellId);
		float subTotal = sell.getFloat("subtotal");
		int sellSumId = sell.getInt("sellsumid");
		SellSum sellSum = SellSum.dao.findById(sellSumId);
		int totalPrice = sellSum.getInt("totalprice");
		int creditSum = sellSum.getInt("creditsum");
		int ticketSum = sellSum.getInt("ticketsum");
		
		// 计算本次退货的优惠金额 [( 小计金额 /（实收款+优惠金额）) * 优惠金额 ]
		int returnTicket = (int)((subTotal / (totalPrice + ticketSum)) * ticketSum);
		// 计算本次退货的积分扣除 [( 小计金额 /（实收款+优惠金额）) * 积分总数]
		int returnCredit = (int)((subTotal / (totalPrice + ticketSum)) * creditSum);
		// 计算本次退货的实际退还金额 （货品小计 - 优惠金额)
		int returnMoney = (int)subTotal - returnTicket;
		
		renderText(returnCredit + "#" + returnTicket + "#" + returnMoney);
	}
	
	// 确认退货
	public void doreturn() {
		String nowTime = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		
		int sellId = getParaToInt("sellid");
		String goodsSerialFromPara = getPara("goodsserial");
		
		// 先查询出Sell表的关键数据
		Sell sell = Sell.dao.findById(sellId);
		if (sell == null) {
			renderText("goodsinfo-error");
			return;
		}
		
		int sellSumId = sell.getInt("sellsumid");
		int customerId = sell.getInt("customerid");
		float subTotal = sell.getFloat("subtotal");
		String barCode = sell.getStr("barcode");
		String goodsSerial = sell.getStr("goodsserial");
		String goodsName = sell.getStr("goodsname");
		String goodsType = sell.getStr("goodstype");
		String goodsSize = sell.getStr("goodssize");
		float unitPrice = sell.getFloat("unitprice");
		float discountRatio = sell.getFloat("discountratio");
		float discountPrice = sell.getFloat("discountprice");
		int buyQuantity = sell.getInt("buyquantity");
		String createTime = sell.getStr("createtime");
		
		// 简单对比一下销售编号和前端传过来的商品货号是否匹配
		if (!goodsSerial.equals(goodsSerialFromPara)) {
			renderText("goodsinfo-error");
			return;
		}
		
		SellSum sellSum = SellSum.dao.findById(sellSumId);
		int totalPrice = sellSum.getInt("totalprice");
		int creditSum = sellSum.getInt("creditsum");
		int ticketSum = sellSum.getInt("ticketsum");
		
		// 计算本次退货的优惠金额 [( 小计金额 /（实收款+优惠金额）) * 优惠金额 ]
		int returnTicket = (int)((subTotal / (totalPrice + ticketSum)) * ticketSum);
		// 计算本次退货的积分扣除 [( 小计金额 /（实收款+优惠金额）) * 积分总数]
		int returnCredit = (int)((subTotal / (totalPrice + ticketSum)) * creditSum);
		// 计算本次退货的实际退还金额 （货品小计 - 优惠金额)
		int returnMoney = (int)subTotal - returnTicket;
		
		// 先将Sell表的本条销售记录复制到Return表
		Return myreturn = new Return();
		myreturn.set("sellid", sellId);
		myreturn.set("sellsumid", sellSumId);
		myreturn.set("customerid", customerId);
		myreturn.set("barcode", barCode);
		myreturn.set("goodsserial", goodsSerial);
		myreturn.set("goodsname", goodsName);
		myreturn.set("goodstype", goodsType);
		myreturn.set("goodssize", goodsSize);
		myreturn.set("unitprice", unitPrice);
		myreturn.set("discountratio", discountRatio);
		myreturn.set("discountprice", discountPrice);
		myreturn.set("buyquantity", buyQuantity);
		myreturn.set("subtotal", subTotal);
		myreturn.set("returnticket", returnTicket);
		myreturn.set("returncredit", returnCredit);
		myreturn.set("returnmoney", returnMoney);
		myreturn.set("selltime", createTime);
		myreturn.set("createtime", nowTime);
		boolean isSaved = myreturn.save();
		
		// 当退货信息成功保存到Return表后，更新相应的记录
		if (isSaved) {
			// 删除Sell表的本条销售记录
			boolean isDeleted = Sell.dao.deleteById(sellId);
			if (isDeleted) {
				// 将库存中对应的记录复原，将库存加回销售数量
				String sqlStore = "select * from storesum where barcode=? and goodsserial=? and goodssize=?";
				StoreSum storeSum = StoreSum.dao.findFirst(sqlStore, barCode, goodsSerial, goodsSize);
				if (storeSum != null) {
					int remained = storeSum.getInt("remained");
					storeSum.set("remained", remained + buyQuantity).set("updatetime", nowTime).update();
				}
				else {
					renderText("goodsinfo-error");
					return;
				}
				
				// 将SellSum中的销售摘要中的金额，积分等进行扣除
				sellSum.set("totalprice", totalPrice - returnMoney);
				sellSum.set("creditsum", creditSum - returnCredit);
				sellSum.set("ticketsum", ticketSum - returnTicket);
				sellSum.update();
				
				// 将Customer表中的积分进行同步扣除
				Customer customer = Customer.dao.findById(customerId);
				int creditCloth = customer.getInt("creditcloth");
				int creditTotal = customer.getInt("credittotal");
				customer.set("creditcloth", creditCloth - returnCredit);
				customer.set("credittotal", creditTotal - returnCredit);
				customer.update();
			}
			renderText("return-successful");
		}
		else {
			renderText("return-failed");
			return;
		}
	}
}
