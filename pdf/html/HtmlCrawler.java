package pdf.html;

import java.io.ByteArrayOutputStream;

public interface HtmlCrawler {

	/**
	 * @param url
	 *            web site
	 * @return create Key & create directory </br>
	 *         "call regist()" Key Directory searching 활용
	 */
	public void htmlLoad(HtmlInfo pdf); // request

	public boolean regist(HtmlInfo pdf);
	
	public HtmlCrawled result();
	
	public void createPdf(ByteArrayOutputStream pdfStream);
}
