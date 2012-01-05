package de.unierlangen.like.navigation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;

public class FileReader {
	
	/* Constructor */
	public FileReader() {
		super();
	}
	
	/* Public methods */
	public String getDataFromFile(String path) throws IOException{
		FileChannel inputChannel = new FileInputStream(path).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate((int)(inputChannel.size()*2));
		int size = inputChannel.read(buffer);
		buffer.flip();
		String content = new String(buffer.array(),0,size);
		inputChannel.close();
		return content;
	}
	
	public String[] splitStringContent (String content){
		// Create a pattern to match breaks
		Pattern oneRow = Pattern.compile(";\r\n");
		//Split input with the pattern
		String[] allRows = oneRow.split(content);
		return allRows;
	}
}
