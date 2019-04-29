package com.woniucx.control;

import java.util.List;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.woniucx.core.CommonUtils;
import com.woniucx.model.Goods;
import com.woniucx.model.StoreSum;
import com.woniucx.model.Store;

public class StoreController extends Controller {

	public void index() {
		if (getSessionAttr("islogin") != "true") {
			redirect("/");
		}
		else {
			setAttr("batchname", getPara("batchname"));
			setAttr("goodsserial", getPara("goodsserial"));
			setAttr("goodsname", getPara("goodsname"));
			setAttr("unitprice", getPara("unitprice"));
			render("/page/store.html");
		}
	}
	
	// 查询批次信息，返回批次名称
	public void querybatch() {
		List<Goods> list = Goods.dao.find("select distinct batchname from goods order by batchname desc");
		renderJson(list);
	}
	
	// 通过进货批次和商品货号查询其售价和品名
	public void queryinfo() {
		String batchName = getPara("batchname");
		String goodsSerial = getPara("goodsserial");
		//List<Goods> list = Goods.dao.find("select goodsname,unitprice,barcode,goodstype from goods where batchname=? and goodsserial=?", batchName, goodsSerial);
		List<Goods> list = Goods.dao.find("select goodsname,unitprice,barcode,goodstype,inputsize,quantity from goods where goodsserial=?", goodsSerial);
		renderJson(list);
	}
	
	// 查询商品编码/货号
	public void queryserial() {
		String chars = getPara("query");
		String batchName = getPara("batchname");
		List<Goods> list = Goods.dao.find("select goodsserial from goods where goodsserial like '%" + chars + "%' and batchname='"+ batchName+"' limit 0,10");
		renderJson(list);
	}
	
	// 添加入库信息
	public void add() {
		String batchName = getPara("batchname");
		String goodsSerial = getPara("goodsserial");
		String barCode = getPara("barcode");
		String inputSize = getPara("inputsize");
		String goodsType = getPara("goodstype");
		int quantity = getParaToInt("quantity");
		String nowtime = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		
		String[] sizeArray = inputSize.split("-");
		
		// 根据批次和货号找到商品的goodsid
		int goodsId = -1;
		try {
			String sqlGoodsId = "select goodsid from goods where batchname=? and goodsserial=?"; 
			goodsId = Goods.dao.findFirst(sqlGoodsId, batchName, goodsSerial).getInt("goodsid");
		}
		catch (NullPointerException e) {
			renderText("失败：批次" + batchName + "下没有找到货号：" + goodsSerial + ".");
			return;
		}
		
		// 根据批次名称+商品货号来查找商品数量，如果与手数*inputSize解析出来的件数不匹配，则不作处理，返回提示信息
		Goods goods = Goods.dao.findById(goodsId);
		if (goods.getInt("quantity") != (quantity * sizeArray.length)) {
			renderText("失败：入库的商品总量（数量*尺码个数）与本批次的商品总数量不一致.");
			return;
		}
		// 如果商品的条码已经录入，说明该商品已经入库，不能重复入库
		else if (!goods.getStr("barcode").equals("0")) {
			renderText("失败：该批次商品已经完成入库，请勿重复录入.");
			return;
		}
		else {
			// 根据goodsid更新goods表的barcode
			boolean isUpdated = Goods.dao.findById(goodsId)
					.set("barcode", barCode)
					.set("goodstype", goodsType)
					.set("inputsize", inputSize).update();
		
			// 录入入库信息
			if (isUpdated) {
				// 将尺码信息及数量等录入store库存记录表
				for (int i=0; i<sizeArray.length; i++) {
					Store store = new Store();
					store.set("goodsid", goodsId);
					store.set("goodssize", sizeArray[i]);
					store.set("inputsize", inputSize);
					store.set("quantity", quantity);
					store.set("userid", getSessionAttr("userid"));
					store.set("createtime", nowtime);
					store.save();
					
					// 基于本次入库信息更新storesum库存数量表
					String sqlSum = "select storesumid,quantity,remained from storesum where barcode=? and goodsserial=? and goodssize=?";
					List<StoreSum> qtyList = StoreSum.dao.find(sqlSum, barCode, goodsSerial, sizeArray[i]);
					if (qtyList.size() < 1) {
						// 如果库存数量表中不存在相同的barcode和goodssize，则直接添加该条记录，表示初次入库
						StoreSum storeSum = new StoreSum();
						storeSum.set("barcode", barCode);
						storeSum.set("goodsserial", goodsSerial);
						storeSum.set("goodssize", sizeArray[i]);
						storeSum.set("quantity", quantity);
						storeSum.set("remained", quantity);
						storeSum.set("createtime", nowtime);
						storeSum.set("updatetime", nowtime);
						storeSum.save();
					}
					else {
						// 如果库存数量表中已经存在相同的barcode和goodssize，则直接修改数量
						int storesumId = qtyList.get(0).getInt("storesumid");
						int beforeQuantity = qtyList.get(0).getInt("quantity");
						int lastQuantity = beforeQuantity + quantity;
						int beforeRemained = qtyList.get(0).getInt("remained");
						int lastRemained = beforeRemained + quantity;
						StoreSum.dao.findById(storesumId)
							.set("quantity", lastQuantity)
							.set("remained", lastRemained)
							.set("updatetime", nowtime)
							.update();
					}
				}
				
				// 三表联合查询，返回本次入库关键信息
				String sqlQuery = "select g.goodsid, g.batchname, g.barcode, g.goodsserial, g.goodsname, "
						+ "s.inputsize, g.quantity, g.unitprice, u.realname from goods g, store s, user u "
						+ "where u.userid=s.userid and g.goodsid=s.goodsid and s.goodsid=? "
						+ "group by g.batchname and g.goodsserial";
				List<Record> list = Db.find(sqlQuery, goodsId);
				// 更新quantity这一列的值
				list.get(0).set("quantity", quantity + "手,共" + (sizeArray.length*quantity) + "件");
				renderJson(list);
			}
			else {
				renderText("失败：更新条码信息失败，请联系管理员进行手工处理.");
			}
		}
		
		// 两条有用的SQL
		/*
		 * select s.storeid,g.goodsid, s.goodssize, sum(s.quantity),g.barcode,g.goodsname from goods g, store s where s.goodsid=g.goodsid and g.barcode='0925' group by s.goodssize
		 * select goods.* from goods left join store on goods.goodsid=store.goodsid where store.goodsid is null
		 * 
		 */
	}
	
	// 查询库存时修改库存数量
	public void edit() {
		int storesumid = getParaToInt("storesumid");
		int remained = getParaToInt("remained");
		StoreSum ss = StoreSum.dao.findById(storesumid);
		ss.set("remained", remained);
		boolean isUpdated = ss.update();
		if (isUpdated) {
			renderText("edit-successful");
		}
		else {
			renderText("edit-failed");
		}
	}
}
