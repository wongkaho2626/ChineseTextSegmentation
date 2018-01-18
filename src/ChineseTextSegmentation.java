import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.huaban.analysis.jieba.WordDictionary;
import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

public class ChineseTextSegmentation {
	
	protected Dictionary dic;  
	
	public ChineseTextSegmentation(){
		System.setProperty("mmseg.dic.path", "data");  //這裡可以指定自訂詞庫  
	    dic = Dictionary.getInstance();  
	}
	
	protected Seg getSeg() {  
	    return new ComplexSeg(dic);  
	}  
	
	public String segWords(String txt, String wordSpilt) throws IOException {  
	    Reader input = new StringReader(txt);  
	    StringBuilder sb = new StringBuilder();  
	    Seg seg = getSeg();  
	    MMSeg mmSeg = new MMSeg(input, seg);  
	    Word word = null;  
	    boolean first = true;  
	    while((word=mmSeg.next())!=null) {  
	        if(!first) {  
	            sb.append(wordSpilt);  
	        }  
	        String w = word.getString();  
	        sb.append(w);  
	        first = false;        
	    }  
	    return sb.toString();  
	} 
	
	public static void main(String[] args) throws IOException, ParseException {		
		
		ChineseTextSegmentation chineseTextSegmentation = new ChineseTextSegmentation();
		
		//read Chinese stop words
		Scanner stopwordScanner = new Scanner(new File("chinese_sw.txt"));
		HashSet<String> stopwords = new HashSet<String>();
		while (stopwordScanner.hasNext()){
			stopwords.add(stopwordScanner.next());
		}
		stopwordScanner.close();

		//start do the Chinese text segmentation
		JSONParser parser = new JSONParser();
        JSONArray posts = (JSONArray) parser.parse(new FileReader("post.json"));
        for(Object object : posts) {
        		JSONObject post = (JSONObject) object;
        		String title = (String) post.get("title");
        		System.out.println(chineseTextSegmentation.segWords(title, " | "));
        }
	}
}
