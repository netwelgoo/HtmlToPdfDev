package pdf.font;

import java.util.List;

import com.itextpdf.tool.xml.XMLWorkerFontProvider;

/**
 * iText Font Register
 * @author pioneer(2016. 2. 11.)
 *
 */
public class ItextFontRegisterSample implements FontRegister<XMLWorkerFontProvider>{
	
	private ItextFontRegisterSample fontRegister = new ItextFontRegisterSample();
	
	
	@Override
	public void register(XMLWorkerFontProvider fontProvider, String fontName){
		FontContainer fc = FontContainer.getInstance();
		
		if(fc.fontFamilyFileInfo.containsKey(fontName)){
			List<String> family = fc.fontFamilyFileInfo.get(fontName);
			for(String font : family){
				fontProvider.register(font);
			}
		}else{
			String font = fc.fontFileInfo.get(fontName);
			fontProvider.register(font);
		}
	}

	@Override
	public void registers(XMLWorkerFontProvider fontProvider, List<String> fonts) {
		for (String font: fonts) {
			register(fontProvider, font);
		}
	}
	
	@Override
	public void registerDirectory(XMLWorkerFontProvider fontProvider, String dir) {
		fontProvider.registerDirectory(dir);
	}
}
