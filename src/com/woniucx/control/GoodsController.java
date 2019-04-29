package com.woniucx.control;

import java.io.*;
import java.util.List;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.upload.UploadFile;
import com.woniucx.core.CommonUtils;
import com.woniucx.model.Goods;

public class GoodsController extends Controller {
	
	public void index() {
		if (getSessionAttr("islogin")!= "true")
			redirect("/");
		else if (!getSessionAttr("role").equals("admin"))
			renderHtml("<div style='font-size: 20px; color: red; margin: 100px auto; width: 500px; text-align: center;'>你不是管理员，无法进入批次导入.</div>");
		else {
			setAttr("batchName", "GB" + CommonUtils.generateDateTime("yyyyMMdd"));
			render("/page/goods.html");
		}
	}
	
	public void upload() {
		UploadFile uploadFile=this.getFile("batchfile", "../upload");
		
		String batchName = getPara("batchname");
		
		List<Goods> list1 = Goods.dao.find("select * from goods where batchname=?", batchName);
		if (list1.size() >= 1) {
			renderText("already-imported");
			return;
		}

		String filePath = uploadFile.getUploadPath();
		String fileName = uploadFile.getFileName();
		String newFileName = "GoodsList-" + batchName + ".xls";
		
		File file = new File(filePath + "/" + fileName);
		File newFile = new File(filePath + "/" + newFileName);
		System.out.println("Current Batch File Path:  " + newFile);
		file.renameTo(newFile);
		
		String sourceFile = filePath + "/" + newFileName;
		
		String[][] content = CommonUtils.readExcel(sourceFile);
		
		if (content == null) {
			renderText("format-error");
			return;
		}
		
		for (int i=0; i<content.length; i++) {
			Goods goods = new Goods();
			goods.set("batchname", batchName);
			goods.set("goodsserial", content[i][0]);
			goods.set("goodsname", content[i][1]);
			goods.set("quantity", content[i][2]);
			goods.set("unitprice", content[i][3]);
			goods.set("totalprice", content[i][4]);
			goods.set("costunitprice", content[i][5]);
			goods.set("costtotalprice", content[i][6]);
			goods.set("userid", getSessionAttr("userid"));
			goods.set("createtime", CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss"));
			goods.save();
		}
		
		List<Goods> list2 = Goods.dao.find("select * from goods where batchname=? order by goodsid", batchName);
		renderJson(list2);
	}
	
	public void querybatch() {
		String batchName = getPara("batchname");
		List<Goods> list = Goods.dao.find("select * from goods where batchname=? order by goodsid", batchName);
		renderJson(list);
	}
	
	public void deletebatch() {
		String batchName = getPara("batchname");
		String sql = "delete from goods where batchname=?";
		int result = Db.update(sql, batchName);
		if (result > 0) {
			renderText("delete-successful");
		}
		else {
			renderText("delete-failed");
		}
	}
	
	public void querygoods() {
		int goodsid = getParaToInt("goodsid");
		Goods goods = Goods.dao.findById(goodsid);
		renderJson(goods);
	}
	
	public void editgoods() {
		String nowtime = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		
		int goodsid = getParaToInt("goodsid");
		String goodsserial = getPara("goodsserial");
		String goodsname = getPara("goodsname");
		int quantity = getParaToInt("quantity");
		float unitprice = Float.parseFloat(getPara("unitprice"));
		float totalprice = Float.parseFloat(getPara("totalprice"));
		float costunitprice = Float.parseFloat(getPara("costunitprice"));
		float costtotalprice = Float.parseFloat(getPara("costtotalprice"));
		Goods goods = Goods.dao.findById(goodsid);
		goods.set("goodsserial", goodsserial);
		goods.set("goodsname", goodsname);
		goods.set("quantity", quantity);
		goods.set("unitprice", unitprice);
		goods.set("totalprice", totalprice);
		goods.set("costunitprice", costunitprice);
		goods.set("costtotalprice", costtotalprice);
		goods.set("createtime", nowtime);
		boolean isUpdated = goods.update();
		
		if (isUpdated) {
			renderText("edit-successful");
		}
		else {
			renderText("edit-failed");
		}
	}
	
	public void deletegoods() {
		int goodsid = getParaToInt("goodsid");
		boolean isDeleted = Goods.dao.findById(goodsid).delete();
		if (isDeleted) {
			renderText("delete-successful");
		}
		else {
			renderText("delete-failed");
		}
	}
}
