package com.ivon.jsontagtool;

public class TagObject {
	
	public static final String EMPTY_FIELD = "@null";
	public static final String DEFAULT_ICON = "https://copy.com/orrImpof8iSJFJWt";
	
	public final String name;
	public final String id_author;
	public final String id_category;
	public final String icon_img;
	
	public TagObject(String name, String author, String category, String image) {
		if (isStringEmpty(name)) {
			throw new IllegalArgumentException("Name field cannot be empty!");
		} else {
			this.name = name;
		}
		
		if (isStringEmpty(author)) {
			this.id_author = EMPTY_FIELD;
		} else {
			this.id_author = author;
		} 
		
		if (isStringEmpty(category)) {
			this.id_category = EMPTY_FIELD;
		} else {
			this.id_category = category;
		}
		
		if (isStringEmpty(image)) {
			this.icon_img = DEFAULT_ICON;
		} else {
			this.icon_img = image;
		}
	}
	
	private boolean isStringEmpty(String s) {
		return s == null || s.replaceAll("\\s+","").equals("");
	}
}
