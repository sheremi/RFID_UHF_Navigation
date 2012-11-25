package de.unierlangen.like.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.better.wakelock.Logger;

import de.unierlangen.like.rfid.GenericTag;

public class TagsDatabase {
    HashMap<String, TagOnTheWall> hashMap = new HashMap<String, TagOnTheWall>();

    private class TagOnTheWall {
        /** EPC of the tag */
        public List<String> epcs;
        /** X position of the tag */
        public float x;
        /** Y position of the tag */
        public float y;
        /** Label written on the tag */
        public String label;

        public TagOnTheWall(float x, float y, String label) {
            super();
            this.x = x;
            this.y = y;
            this.label = label;
            epcs = new ArrayList<String>();
        }

        public TagOnTheWall(String epc, float x, float y, String label) {
            this(x, y, label);
            epcs.add(epc);
        }

        public void addEPC(String epc) {
            epcs.add(epc);
        }
    }

    public TagsDatabase() {
        // Map keys (EPC values) to objects of tags for navigation with specific
        // coordinates
        ArrayList<TagOnTheWall> tagsOnTheWall = new ArrayList<TagsDatabase.TagOnTheWall>();
        TagOnTheWall newTag;

        newTag = new TagOnTheWall(30.00f, 7.25f, "1");
        newTag.addEPC("308033b2ddd96400000");
        newTag.addEPC("b08033b2ddd96400000");

        newTag.addEPC("");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(24.68f, 5.67f, "2");
        newTag.addEPC("50033b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(32.00f, 4.25f, "3");
        newTag.addEPC("305fb63ac1f3841ec88467");
        tagsOnTheWall.add(newTag);

        // Emulation tags
        tagsOnTheWall.add(new TagOnTheWall("BE1", 24.68f, 5.67f, ""));
        tagsOnTheWall.add(new TagOnTheWall("BE2", 30.00f, 7.25f, ""));
        tagsOnTheWall.add(new TagOnTheWall("BE3", 34.50f, 5.67f, ""));
        tagsOnTheWall.add(new TagOnTheWall("BE4", 34.70f, 7.71f, ""));
        tagsOnTheWall.add(new TagOnTheWall("BE5", 41.27f, 7.25f, ""));
        tagsOnTheWall.add(new TagOnTheWall("BE6", 42.18f, 5.21f, ""));
        tagsOnTheWall.add(new TagOnTheWall("BE7", 43.09f, 7.25f, ""));

        // populate the map
        for (TagOnTheWall tagOnTheWall : tagsOnTheWall) {
            for (String epc : tagOnTheWall.epcs) {
                hashMap.put(epc, tagOnTheWall);
            }
        }
    }

    public ArrayList<Tag> getTags(ArrayList<GenericTag> genericTags) {
        ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
        for (GenericTag genericTag : genericTags) {
            TagOnTheWall tagOnTheWall = hashMap.get(genericTag.getEpc());
            if (tagOnTheWall != null) {
                Logger.d("found " + tagOnTheWall.label + " by epc " + genericTag.getEpc());
                arrayOfTags.add(new Tag(genericTag, tagOnTheWall.x, tagOnTheWall.y));
            } else {
                Logger.w("tag with EPC = " + genericTag.getEpc() + " is not in the DB!");
            }
        }
        return arrayOfTags;
    }
}
