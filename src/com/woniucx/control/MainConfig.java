package com.woniucx.control;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.woniucx.model.Goods;
import com.woniucx.model.Return;
import com.woniucx.model.Sell;
import com.woniucx.model.SellSum;
import com.woniucx.model.StoreSum;
import com.woniucx.model.Store;
import com.woniucx.model.User;
import com.woniucx.model.Customer;

public class MainConfig extends JFinalConfig {
	
	@Override
	public void configConstant(Constants me) {
		PropKit.use("db.properties");
		me.setDevMode(true);
	}

	@Override
	public void configEngine(Engine me) {

	}

	@Override
	public void configHandler(Handlers me) {
		me.add(new ContextPathHandler("basePath"));
	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new SessionInViewInterceptor());
	}

	@Override
	public void configPlugin(Plugins me) {
		DruidPlugin dp = new DruidPlugin(PropKit.get("db_url"), PropKit.get("db_username"), PropKit.get("db_password"));
		me.add(dp);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp); me.add(arp);
		arp.addMapping("user", "userid", User.class);
		arp.addMapping("goods", "goodsid", Goods.class);
		arp.addMapping("customer", "customerid", Customer.class);
		arp.addMapping("store", "storeid", Store.class);
		arp.addMapping("storesum", "storesumid", StoreSum.class);
		arp.addMapping("sell", "sellid", Sell.class);
		arp.addMapping("sellsum", "sellsumid", SellSum.class);
		arp.addMapping("return", "returnid", Return.class);
	}

	@Override
	public void configRoute(Routes me) {
		me.setBaseViewPath("/");
		me.add("/", IndexController.class);
		me.add("/user", UserController.class);
		me.add("/sell", SellController.class);
		me.add("/store", StoreController.class);
		me.add("/goods", GoodsController.class);
		me.add("/customer", CustomerController.class);
		me.add("/query", QueryController.class);
		me.add("/report", ReportController.class);
	}

}
