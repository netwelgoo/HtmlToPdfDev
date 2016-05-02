package pdf.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

import pdf.convert.ConverterHtmlToPdf;
import pluto.lang.Tracer;

public class CssExternalToInlineStyle {
	
	public String inlineStyles(String html, File cssFile, boolean removeClasses) 
	throws IOException, FileNotFoundException {
	    Document document = Jsoup.parse(html);
	    CSSOMParser parser = new CSSOMParser();
	    InputSource source = new InputSource(new FileReader(cssFile));
	    CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, null);

	    CSSRuleList ruleList = stylesheet.getCssRules();
	    Map<Element, Map<String, String>> allElementsStyles = new HashMap();
	    for (int ruleIndex = 0; ruleIndex < ruleList.getLength(); ruleIndex++) {
	        CSSRule item = ruleList.item(ruleIndex);
	        if (item instanceof CSSStyleRule) {
	            CSSStyleRule styleRule = (CSSStyleRule) item;
	            String cssSelector = styleRule.getSelectorText();
	            Elements elements = null; 
	            try {
	            	elements = document.select(cssSelector);
				} catch (Selector.SelectorParseException e) {
					Tracer.info("CSS extarnal to inline parser error. so contine Exception " +e);
					continue;
				}
	            for (int elementIndex = 0; elementIndex < elements.size(); elementIndex++) {
	                Element element = elements.get(elementIndex);
	                Map<String, String> elementStyles = allElementsStyles.get(element);
	                if (elementStyles == null) {
	                    elementStyles = new LinkedHashMap<String, String>();
	                    allElementsStyles.put(element, elementStyles);
	                }
	                CSSStyleDeclaration style = styleRule.getStyle();
	                for (int propertyIndex = 0; propertyIndex < style.getLength(); propertyIndex++) {
	                    String propertyName = style.item(propertyIndex);
	                    String propertyValue = style.getPropertyValue(propertyName);
	                    elementStyles.put(propertyName, propertyValue);
	                }
	            }
	        }
	    }

	    for (Map.Entry<Element, Map<String, String>> elementEntry : allElementsStyles.entrySet()) {
	        Element element = elementEntry.getKey();
	        StringBuilder builder = new StringBuilder();
	        for (Map.Entry<String, String> styleEntry : elementEntry.getValue().entrySet()) {
	            builder.append(styleEntry.getKey()).append(":").append(styleEntry.getValue()).append(";");
	        }
	        builder.append(element.attr("STYLE_ATTR"));
	        element.attr("STYLE_ATTR", builder.toString());
	        if (removeClasses) {
	            element.removeAttr("CLASS_ATTR");
	        }
	    }
	    System.out.println(">>>>>>>>>>>>>>>>>>>>>> NEW HTML >>>>>>>>>>>>>>>>>>>> ");
	    System.out.println(document.html());
	    return document.html();
	}
	
	/**
	* 
	* 
	* @param args
	*/
	public static void main(String[] args) throws Exception {
		String htmlContent = ConverterHtmlToPdf.htmlContent("C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/infos/20160331_4/20160331_4.html");
		
		new CssExternalToInlineStyle().inlineStyles(htmlContent,
				new File("C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/infos/20160331_4/css/style.css"),
				true
				);
	}
}
