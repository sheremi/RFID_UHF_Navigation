package de.unierlangen.like.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.unierlangen.like.rfid.GenericTag;
import de.unierlangen.like.rfid.Reader;
import de.unierlangen.like.rfid.Reader.ReaderException;

public class TagsDatabase {
	
	HashMap<String, Float[]> hashMap = new HashMap<String, Float[]>();
	
	public TagsDatabase() {
		super();
	}
	//TODO change to private
	public HashMap<String, Float[]> createTagsHashMap(){
		//Map keys (EPC values) to objects of tags for navigation with specific coordinates

		hashMap.put("EBA123", new Float[]{18.37f,5.89f});
		hashMap.put("FA894", new Float[]{14.74f,7.03f});
		hashMap.put("BEEB111", new Float[]{22.00f,5.89f});
		hashMap.put("DEFFA321", new Float[]{26.00f,9.89f});
/**
 * 
 * for (GenericTag genericTag: reader.performRound()){
		if (tagsHashMap.containsKey(genericTag.getEpc())){
			Float[] coordinates = tagsHashMap.get(genericTag.getEpc());
			arrayOfTags.add(new Tag(genericTag, coordinates[0], coordinates[1]));
		}
 */
		return hashMap;
	}

	public Tag makeTag(GenericTag genericTag) {
		//TODO implement
		return new Tag(genericTag, 1, 1);
	}
	
	public ArrayList<Tag> getReadTags(Reader reader) throws IOException, ReaderException{
		ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
		for (GenericTag genericTag: reader.performRound()){
			if (hashMap.containsKey(genericTag.getEpc())){
				Float[] coordinates = hashMap.get(genericTag.getEpc());
				arrayOfTags.add(new Tag(genericTag, coordinates[0], coordinates[1]));
			}
		}	
		return arrayOfTags;
	}

}
