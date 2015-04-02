package com.moscrop.official.rss;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivon on 20/10/14.
 */
public class RSSItem implements Parcelable {

    public final long date;
    public final String title;
    public final String content;
    public final String preview;
    public final String[] tags;
    public final String url;
    public final String metadata;

    public RSSItem(Parcel in) {
        this(in.readLong(), in.readString(), in.readString(), in.readString(), in.createStringArray(), in.readString(), in.readString());
        //   date           title            content          preview          tags                    url              metadata
    }

    public RSSItem(long date, String title, String content, String[] tags, String url) {
        this(date, title, content, null, tags, url);
    }

    public RSSItem(long date, String title, String content, String preview, String[] tags, String url) {
        this(date, title, content, preview, tags, url, null);
    }

    public RSSItem(long date, String title, String content, String preview, String[] tags, String url, String metadata) {
        this.date = date;
        this.title = title;
        this.content = content;
        if (preview != null) {
            this.preview = preview;
        } else {
            this.preview = "";
        }
        this.tags = tags;
        this.url = url;
        if (metadata != null) {
            this.metadata = metadata;
        } else {
            this.metadata = generateDisplayMetaData(content);
        }
    }

    private String generateDisplayMetaData(String content) {

        Random random = new Random();
        int i = random.nextInt(4);

        String s = i + "";

        Pattern pattern = Pattern.compile("src=\\\"([^\\\"]+)");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String find = matcher.group(1);
            s += ",";
            s += find;
        }

        return s;
    }

    /** Implementations of Parcelable methods down below */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(date);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(preview);
        dest.writeStringArray(tags);
        dest.writeString(url);
        dest.writeString(metadata);
    }

    public static final Parcelable.Creator<RSSItem> CREATOR = new Parcelable.Creator<RSSItem>() {

        @Override
        public RSSItem createFromParcel(Parcel source) {
            return new RSSItem(source);
        }

        @Override
        public RSSItem[] newArray(int size) {
            return new RSSItem[size];
        }
    };
}
