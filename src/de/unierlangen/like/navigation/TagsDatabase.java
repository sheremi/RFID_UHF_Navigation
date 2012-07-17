package de.unierlangen.like.navigation;

import java.util.ArrayList;
import java.util.HashMap;

import de.unierlangen.like.rfid.GenericTag;

public class TagsDatabase {

    HashMap<String, Float[]> hashMap = new HashMap<String, Float[]>();

    public TagsDatabase() {
        super();
    }

    private HashMap<String, Float[]> createTagsHashMap() {
        // Map keys (EPC values) to objects of tags for navigation with specific
        // coordinates

        hashMap.put("204033b2dcd96c00000", new Float[] { 24.68f, 5.67f });
        hashMap.put("b08033b2ddd96c00000", new Float[] { 30.00f, 7.25f });
        hashMap.put("BE1", new Float[] { 24.68f, 5.67f });
        hashMap.put("BE2", new Float[] { 30.00f, 7.25f });
        hashMap.put("BE3", new Float[] { 34.50f, 5.67f });
        hashMap.put("BE4", new Float[] { 34.70f, 7.71f });
        hashMap.put("BE5", new Float[] { 41.27f, 7.25f });
        hashMap.put("BE6", new Float[] { 42.18f, 5.21f });
        hashMap.put("BE7", new Float[] { 43.09f, 7.25f });

        return hashMap;
    }

    public Tag makeTag(GenericTag genericTag) {
        // TODO implement makeTag in tagsdatabase
        return new Tag(genericTag, 1, 1);
    }

    public ArrayList<Tag> getTags(ArrayList<GenericTag> genericTags) {
        hashMap = createTagsHashMap();
        ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
        for (GenericTag genericTag : genericTags) {
            if (hashMap.containsKey(genericTag.getEpc())) {
                Float[] coordinates = hashMap.get(genericTag.getEpc());
                arrayOfTags.add(new Tag(genericTag, coordinates[0], coordinates[1]));
            }
        }
        return arrayOfTags;
    }

}
