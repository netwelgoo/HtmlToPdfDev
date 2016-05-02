package pdf.font;

import com.itextpdf.tool.xml.XMLWorkerFontProvider;

import pdf.common.SysProperty;

public enum ItextFontRegister {
	
	INSTANCE;
	
	final static XMLWorkerFontProvider fontProvider;
	static{
		fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
//		fontProvider.registerDirectory(Message.get("font.dir"));
		fontProvider.registerDirectory(SysProperty.PdfFontFilePath.value());
	}
	
	public XMLWorkerFontProvider fontProvider(){
		return fontProvider;
	}
}
