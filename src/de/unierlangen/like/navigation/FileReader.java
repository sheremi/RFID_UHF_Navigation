package de.unierlangen.like.navigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import android.content.Context;

public class FileReader {

    /* Constructor */
    public FileReader() {
        super();
    }

    /* Public methods */
    public static ArrayList<String> getStringsFromAsset(Context context, String string)
            throws IOException {
        ArrayList<String> strings;
        InputStream inputStream = context.getAssets().open(string);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        CharBuffer buffer = CharBuffer.allocate(10000);
        int size = bufferedReader.read(buffer);

        buffer.flip();
        String content = new String(buffer.array(), 0, size);
        // Create a pattern to match breaks
        Pattern oneRow = Pattern.compile(";\r\n");
        // Split input with the pattern
        String[] allRows = oneRow.split(content);
        strings = new ArrayList<String>();
        strings.addAll(Arrays.asList(allRows));
        bufferedReader.close();
        return strings;
    }
}
