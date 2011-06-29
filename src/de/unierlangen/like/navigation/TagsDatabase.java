package de.unierlangen.like.navigation;

import java.util.HashMap;

import de.unierlangen.like.rfid.GenericTag;

public class TagsDatabase {
	
	public TagsDatabase() {
		super();
	}
	//TODO change to private
	public HashMap<String, Float[]> createTagsHashMap(){
		//Map keys (EPC values) to objects of tags for navigation with specific coordinates
		HashMap<String, Float[]> hashMap = new HashMap<String, Float[]>();
		//
		Float[] coordinates1 = {18.37f,5.89f};
		hashMap.put("EBA123", coordinates1);
		//
		Float[] coordinates2 = {14.74f,7.03f};
		hashMap.put("FA894", coordinates2);
		//
		Float[] coordinates3 = {22.00f,5.89f};
		hashMap.put("BEEF666", coordinates3);

		return hashMap;
		
	}
	
	public Tag makeTag(GenericTag genericTag) {
		//TODO implement
		return new Tag(genericTag, 1, 1);
	
	}

}
