package com.moscrop.official.rss;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivon on 20/10/14.
 */
public class RSSItem implements Parcelable {

    public final String objectId;
    public final long date;
    public final String title;
    public final String[] categories;
    public final String icon;
    public final String bgImage;

    public RSSItem(Parcel in) {
        this(in.readString(), in.readLong(), in.readString(), in.createStringArray(), in.readString(), in.readString());
        //   objectId,        date           title            categories              icon             bgImage
    }


    public RSSItem(String objectId, long date, String title, String[] categories, String icon, String bgImage) {
        this.objectId = objectId;
        this.date = date;
        this.title = title;
        this.categories = categories;
        this.icon = icon;
        this.bgImage = bgImage;
    }

    /** Implementations of Parcelable methods down below */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(objectId);
        dest.writeLong(date);
        dest.writeString(title);
        dest.writeStringArray(categories);
        dest.writeString(icon);
        dest.writeString(bgImage);
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
