package pdf.convert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

import pdf.common.SysProperty;
import pluto.lang.Tracer;

public class PdfInfomation {
	
	private final String postId;
	
	private Rectangle pageSize = PageSize.A4 ;
	private int top 	= 30;
	private int bottom 	= 30;
	private int left 	= 30;
	private int right 	= 30;
	
	private float viewSize = 10;
	private List<String> cssList 	= new ArrayList<String>();
	private String adminPassword 	= "humuson";

	private PdfInfomation(){ postId= null;}

	public PdfInfomation(String postId){
		this.postId = postId;
		String cssDirectory=SysProperty.PdfLocalHtmlPath.value()+ postId +"/css";
		registCssFileList(cssDirectory);
	}
	
	public void registCssFileList(String fontFilePath){
		try{
			File dirFile=new File(fontFilePath);
			File []fileList=dirFile.listFiles();
			for(File tempFile : fileList) {
				if(tempFile.isFile()) {
					cssList.add(tempFile.getParent()+System.getProperty("file.separator")+tempFile.getName());
				}
			}
		}catch(Exception e){
			Tracer.error("pdf css list add Exception so continue..." +e);
		}
	}
	
	public void size(Rectangle size, int top, int bottom, int left, int right){
		pageSize=size;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}
	
	public float getViewSize() {
		return viewSize;
	}

	public void setViewSize(float viewSize) {
		this.viewSize = viewSize;
	}

	public String getPostId() {
		return postId;
	}

	public Rectangle getPageSize() {
		return pageSize;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}
	
	public List<String> getCssList() {
		return cssList;
	}
	
	public byte[] getAdminPassword(){
		return adminPassword.getBytes();
	}

}
