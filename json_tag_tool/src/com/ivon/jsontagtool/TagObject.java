package com.ivon.jsontagtool;

public class TagObject {
	
	public static class InvalidNameException extends RuntimeException {
		private static final long serialVersionUID = -7461375040342668295L;
		public InvalidNameException() { super(); }
		public InvalidNameException(String message) { super(message); }
		public InvalidNameException(String message, Throwable cause) { super(message, cause); }
		public InvalidNameException(Throwable cause) { super(cause); }
		protected InvalidNameException(String message, Throwable cause,
                boolean enableSuppression,
                boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
	
	public static class InvalidCriteriaException extends RuntimeException {
		private static final long serialVersionUID = 1068142240712211856L;
		public InvalidCriteriaException() { super(); }
		public InvalidCriteriaException(String messokay howage) { super(message); }
		public InvalidCriteriaException(String message, Throwable cause) { super(message, cause); }
		public InvalidCriteriaException(Throwable cause) { super(cause); }
		protected InvalidCriteriaException(String message, Throwable cause,
                boolean enableSuppression,
                boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
	
	public static final String EMPTY_FIELD = "@null";
	public static final String DEFAULT_ICON = "moscrop_app";
	
	public final String name;
	public final String id_author;
	public final String id_category;
	public final String icon_img;
	
	public TagObject(String name, String author, String category, String image) {
		if (isStringEmpty(name)) {
			throw new InvalidNameException("Name field cannot be empty!");
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
			this.icon_img = completeIconGithubUrl(DEFAULT_ICON);
		} else {
			this.icon_img = completeIconGithubUrl(image);
		}
		completeIconGithubUrl(image);
		
		if (this.id_author.equals(EMPTY_FIELD) && this.id_category.equals(EMPTY_FIELD)) {
			throw new InvalidCriteriaException("You must specify either a name or category to match for!");
		}
	}
	
	private static String completeIconGithubUrl(String iconName) {
		return "https://raw.githubusercontent.com/IvonLiu/moscrop-secondary-app/master/moscropOnline/clubs/"
				+ iconName
				+ ".png";
	}
	
	private boolean isStringEmpty(String s) {
		return s == null || s.replaceAll("\\s+","").equals("");
	}
}
