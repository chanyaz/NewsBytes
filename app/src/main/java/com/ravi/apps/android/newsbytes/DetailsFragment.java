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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.apps.android.newsbytes.service.AddFavoriteService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

/**
 * Displays detailed information about the news story.
 */
public class DetailsFragment extends Fragment implements View.OnClickListener {

    // Tag for logging messages.
    private static final String LOG_TAG = HeadlinesFragment.class.getSimpleName();

    // Tag used to identify this fragment.
    public static final String DETAILS_FRAGMENT_TAG = "details_fragment_tag";

    // Key used to get the news parcelable from bundle.
    public static final String NEWS_DETAILS = "news_details";

    // Key used to pass the favorite news story data to the intent service.
    public static final String NEWS_FAVORITE = "news_favorite";

    // Key used to save news story object upon configuration change.
    public static final String NEWS_KEY = "news_key";

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
    private FloatingActionButton mShare;

    // Reference to thumbnail target for picasso.
    private ThumbnailTarget mThumbnailTarget = new ThumbnailTarget();

    // Reference to photo target for picasso.
    private PhotoTarget mPhotoTarget = new PhotoTarget();

    // Reference to share action provider.
    private ShareActionProvider mShareActionProvider;

    public DetailsFragment() {
        setHasOptionsMenu(true);
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

        // Check if it was a configuration change.
        if(savedInstanceState != null) {
            // Retrieve the saved news story object.
            if(savedInstanceState.containsKey(NEWS_KEY)) {
                mNews = savedInstanceState.getParcelable(NEWS_KEY);
            }
        }

        // Set references for all the views.
        setReferencesToViews(rootView);

        // Set the click listeners for the buttons.
        mMarkAsFav.setOnClickListener(this);
        mReadMore.setOnClickListener(this);
        mShare.setOnClickListener(this);

        // Bind data to all the views.
        bindDataToView();

        // If the share action provider has been created, set the share intent.
        if(mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createNewsShareIntent());
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options menu.
        inflater.inflate(R.menu.menu_details, menu);

        // Retrieve the share action menu item.
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the share action provider.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If the news object has been created, set the share intent.
        if(mNews != null) {
            mShareActionProvider.setShareIntent(createNewsShareIntent());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the news story object in the bundle.
        outState.putParcelable(NEWS_KEY, mNews);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        // Determine which view was clicked and proceed accordingly.
        switch(v.getId()) {
            case R.id.mark_favorite_button: {
                Log.d(LOG_TAG, "Mark as fav button clicked");

                // Disable mark as favorite button.
                mMarkAsFav.setEnabled(false);

                // Set the news story as favorite.
                mNews.setIsFavorite(1);

                // Create intent to add news story into database.
                Intent intent = new Intent(getActivity(), AddFavoriteService.class);
                intent.putExtra(NEWS_FAVORITE, mNews);

                // Send intent to start add favorite intent service.
                getActivity().startService(intent);

                break;
            }
            case R.id.read_more_button: {
                Log.d(LOG_TAG, "Read more button clicked");

                // Check if news story uri is valid.
                if(mNews.getUriStory() != null && !mNews.getUriStory().isEmpty()) {
                    // Create implicit intent to view full news story.
                    Intent newsIntent = new Intent();

                    // Set the intent action and data.
                    newsIntent.setAction(Intent.ACTION_VIEW)
                            .setData(Uri.parse(Utility
                                    .removeCharsFromString(mNews.getUriStory(), "\\")));

                    // Check if at least one app exists on the device that can handle this intent.
                    if(getActivity().getPackageManager().queryIntentActivities
                            (newsIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                        // Pass intent to view full news story.
                        startActivity(newsIntent);
                    } else {
                        // No apps exist on the device that can perform this action.
                        // Show user message in an alert dialog.
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle(getString(R.string.msg_err_dialog_title));
                        alert.setMessage(getString(R.string.msg_err_no_apps));
                        alert.setPositiveButton(getString(R.string.label_dialog_ok), null);
                        alert.show();
                    }
                } else {
                    // News story uri is not available. Show user message in an alert dialog.
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(getString(R.string.msg_err_dialog_title));
                    alert.setMessage(getString(R.string.msg_err_no_uri));
                    alert.setPositiveButton(getString(R.string.label_dialog_ok), null);
                    alert.show();
                }

                break;
            }
            case R.id.share_fab: {
                Log.d(LOG_TAG, "Share button clicked");

                // Create the intent to share the news story.
                Intent shareIntent = Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType(getString(R.string.type_share_intent))
                        .setSubject(getHeadline())
                        .setText(getSummary() + getString(R.string.msg_read_more) + getUriStory())
                        .getIntent(), getString(R.string.action_share));

                // Check if at least one app exists on the device that can handle this intent.
                if(getActivity().getPackageManager().queryIntentActivities
                        (shareIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                    // Pass intent to share news story.
                    startActivity(shareIntent);
                } else {
                    // No apps exist on the device that can perform this action.
                    // Show user message in an alert dialog.
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(getString(R.string.msg_err_dialog_title));
                    alert.setMessage(getString(R.string.msg_err_no_apps));
                    alert.setPositiveButton(getString(R.string.label_dialog_ok), null);
                    alert.show();
                }

                break;
            }
        }
    }

    /**
     * Provides target for Picasso that loads the thumbnail image from the url
     * and extracts and stores the thumbnail image byte array.
     */
    private final class ThumbnailTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Check if bitmap is valid before attempting to convert into byte array.
            if(bitmap != null) {
                // Get byte array from thumbnail bitmap.
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                // Set the thumbnail byte array in the news object.
                mNews.setThumbnailByteArray(stream.toByteArray());
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }
    }

    /**
     * Provides target for Picasso that loads the photo image from the url
     * and extracts and stores the photo image byte array.
     */
    private final class PhotoTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Check if bitmap is valid before attempting to convert into byte array.
            if(bitmap != null) {
                // Set photo bitmap onto the image view.
                mPhoto.setImageBitmap(bitmap);

                // Get byte array from photo bitmap.
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                // Set the photo byte array in the news object.
                mNews.setPhotoByteArray(stream.toByteArray());
            }

            // Now enable the mark as favorite button if this is not a favorite story.
            if(mNews.getIsFavorite() == 0) {
                mMarkAsFav.setEnabled(true);
            }
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
        mShare = (FloatingActionButton) rootView.findViewById(R.id.share_fab);
    }

