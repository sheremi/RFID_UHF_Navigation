package de.unierlangen.like;

import java.util.ArrayList;

import de.unierlangen.like.rfid.GenericTag;

public class Intents {
    public static final String ACTION_READ_TAGS = "de.unierlangen.like.rfid.ReaderIntents.ACTION_READ_TAGS";
    /**
     * {@link ArrayList} of {@link GenericTag} is attached to the intent as an
     * extra with the key {@link #EXTRA_TAGS}
     */
    public static final String ACTION_TAGS = "de.unierlangen.like.rfid.ReaderIntents.ACTION_TAGS";
    public static final String EXTRA_TAGS = "EXTRA_TAGS";

    public static final String ACTION_SET_DESTINATION = "de.unierlangen.like.Intents.ACTION_SET_DESTINATION";
    public static final String EXTRA_DESTINATION = "EXTRA_DESTINATION";
    public static final String ACTION_TAGS_ON_WALLS = "de.unierlangen.like.Intents.ACTION_TAGS_ON_WALLS";
    public static final String ACTION_LOCATION_FOUND = "de.unierlangen.like.Intents.ACTION_LOCATION_FOUND";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";
    public static final String ACTION_ROUTE_FOUND = "de.unierlangen.like.Intents.ACTION_ROUTE_FOUND";
    public static final String ACTION_ZONES = "de.unierlangen.like.Intents.ACTION_ZONES";
    public static final String EXTRA_ROUTE = "EXTRA_ROUTE";
    public static final String EXTRA_ZONES = "EXTRA_ZONES";
    public static final String EXTRA_TAGS_ON_WALLS = "EXTRA_TAGS_ON_WALLS";
    public static final String ACTION_START_NAVIGATION = "de.unierlangen.like.Intents.ACTION_START_NAVIGATION";
}
