import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Word;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luhuiguo.chinese.ChineseUtils;

public class ChineseTextSegmentation {
	
	protected Dictionary dic;  
	
	public ChineseTextSegmentation(){
		//set the dictionary path
		System.setProperty("mmseg.dic.path", "data");   
	    dic = Dictionary.getInstance();
	}
	
	public String segmentation(String sentence, HashSet<String> stopwords) throws IOException {
		Reader reader = new StringReader(sentence);  
	    StringBuilder stringBuilder = new StringBuilder();  
	    ComplexSeg seg = new ComplexSeg(dic);  
	    MMSeg mmSeg = new MMSeg(reader, seg);  
	    Word word = null;  
	    boolean first = true;  
	    while((word = mmSeg.next()) != null) {  
	        if(stopwords.contains(word.getString())) {
	        		break;
	        }
	        if(!first) {  
        			stringBuilder.append(" | ");  
	        }
	        stringBuilder.append(word.getString());
	        first = false;
	    }  
	    return stringBuilder.toString();  
	} 
	
	public static void main(String[] args) throws IOException, ParseException {		
		
		//read Chinese stop words
		Scanner stopwordScanner = new Scanner(new File("chinese_sw.txt"));
		HashSet<String> stopwords = new HashSet<String>();
		while (stopwordScanner.hasNext()){
			stopwords.add(stopwordScanner.next());
		}
		stopwordScanner.close();

		ChineseTextSegmentation chineseTextSegmentation = new ChineseTextSegmentation();

		//start do the Chinese text segmentation
		JSONParser parser = new JSONParser();
        JSONArray posts = (JSONArray) parser.parse(new FileReader("post.json"));
        for(Object object : posts) {
        		JSONObject post = (JSONObject) object;
        		String title = ChineseUtils.toTraditional((String) post.get("title"));
        		String titleAfterSegmentation = chineseTextSegmentation.segmentation(title, stopwords);
        	    System.out.println(titleAfterSegmentation);
        	    	post.put("title", titleAfterSegmentation);
        }
        
        File file = new File("postAfterChineseTextSegmentation.json");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(file, posts);
	}
	
}
