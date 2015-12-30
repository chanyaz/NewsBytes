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

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

/**
 * Displays detailed information about the news story.
 */
public class DetailsFragment extends Fragment {

    // Key used to get the news parcelable from bundle.
    public static final String NEWS_DETAILS = "News Details";

    // News object containing the details about this news story.
    private News mNews;

    // All the views in this fragment.
    private ImageView mPhoto;
    private TextView mCaption;
    private TextView mHeadline;
    private TextView mAuthor;
    private TextView mDate;
    private TextView mSummary;
    private Button mMarkAsFav;
    private Button mReadMore;

    // Reference to load target for picasso.
    private PicassoTarget mPicassoTarget = new PicassoTarget();

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout.
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        // Get the arguments for this fragment.
        Bundle arguments = getArguments();
        if(arguments != null) {
            // Extract the news object.
            mNews = (News) arguments.getParcelable(NEWS_DETAILS);
        }

        // Set references for all the views.
        setReferencesToViews(rootView);

        // Bind data to all the views.
        bindDataToView();

        // Set the mark as favorite button click listener.
//        mMarkAsFav.setOnClickListener(this);

        // Set the read more button click listener.
//        mReadMore.setOnClickListener(this);

        return rootView;
    }

    /**
     * Provides target for Picasso that loads the photo image from the url
     * and extracts and stores the photo image byte array.
     */
    private final class PicassoTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Set photo bitmap onto the image view.
            mPhoto.setImageBitmap(bitmap);

            // Get byte array from photo bitmap.
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            // Set the photo byte array in the news object.
            mNews.setPhotoByteArray(stream.toByteArray());
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }
    }

    /**
     * Sets the references to all the views.
     */
    private void setReferencesToViews(View rootView) {
        mPhoto = (ImageView) rootView.findViewById(R.id.photo_imageview);
        mCaption = (TextView) rootView.findViewById(R.id.caption_textview);
        mHeadline = (TextView) rootView.findViewById(R.id.headline_textview);
        mAuthor = (TextView) rootView.findViewById(R.id.author_textview);
        mDate = (TextView) rootView.findViewById(R.id.date_textview);
        mSummary = (TextView) rootView.findViewById(R.id.summary_textview);
        mMarkAsFav = (Button) rootView.findViewById(R.id.mark_favorite_button);
        mReadMore = (Button) rootView.findViewById(R.id.read_more_button);
    }

    /**
     * Binds the data to the corresponding views.
     */
    private void bindDataToView() {
        // Load the photo using Picasso.
        if(mNews.getUriPhoto() != null) {
            Picasso.with(getActivity())
                    .load(mNews.getUriPhoto())
                    .into(mPicassoTarget);
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the caption.
        if(mNews.getCaptionPhoto() != null && !mNews.getCaptionPhoto().isEmpty()) {
            mCaption.setText(mNews.getCaptionPhoto());
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the headline.
        if(mNews.getHeadline() != null && !mNews.getHeadline().isEmpty()) {
            mHeadline.setText(mNews.getHeadline());
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the author.
        if(mNews.getAuthor() != null && !mNews.getAuthor().isEmpty()) {
            mAuthor.setText(mNews.getAuthor());
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the date.
        if(mNews.getDate() != null && !mNews.getDate().isEmpty()) {
            mDate.setText(mNews.getDate());
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the summary.
        if(mNews.getSummary() != null && !mNews.getSummary().isEmpty()) {
            mSummary.setText(mNews.getSummary());
        } else {
            // TODO: Set appropriate error meassage.
        }

    }
}
