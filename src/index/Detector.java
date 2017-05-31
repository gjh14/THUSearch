package index;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

public class Detector {
	private static CodepageDetectorProxy detector;
	
	static{
		detector = CodepageDetectorProxy.getInstance();
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());
		detector.add(ASCIIDetector.getInstance());
        detector.add(UnicodeDetector.getInstance()); 
	}
	
	static public String fileCode(File file){
		try {
			URL url = file.toURI().toURL();
			Charset charset = detector.detectCodepage(url);
			return charset.name();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public String textCode(String text){
		try{
			InputStream in = new ByteArrayInputStream(text.getBytes());
			Charset charset = detector.detectCodepage(in, text.getBytes().length);
			return charset.name() != null ? charset.name() : "gbk";
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		System.out.println(Detector.textCode("ÄãºÃ"));
	}
}
