package pdf.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import sun.font.FontManager;

/**
 * Container of Font & FontFamily Info </br>
 * (Singleton pattern) 
 * 
 * @author pioneer(2016.02.11)
 */
public class FontContainer {
	
	private static FontContainer uniqueFontContainer = new FontContainer();
	
	public static Map<String, String> fontFileInfo;

	public static Map<String, List<String>> fontFamilyFileInfo; 
											 
	private static String fontFilePath;
	
	private FontContainer() {
		if(fontFileInfo == null){
			fontFileInfo = new HashMap<String, String>();
			fontFamilyFileInfo = new HashMap<String, List<String>>();
			registerFontList();
		}
    }

	public static FontContainer getInstance() {
        return uniqueFontContainer;
    }
	
	public static void init(Object o){
		Properties prop = (Properties)o;
		fontFilePath = prop.getProperty("font.file.path", "C:/Windows/Fonts/");
		registerFontList();
	}
	
	public static void registerFontList(){
		
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		
		for (Font font : fonts) {
			fontFileInfo.put(font.getFontName(), "폰트파일");
//			String fontFilePath = FontManager.getFontPath( true ) + "/" + FontManager.getFileNameForFontName(font.getFontName());
		}
	}
	
	/**
	 * saved only ttf or ttc file
	 *  
	 * @param fontFilePath
	 */
	public static void registerFontList(String fontFilePath){
		File dirFile=new File(fontFilePath);
		File []fileList=dirFile.listFiles();
		for(File tempFile : fileList) {
			if(tempFile.isFile()) {
			  String tempPath=tempFile.getParent();
			  String tempFileName=tempFile.getName();
			  if(tempFileName.toLowerCase().endsWith(".ttf") 
					  || tempFileName.toLowerCase().endsWith(".ttc"))
			  registerFontInfo(tempPath + System.getProperty("file.separator") + tempFileName);
			}
		}
	}
	
	public static void familyAdd(String key, String file){
		List<String> familyList = fontFamilyFileInfo.get(key);
		if(familyList == null){
			List<String> value = new ArrayList<String>();
			value.add(file);
			fontFamilyFileInfo.put(key, value);
		}else{
			familyList.add(file);
		}
	}
	
	public static void registerFontInfo(String fontFile){
		try {
			File f = new File(fontFile);
			System.out.println(f);
			
			InputStream inputStream = new FileInputStream(f);
			Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			
			if("굴림".equals(font.getName())){
				fontFileInfo.put("돋움", fontFile);
				fontFileInfo.put("돋움체", fontFile);
			}
			fontFileInfo.put(font.getName(), fontFile);
			fontFileInfo.put(font.getPSName(), fontFile);
			
			familyAdd(font.getFamily(), fontFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String fontFile(String alias){
		return fontFileInfo.get(alias);
	}
	
	public Map<String, String> fontFileInfo(){
		return fontFileInfo;
	}
	
	public static void main1(String[] args) {
		FontContainer fc = FontContainer.getInstance();
		fc.registerFontList("C:/Windows/Fonts/");
		Map<String, String> fontInfo = fc.fontFileInfo();
		
		Iterator iter = fontInfo.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next().toString();
			String value = fontInfo.get(key);
			
			System.out.println("Key="+key +" value="+value);
		}
	}
	
	
	public static void main(String[] args)
	  {
	    String fontName[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

	    for ( int i = 0; i < fontName.length; i++ )
	    {
	      System.out.println(fontName[i]);
	    }
	    System.out.println(">> =============================  >> ");
	    for (Font font : fonts) {
	    	
			System.out.println("font name["+font.getFontName()+"] "
					+ "file["+font.getAttributes()+"]");
		}
	  }


}
