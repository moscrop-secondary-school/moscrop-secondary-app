package com.moscrop.official.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moscrop.official.util.JsonUtil;
import com.moscrop.official.util.Logger;
import com.moscrop.official.util.Preferences;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Owner on 9/27/2015.
 */
public class ParseCategoryHelper {

    public static final String TAG_LIST_JSON = "categories.json";

    public static class Category {
        public final String name;
        public final String id;

        public Category(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    private static JSONObject getRootJsonObject(Context context) throws IOException, JSONException {
        File file = new File(context.getFilesDir(), TAG_LIST_JSON);
        if (!file.exists()) {
            copyFromAssetsToInternalStorage(context);
            file = new File(context.getFilesDir(), TAG_LIST_JSON);
        }
        return JsonUtil.getJsonObjectFromFile(file);
    }

    private static void copyFromAssetsToInternalStorage(Context context) throws IOException {
        InputStream input = context.getAssets().open(TAG_LIST_JSON);

        // Path to the just created empty db
        String outFileName = context.getFileStreamPath(TAG_LIST_JSON).toString();

        // Open the empty db as the output stream
        OutputStream output = new FileOutputStream(outFileName);

        // Transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer))>0) {
            output.write(buffer, 0, length);
        }

        // Close the streams
        output.flush();
        output.close();
        input.close();
    }

    public static ParseObject[] getFilterCategories(Context context, String tag) {

        Logger.error("Getting filter categories for tag " + tag);
        try {
            Category[] categories = null;
            switch (tag) {
                case "All":
                    categories = getAllTags(context);
                    break;
                case "Subscribed":
                    categories = getSubscribedTags(context);
                    break;
                default:
                    Category category = findCategoryByName(context, tag);
                    if (category != null) {
                        categories = new Category[]{category};
                    }
                    break;
            }

            if (categories != null) {
                ParseObject[] filterObjects = new ParseObject[categories.length];
                for (int i = 0; i < categories.length; i++) {
                    filterObjects[i] = new ParseObject("Categories");
                    filterObjects[i].setObjectId(categories[i].id);
                }
                return filterObjects;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new ParseObject[] {};
    }

    public static Category findCategoryByName(Context context, String name) throws IOException, JSONException {
        JSONObject root = getRootJsonObject(context);
        JSONObject[] tags = JsonUtil.extractJsonArray(root.getJSONArray("tags"));
        for (JSONObject tag : tags) {
            if (tag.getString("name").equals(name)) {
                return new Category(
                        tag.getString("name"),
                        tag.getString("id")
                );
            }
        }
        return null;
    }

    public static Category[] getAllTags(Context context) throws IOException, JSONException {
        JSONObject root = getRootJsonObject(context);
        JSONObject[] tags = JsonUtil.extractJsonArray(root.getJSONArray("tags"));
        List<Category> categories = new ArrayList<>();
        for (JSONObject tag : tags) {
            if (!tag.getString("name").equals("Student Bulletin")) {
                categories.add(new Category(
                        tag.getString("name"),
                        tag.getString("id")
                ));
            }
        }
        return categories.toArray(new Category[categories.size()]);
    }

    public static String[] getAllTagNames(Context context) throws IOException, JSONException {
        Category[] categories = getAllTags(context);
        String[] names = new String[categories.length];
        for (int i=0; i<categories.length; i++) {
            names[i] = categories[i].name;
        }

        // Use custom comparator because we want
        // "Official" to remain at the top of the list
        Arrays.sort(names, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.equals("Official")) {
                    return Integer.MIN_VALUE;
                } else if (s2.equals("Official")) {
                    return Integer.MAX_VALUE;
                } else {
                    return s1.compareToIgnoreCase(s2);
                }
            }
        });

        return names;
    }

