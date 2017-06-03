package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ViReader {
	ArrayList<String> urlList = new ArrayList<String>();
	ArrayList<String> strList = new ArrayList<String>();
	
	public ViReader(String filename) {
		File file = new File(filename);
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String linedata = null;
            int line = 0;
            
            while ((linedata = reader.readLine()) != null) {
            	++line;
            	
                String[] split = linedata.split("\t");
                if (split.length < 1) {
                	System.err.println("line " + line + ": " + linedata);
                	continue;
                }
                
                urlList.add(split[0]);
                
                //<a target="_blank" href="http://www.hehe.edu.cn/">´¹Ö±</a>
                String str = "";
                for (int i = 1; i < split.length; i += 2) {
                	str = str + "<a target=\"_blank\" href=\"http://" + split[i + 1] + "\">" + split[i] + "</a>\n";
                }
                strList.add(str);
                
                //System.out.println(split[0] + "\n" + str);
            }
            
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	e1.printStackTrace();
                }
            }
        }
	}
	
	public String get(String url) {
		for (int i = 0; i < urlList.size(); ++i)
			if (urlList.get(i).equals(url))
				return strList.get(i);
		return null;
	}
	
	public static void main(String argv[]) {
		ViReader a = new ViReader("D:/workspace/mirror__4/vi.txt");
		System.out.println(a.get("news.tsinghua.edu.cn/publish/thunews/index.html"));
	}
}