    /**
     * Binds the data to the corresponding views.
     */
    private void bindDataToView() {
        // Set the title to reflect the current news category.
        getActivity().setTitle(Utility.getNewsCategoryLabel(getActivity()));

        // Load photo into image view. If this is a favorite story,
        // then load from byte array else use Picasso.
        if(mNews.getIsFavorite() == 1) {
            // Get the photo byte array from the news object.
            byte[] photoByteStream = mNews.getPhoto();

            // Check if byte array for photo is non null and non zero length.
            if(photoByteStream != null && photoByteStream.length != 0) {
                // Get the photo bitmap from byte array.
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoByteStream, 0, photoByteStream.length);

                // Set the photo bitmap into the image view.
                mPhoto.setImageBitmap(photoBitmap);
                mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                // TODO: Set appropriate error meassage.
            }
        } else {
            // Load the photo using picasso.
            if(mNews.getUriPhoto() != null) {
                Picasso.with(getActivity())
                        .load(mNews.getUriPhoto())
                        .into(mPhotoTarget);
            } else {
                // TODO: Set appropriate error meassage.
            }
        }

        // Check if it's not a favorite and the thumbnail byte array was not generated in the
        // headlines screen. If so, use picasso to get the bitmap and generate thumbnail byte
        // array and then store it in the news object. It will be required if the user
        // chooses to mark this as favorite.
        if(mNews.getIsFavorite() == 0
                && mNews.getThumbnail() != null
                && mNews.getThumbnail().length == 0) {
            // Generate the thumbnail byte array using picasso.
            Picasso.with(getActivity())
                    .load(mNews.getUriThumbnail())
                    .into(mThumbnailTarget);
        }

        // Set the caption.
        if(mNews.getCaptionPhoto() != null && !mNews.getCaptionPhoto().isEmpty()) {
            mCaption.setText(mNews.getCaptionPhoto());
            mCaption.setContentDescription(mNews.getCaptionPhoto());
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the headline.
        if(mNews.getHeadline() != null && !mNews.getHeadline().isEmpty()) {
            mHeadline.setText(mNews.getHeadline());
            mHeadline.setContentDescription(mNews.getHeadline());
        } else {
            // TODO: Set appropriate error meassage.
        }

        // Set the author.
        if(mNews.getAuthor() != null && !mNews.getAuthor().isEmpty()) {
            mAuthor.setText(mNews.getAuthor());
            mAuthor.setContentDescription(mNews.getAuthor());
        } else {
            // TODO: Set appropriate error message.
        }

        // Set the date.
        if(mNews.getDate() != null && !mNews.getDate().isEmpty()) {
            mDate.setText(mNews.getDate());
            mDate.setContentDescription(mNews.getDate());
        } else {
            // TODO: Set appropriate error message.
        }

        // Set the summary.
        if(mNews.getSummary() != null && !mNews.getSummary().isEmpty()) {
            mSummary.setText(mNews.getSummary());
            mSummary.setContentDescription(mNews.getSummary());
        } else {
            // TODO: Set appropriate error message.
        }
    }

    /**
     * Returns an intent with the news headline, summary and uri added as extras.
     */
    private Intent createNewsShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType(getString(R.string.type_share_intent));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getHeadline());
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getSummary() + getString(R.string.msg_read_more) + getUriStory());
        return shareIntent;
    }

    /**
     * Gets the headline to set as subject.
     */
    private String getHeadline() {
        // Get the headline from the news object.
        String headline = null;
        if(mNews.getHeadline() != null && !mNews.getHeadline().isEmpty()) {
            headline = mNews.getHeadline();
        } else {
            // TODO: Show error message.
            // Error message.
            headline = "Some error message";
        }

        return headline;
    }

    /**
     * Gets the summary to set as text.
     */
    private String getSummary() {
        // Get the summary from the news object.
        String summary = null;
        if(mNews.getSummary() != null && !mNews.getSummary().isEmpty()) {
            summary = mNews.getSummary();
        } else {
            // TODO: Show error message.
            // Error message.
            summary = "Some error message";
        }

        return summary;
    }

    /**
     * Gets the uri for the story to set as text.
     */
    private String getUriStory() {
        // Get the uri from the news object.
        String uri = null;
        if(mNews.getUriStory() != null && !mNews.getUriStory().isEmpty()) {
            uri = mNews.getUriStory();
        } else {
            // TODO: Show error message.
            // Error message.
            uri = "Some error message";
        }

        return uri;
    }
}
