package pdf.html;

public enum CrawlingStateFactory {

	INSTANCE;

	public synchronized HtmlCrawler instance(String state) throws Exception {

		if ("40".equals(state)) {
			return new RegistState();
		} else if ("00".equals(state)) {
			return new FirstState(); // state = 00
		} else {
			throw new NotFindException("not Find Code[" + state + "] 00=initial, 40=saved");
		}
	}

	class NotFindException extends Exception {
		NotFindException(String s) {
			super(s);
		}
	}
}
