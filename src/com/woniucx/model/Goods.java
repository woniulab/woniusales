package com.woniucx.model;

import com.jfinal.plugin.activerecord.Model;

public class Goods extends Model<Goods> {
	public static final Goods dao = new Goods().dao(); 
}
