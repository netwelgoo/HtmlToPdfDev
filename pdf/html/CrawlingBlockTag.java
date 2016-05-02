package pdf.html;

public enum CrawlingBlockTag {
	
	html("header,footer,section,"
		+ "article,aside,nav,"
		+ "canvas,audio,video,"
		+ "mark,time,meter,"
		+ "progress,caption"),
	
	css("background-img,"
			+ "bgcolor,"
			+ "border-style:dotted,"
			+ "border-style:dashed,"
			+ "border-style:double,"
			+ "border-style:solid,"
			+ "border-radius");
	
	private String tags;
	
	private CrawlingBlockTag(String tags){
		this.tags = tags;
	}
	
	public String value(){
		return tags;
	}
}
