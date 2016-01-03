/*
 * Copyright (C) 2015 Ravi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ravi.apps.android.newsbytes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores the detailed information about a news story and implements the parcelable
 * interface to enable it to be passed to other activities through intents.
 */
public class News implements Parcelable {

    // Detailed news information.
    private final String mHeadline;
    private final String mSummary;
    private final String mUriStory;
    private final String mAuthor;
    private final String mDate;
    private final String mUriThumbnail;
    private byte[] mThumbnail;
    private final String mCaptionThumbnail;
    private final String mCopyrightThumbnail;
    private final String mUriPhoto;
    private byte[] mPhoto;
    private final String mCaptionPhoto;
    private final String mCopyrightPhoto;
    private int mIsFavorite;

    // Public constructor.
    public News(String headline, String summary, String uriStory, String author, String date,
                String uriThumbnail, byte[] thumbnail, String captionThumbnail,
                String copyrightThumbnail, String uriPhoto, byte[] photo, String captionPhoto,
                String copyrightPhoto, int isFavorite) {
        // Store the news story details data into respective member variables.
        mHeadline = headline;
        mSummary = summary;
        mUriStory = uriStory;
        mAuthor = author;
        mDate = date;
        mUriThumbnail = uriThumbnail;
        if(thumbnail != null) {
            mThumbnail = thumbnail;
        } else {
            mThumbnail = new byte[0];
        }
        mCaptionThumbnail = captionThumbnail;
        mCopyrightThumbnail = copyrightThumbnail;
        mUriPhoto = uriPhoto;
        if(photo != null) {
            mPhoto = photo;
        } else {
            mPhoto = new byte[0];
        }
        mCaptionPhoto = captionPhoto;
        mCopyrightPhoto = copyrightPhoto;
        mIsFavorite = isFavorite;
    }

    // Private constructor used to re-create the object from the parcel.
    private News(Parcel source) {
        // Extract the news story details from the parcel and store it into
        // the respective member variables.
        mHeadline = source.readString();
        mSummary = source.readString();
        mUriStory = source.readString();
        mAuthor = source.readString();
        mDate = source.readString();
        mUriThumbnail = source.readString();
        mThumbnail = new byte[source.readInt()];
        source.readByteArray(mThumbnail);
        mCaptionThumbnail = source.readString();
        mCopyrightThumbnail = source.readString();
        mUriPhoto = source.readString();
        mPhoto = new byte[source.readInt()];
        source.readByteArray(mPhoto);
        mCaptionPhoto = source.readString();
        mCopyrightPhoto = source.readString();
        mIsFavorite = source.readInt();
    }

    // Reference used to recreate the object from the parcel.
    public static final Parcelable.Creator<News> CREATOR =
            new Parcelable.Creator<News>() {
                @Override
                public News createFromParcel(Parcel source) {
                    return new News(source);
                }

                @Override
                public News[] newArray(int size) {
                    return new News[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the news story details into the parcel.
        dest.writeString(mHeadline);
        dest.writeString(mSummary);
        dest.writeString(mUriStory);
        dest.writeString(mAuthor);
        dest.writeString(mDate);
        dest.writeString(mUriThumbnail);
        if(mThumbnail != null) {
            dest.writeInt(mThumbnail.length);
            dest.writeByteArray(mThumbnail);
        } else {
            dest.writeInt(0);
            dest.writeByteArray(new byte[0]);
        }
        dest.writeString(mCaptionThumbnail);
        dest.writeString(mCopyrightThumbnail);
        dest.writeString(mUriPhoto);
        if(mPhoto != null) {
            dest.writeInt(mPhoto.length);
            dest.writeByteArray(mPhoto);
        } else {
            dest.writeInt(0);
            dest.writeByteArray(new byte[0]);
        }
        dest.writeString(mCaptionPhoto);
        dest.writeString(mCopyrightPhoto);
        dest.writeInt(mIsFavorite);
    }

    // Returns the headline.
    public String getHeadline() {
        return mHeadline;
    }

    // Returns the summary.
    public String getSummary() {
        return mSummary;
    }

    // Returns the uri for the story.
    public String getUriStory() {
        return mUriStory;
    }

    // Returns the author.
    public String getAuthor() {
        return mAuthor;
    }

    // Returns the date.
    public String getDate() {
        return mDate;
    }

    // Returns the uri for the thumbnail.
    public String getUriThumbnail() {
        return mUriThumbnail;
    }

    // Returns the thumbnail.
    public byte[] getThumbnail() {
        return mThumbnail;
    }

    // Returns the caption for the thumbnail.
    public String getCaptionThumbnail() {
        return mCaptionThumbnail;
    }

    // Returns the copyright for the thumbnail.
    public String getCopyrightThumbnail() {
        return mCopyrightThumbnail;
    }

    // Returns the uri for the photo.
    public String getUriPhoto() {
        return mUriPhoto;
    }

    // Returns the photo.
    public byte[] getPhoto() {
        return mPhoto;
    }

    // Returns the caption for the photo.
    public String getCaptionPhoto() {
        return mCaptionPhoto;
    }

    // Returns the copyright for the photo.
    public String getCopyrightPhoto() {
        return mCopyrightPhoto;
    }

    // Returns whether story is marked as favorite.
    public int getIsFavorite() {
        return mIsFavorite;
    }

    // Sets the thumbnail as a byte array.
    public void setThumbnailByteArray(byte[] thumbnail) {
        mThumbnail = thumbnail;
    }

    // Sets the photo as a byte array.
    public void setPhotoByteArray(byte[] photo) {
        mPhoto = photo;
    }

    // Sets whether news story is marked as favorite.
    public void setIsFavorite(int isFavorite) {
        mIsFavorite = isFavorite;
    }
}

