package pdf.convert;

import java.util.Arrays;

public class PdfAudience {
	
	private final String emailAddress;
	
	private final String htmlContent;
	
	private final String postId;

	private final String password;

	private final String outPdfFileName;
	
	private byte[] pdfContent;
	
	private long createTime;
	
	private long takeTime;
	
	private boolean finished = false;
	
	private boolean success = true;
	
	private String resultMessage="OK";
	
	
	public PdfAudience( String postId, 
						String emailAddress, 
						String htmlContent,
						String outPdfFileName
			           )
	{
		this(postId, emailAddress, htmlContent, outPdfFileName, "");
	}
	
	public PdfAudience( String postId, 
			String emailAddress, 
			String htmlContent,
			String outPdfFileName,
			String password 		//password가 필요 없는 경우에는 ""를 넣어 줄 것.
			)
	{
	this.postId 		= postId;
	this.emailAddress 	= emailAddress;
	this.htmlContent 	= htmlContent;
	this.outPdfFileName = outPdfFileName;
	this.password 		= password;
	createTime 			= System.currentTimeMillis();
	}

	public String getPostId() {
		return postId;
	}

	public byte[] getPdfContent() {
		return pdfContent;
	}

	
	public String getPassword() {
		return password;
	}

	public void setPdfContent(byte[] pdfContent) {
		this.pdfContent = pdfContent;
	}


	public long getCreateTime() {
		return createTime;
	}


	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}


	public long getTakeTime() {
		return takeTime;
	}


	public void setTakeTime(long takeTime) {
		this.takeTime = takeTime - createTime;
	}


	public String getEmailAddress() {
		return emailAddress;
	}


	public String getHtmlContent() {
		return htmlContent;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		setTakeTime(System.currentTimeMillis());
		this.finished = finished;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	
	public String getOutPdfFileName() {
		return outPdfFileName;
	}

	@Override
	public String toString() {
		return "PdfAudience [emailAddress=" + emailAddress + ", htmlContent=" + htmlContent + ", postId=" + postId
				+ ", password=" + password + ", outPdfFileName=" + outPdfFileName + ", pdfContent="
				+ Arrays.toString(pdfContent) + ", createTime=" + createTime + ", takeTime=" + takeTime + ", finished="
				+ finished + ", success=" + success + ", resultMessage=" + resultMessage + "]";
	}

}
