###News Bytes


####What is it?

This is an Android app that provides live news updates to the user on their 
handheld devices. The app displays a list of headlines of the top news stories 
pulled from a remote news server. The user can then click on any headline to 
view it’s related summary, photo, caption and a link to read further details.


####Features

Highlights of the main features of the app:

- Live updates of the top news stories.
- Synopsis comprised of headline, summary, photo and caption.
- Share news story with friends through your preferred app.
- Mark news story as “Favorite” for offline reading.
- Read more details on a news story.
- Choose a preferred news story category. 


####Platform and Libraries Used

- Android SDK 17 or Higher
- Android Support AppCompat 23.1.1
- Android Support Design 23.1.1
- Google Play Services Ads 8.3.0
- Google Play Services Analytics 8.3.0
- Picasso 2.5.2


####Installation Instructions

This project uses the Gradle build automation system. To build this project, 
use the "gradlew build" command or use "Import Project" in Android Studio.

The app uses the following API keys that must be specified in the strings.xml
file:
1. New York times Developer API Key: This used in the query to fetch the news
data from the The New York Times Developer API and can be generated 
[here](http://developer.nytimes.com/apps/register).
2. Google Analytics Tracking ID: This is used by the Google Play Services 
Analytics library for tracking analytics data for the app and can be generated 
[here](https://www.google.co.in/analytics).


####Attribution

The news data is provided to the app by the New York Times Developer API. You 
can read further details [here](http://developer.nytimes.com).