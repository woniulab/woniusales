package com.woniucx.control;

import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.woniucx.model.Goods;

public class QueryController extends Controller {
	
	public void index() {
		if (getSessionAttr("islogin") != "true")
			redirect("/");
		else
			render("/page/query.html");
	}
	
	public void stored() {
		String goodsSerial = getPara("goodsserial");
		String goodsName = getPara("goodsname");
		String barCode = getPara("barcode");
		String goodsType = getPara("goodstype");
		String earlyStoreTime = getPara("earlystoretime");
		String lastStoreTime = getPara("laststoretime");
		int page = (getParaToInt("page")-1)*50;
		
		String sql = "select s.storesumid, s.goodsserial, s.barcode, g.goodsname, s.goodssize, g.unitprice, "
				+ "s.quantity, s.remained, s.createtime from storesum s, goods g where s.barcode=g.barcode ";

		if (goodsSerial.length() >= 1) {
			sql += "and s.goodsserial='" + goodsSerial + "' ";
		}
		if (goodsName.length() >= 1) {
			sql += "and g.goodsname like '%" + goodsName + "%' ";
		}
		if (barCode.length() >= 1) {
			sql += "and s.barcode='" + barCode + "' ";
		}
		if (goodsType.length() >= 1) {
			sql += "and g.goodstype='" + goodsType + "' ";
		}
		
		if (earlyStoreTime.length() >= 1 && lastStoreTime.length() < 1) {
			// 只有最早入库时间，单向条件
			sql += "and s.createtime>='" + earlyStoreTime + " 00:00:00' ";
		}
		if (earlyStoreTime.length() < 1 && lastStoreTime.length() >= 1) {
			// 只有最晚入库时间，单向条件
			sql += "and s.createtime<='" + lastStoreTime + " 23:59:59' ";
		}
		if (earlyStoreTime.length() >= 1 && lastStoreTime.length() >= 1) {
			// 只有最晚入库时间，单向条件
			sql += "and s.createtime>='" + earlyStoreTime + " 00:00:00' and s.createtime<='" + lastStoreTime + " 23:59:59' ";
		}
		
		sql += "group by s.barcode,s.goodssize order by storesumid limit " + page + ",50";;
		
		//System.out.println("库存查询的SQL语句为：" + sql);
		
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
	
	public void notstored() {
		int page = (getParaToInt("page")-1)*50;
		String sql = "select goodsid,batchname,goodsserial,goodsname,unitprice,quantity,createtime from goods where barcode='0' limit " + page + ",50";
		List<Goods> list = Goods.dao.find(sql);
		renderJson(list);
	}
	
	public void zerostored() {
		int page = (getParaToInt("page")-1)*50;
		String sql = "select s.storesumid, s.goodsserial, s.barcode, g.goodsname, s.goodssize, g.unitprice, "
				+ "s.quantity, s.remained, s.createtime from storesum s, goods g where s.barcode=g.barcode and s.remained=0"
				+ " limit " + page + ",50";
		List<Record> list = Db.find(sql);
		renderJson(list);
	}
}

