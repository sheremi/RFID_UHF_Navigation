package de.unierlangen.like.rfid;

import java.util.ArrayList;

public class ReaderIntents {
    public static final String ACTION_READ_TAGS = "de.unierlangen.like.rfid.ReaderIntents.ACTION_READ_TAGS";
    /**
     * {@link ArrayList} of {@link GenericTag} is attached to the intent as an
     * extra with the key {@link #EXTRA_TAGS}
     */
    public static final String ACTION_TAGS = "de.unierlangen.like.rfid.ReaderIntents.ACTION_TAGS";
    public static final String EXTRA_TAGS = "EXTRA_TAGS";

}
