package de.unierlangen.like.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.rfid.GenericTag;

public class TagsDatabase {
    private final Logger log = Logger.getDefaultLogger();
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

        newTag = new TagOnTheWall(31.00f, 7.15f, "1");
        newTag.addEPC("308033b2ddd96400000");
        newTag.addEPC("b08033b2ddd96400000");

        newTag.addEPC("");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(24.68f, 5.77f, "2");
        newTag.addEPC("50033b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(35.00f, 5.77f, "3");
        newTag.addEPC("305fb63ac1f3841ec88467");
        tagsOnTheWall.add(newTag);

        // tags from Andreas
        newTag = new TagOnTheWall(33.40f, 8.25f, "4");
        newTag.addEPC("30833b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(35.10f, 7.15f, "5");
        newTag.addEPC("a1b133b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(37.60f, 5.77f, "6");
        newTag.addEPC("a2b233b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(40.00f, 7.15f, "7");
        newTag.addEPC("a3b333b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(40.50f, 5.77f, "8");
        newTag.addEPC("a4b433b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(42.20f, 5.77f, "9");
        newTag.addEPC("a5b533b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(44.00f, 7.15f, "10");
        newTag.addEPC("a6b633b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(54.00f, 5.20f, "11");
        newTag.addEPC("a7b733b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(48.00f, 5.77f, "12");
        newTag.addEPC("a8b833b2ddd96400000");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(49.50f, 5.77f, "13");
        newTag.addEPC("a9b933b2ddd96400000");
        tagsOnTheWall.add(newTag);

        // NFC tags
        newTag = new TagOnTheWall(49.50f, 5.77f, "Visa");
        newTag.addEPC("[104, -33, 84, 20]");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(33.34f, 22.0f, "lab");
        newTag.addEPC("[-36, 112, -119, 42]");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(41.05f, -5.44f, "NivesBerner");
        newTag.addEPC("[28, 8, 72, 5]");
        tagsOnTheWall.add(newTag);

        newTag = new TagOnTheWall(41.05f, -12.70f, "AndreasL");
        newTag.addEPC("[-116, -95, -119, 42]");
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
                log.d("found " + tagOnTheWall.label + " by epc " + genericTag.getEpc());
                arrayOfTags.add(new Tag(genericTag, tagOnTheWall.x, tagOnTheWall.y));
            } else {
                log.w("tag with EPC = " + genericTag.getEpc() + " is not in the DB!");
            }
        }
        return arrayOfTags;
    }
}