    public static Category[] getSubscribedTags(Context context) throws IOException, JSONException {
        Category[] allCategories = getAllTags(context);

        // Get a list of tags the user subscribed to
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> existingSelectedValuesSet = prefs.getStringSet(Preferences.Keys.TAGS, Preferences.Default.TAGS);
        String[] existingSelectedValues = existingSelectedValuesSet.toArray(new String[existingSelectedValuesSet.size()]);

        List<Category> validatedCategories = new ArrayList<>();
        for (String value : existingSelectedValues) {
            for (Category category : allCategories) {
                if (category.name.equals(value))
                    validatedCategories.add(category);
            }
        }

        Category[] subscribedCategories = validatedCategories.toArray(new Category[validatedCategories.size()]);

        return subscribedCategories;
    }

    public static String[] getSubscribedTagNames(Context context) throws IOException, JSONException {

        // Get a list of recognized tags
        String[] tagNamesArray = getAllTagNames(context);

        // Get a list of tags the user subscribed to
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> existingSelectedValuesSet = prefs.getStringSet(Preferences.Keys.TAGS, Preferences.Default.TAGS);
        String[] existingSelectedValues = existingSelectedValuesSet.toArray(new String[existingSelectedValuesSet.size()]);

        List<String> validatedSelectedValues = new ArrayList<String>();
        for (String value : existingSelectedValues) {
            if (Arrays.asList(tagNamesArray).contains(value)) {
                validatedSelectedValues.add(value);
            }
        }

        Collections.sort(validatedSelectedValues, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.equals("Official")) {
                    return Integer.MIN_VALUE;
                } else if (s2.equals("Official")) {
                    return Integer.MAX_VALUE;
                } else {
                    return s1.compareToIgnoreCase(s2);
                }
            }
        });

        String[] subscribedTags = new String[validatedSelectedValues.size()];
        for (int i=0; i<subscribedTags.length; i++) {
            subscribedTags[i] = validatedSelectedValues.get(i);
        }

        return subscribedTags;
    }

    private static final long CATEGORIES_LIST_UPDATE_MIN_WAIT = 5*60*1000;  // 5 minutes

    public static void downloadCategoriesList(final Context context, final Runnable endAction) {

        final SharedPreferences prefs = context.getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        long lastUpdate = prefs.getLong(Preferences.App.Keys.CATEGORIES_UPDATED_AT, Preferences.App.Default.CATEGORIES_UPDATED_AT);
        if (System.currentTimeMillis() - lastUpdate > CATEGORIES_LIST_UPDATE_MIN_WAIT) {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(Preferences.App.Keys.CATEGORIES_UPDATED_AT, System.currentTimeMillis());
            editor.apply();
Logger.log("updating categories");
            HashMap<String, Object> params = new HashMap<>();
            ParseCloud.callFunctionInBackground("getCategoriesLastUpdatedTime", params, new FunctionCallback<Long>() {
                @Override
                public void done(final Long millis, ParseException e) {
                    if (e == null) {
                        long lastVersion = prefs.getLong(Preferences.App.Keys.CATEGORIES_VERSION, Preferences.App.Default.CATEGORIES_VERSION);

                        // Only download tags if there is a newer version.
                        if (millis > lastVersion) {
                            ParseQuery<ParseObject> query = new ParseQuery<>("Categories");
                            query.orderByAscending("name");
                            query.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> list, ParseException e) {
                                    JSONObject root = new JSONObject();
                                    JSONArray tags = new JSONArray();
                                    for (ParseObject item : list) {
                                        try {
                                            JSONObject tag = new JSONObject();
                                            tag.put("name", item.getString("name"));
                                            tag.put("id", item.getObjectId());
                                            tags.put(tag);
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    try {
                                        root.put("tags", tags);
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }

                                    try {
                                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(TAG_LIST_JSON, Context.MODE_MULTI_PROCESS));
                                        outputStreamWriter.write(root.toString());
                                        outputStreamWriter.close();

                                        // Save updated time once new categories
                                        // are successfully written to file
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putLong(Preferences.App.Keys.CATEGORIES_VERSION, millis);
                                        editor.apply();

                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        }
                    }

                    endAction.run();
                }
            });

        } else {
            Logger.log("skipping update");
            endAction.run();
        }

    }
}
