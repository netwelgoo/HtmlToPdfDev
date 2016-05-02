package pdf.html;

import java.io.ByteArrayOutputStream;

/**
 * 기존 PostID가 등록되어 있지 않은 경우 불러오기 및 등록 작업
 * @author pioneer(2016. 2. 23.)
 *
 */
public class FirstState extends AbstractCrawlingState{

	@Override
	public boolean regist(HtmlInfo pdf) {
		try {
			changeRealDirectory(pdf);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		try {
//2. DB에 등록 한다.
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
}
