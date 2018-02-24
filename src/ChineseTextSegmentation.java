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
	private static final String COMMENT = "/Users/wongkaho/Eclipse Workspace/ChineseTextSegmentation/resource/comment";
	private static final String POST = "/Users/wongkaho/Eclipse Workspace/ChineseTextSegmentation/resource/post";

	
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
//	        if(stopwords.contains(word.getString())) {
//	        		break;
//	        }
	    		String w = word.getString();
	        if(!first) {  
		        	if (w.matches(".*[a-z].*")) { 
		        		stringBuilder.append("  | "); 
		        	}else {
		        		stringBuilder.append(" | ");  
		        	}
	        }
	        stringBuilder.append(w);
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

		//start do the Chinese text segmentation on post
		JSONParser parserPost = new JSONParser();
        JSONArray posts = (JSONArray) parserPost.parse(new FileReader(POST + ".json"));
        int cntPost = 0;
        for(Object object : posts) {
        		JSONObject post = (JSONObject) object;
        		String title = ChineseUtils.toTraditional(filterWord((String) post.get("title").toString().trim()));
        		String titleAfterSegmentation = chineseTextSegmentation.segmentation(title, stopwords);
        		cntPost++;
        		System.out.println(cntPost);
        	    	post.put("title", titleAfterSegmentation);
        }
                
        File filePost = new File("postAfterChineseTextSegmentation.json");
		ObjectMapper mapperPost = new ObjectMapper();
		mapperPost.writeValue(filePost, posts);
		
	    //start do the Chinese text segmentation on comment
		for(int i = 1500; i < 2727; i++) {
		JSONParser parserComment = new JSONParser();
        JSONArray comments = (JSONArray) parserComment.parse(new FileReader(COMMENT + i + ".json"));
        int cntComment = 0;
        for(Object object : comments) {
        		JSONObject comment = (JSONObject) object;
        		JSONArray contents = (JSONArray) comment.get("content");
        		JSONArray newContents = new JSONArray();
        		for(Object content : contents) {
        			String s = ChineseUtils.toTraditional(filterWord((String) content.toString().trim()));
        			String sAfterSegmentation = chineseTextSegmentation.segmentation(s, stopwords);
        			newContents.add(sAfterSegmentation);
        			cntComment++;
        			System.out.println(cntComment);
        		}
        		comment.put("content", newContents);
        }
        
        File fileComment = new File(COMMENT + "AfterChineseTextSegmentation" + i + ".json");
		ObjectMapper mapperComment = new ObjectMapper();
		mapperComment.writeValue(fileComment, comments);
		}
	}
	
	private static String filterWord(String input) {
		String output = input;
		if(input.contains(" [ 本帖最後由")) {
			int firstindexof = input.indexOf(" [ 本帖最後由");
			output = input.substring(0, firstindexof);
		}
		return output;
	}
	
}
