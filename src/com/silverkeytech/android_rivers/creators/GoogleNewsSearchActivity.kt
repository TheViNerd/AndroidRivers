/*
Android Rivers is an app to read and discover news using RiverJs, RSS and OPML format.
Copyright (C) 2012 Dody Gunawinata (dodyg@silverkeytech.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.silverkeytech.android_rivers.creators

import android.os.Bundle
import android.util.Log
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.activities.Duration
import com.silverkeytech.android_rivers.activities.FeedContentRenderer
import com.silverkeytech.android_rivers.activities.toastee
import com.silverkeytech.android_rivers.asyncs.DownloadFeedAsync
import com.silverkeytech.android_rivers.db.checkIfUrlAlreadyBookmarked
import org.holoeverywhere.ArrayAdapter
import org.holoeverywhere.app.Activity
import org.holoeverywhere.widget.Button
import org.holoeverywhere.widget.EditText
import org.holoeverywhere.widget.Spinner

class GoogleNewsSearchActivity (): Activity(){
    companion object {
        val TAG: String = GoogleNewsSearchActivity::class.java.simpleName
    }

    var feedUrl: String = ""
    var feedName: String = ""
    var feedLanguage: String = ""
    var feedDateIsParseable: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_news_search)

        var actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.

        title = this.getString(R.string.title_google_news)

        //handle UI

        val searchTerm = findViewById(R.id.google_news_search_term)!! as EditText
        searchTerm.hint = this.getString(R.string.optional_search_term)

        val regionList = findViewById(R.id.google_news_search_region)!! as Spinner

        val editionsAndLanguages = getEditionsAndLanguages()
        val countryEditions = editionsAndLanguages.asSequence().map { x -> x.key }.toCollection(arrayListOf<String>())

        val adapter = ArrayAdapter<String>(this, org.holoeverywhere.R.layout.simple_spinner_item, countryEditions);
        adapter.setDropDownViewResource(org.holoeverywhere.R.layout.simple_spinner_dropdown_item);
        regionList.adapter = adapter;

        val country = this.getStoredPref().googleNewsCountry
        Log.d(TAG, "Stored country $country")
        if (country != ""){
            var foundPosition = -1
            for(c in countryEditions.withIndex()){
                if (c.value == country)
                    foundPosition = c.index
            }

            Log.d(TAG, "Found position $foundPosition")
            if (foundPosition > -1){
                regionList.setSelection(foundPosition)
            }
        }

        val bookmark = findViewById(R.id.google_news_search_bookmark_btn)!! as Button
        bookmark.isEnabled = false
        bookmark.setOnClickListener{
            addBookmarkOption(this, feedDateIsParseable) {
                collection ->
                saveBookmark(this, feedName, feedUrl, feedLanguage, collection)
                bookmark.isEnabled = false
            }
        }

        val go = findViewById(R.id.google_news_search_go_btn)!! as Button
        go.setOnClickListener {
            val position = regionList.selectedItemPosition
            val key = countryEditions.get(position)
            val info = editionsAndLanguages[key]!!
            feedUrl = "https://news.google.com/news/feeds?cf=all&ned=${info.edition}&hl=${info.lang}&output=rss&num=50"
            val search = searchTerm.text?.toString()
            if (!search.isNullOrEmpty()){
                feedUrl += "&q=${java.net.URLEncoder.encode(search!!, "UTF-8")}"
            }

            //store google news country so you don't have to keep searching for it
            this.getStoredPref().googleNewsCountry = key;
            Log.d(TAG, "Storing google news country ${key}")

            Log.d(TAG, "Downloading $feedUrl")

            feedName = if (search.isNullOrEmpty()) key else search!!

            DownloadFeedAsync(this, false)
                    .executeOnComplete {
                res ->
                if (res.isTrue()){
                    val feed = res.value!!
                    feedDateIsParseable = feed.isDateParseable
                    if (!feed.language.isNullOrBlank()){
                        feedLanguage = feed.language
                    }

                    if (feed.items.size > 0 && !checkIfUrlAlreadyBookmarked(feedUrl))
                        bookmark.isEnabled = true
                    else
                        bookmark.isEnabled = false

                    FeedContentRenderer(this, feedLanguage)
                            .handleNewsListing(R.id.google_news_search_results_lv, feedName, feedUrl, feed.items)
                }else{
                    toastee("Error ${res.exception?.message}", Duration.LONG)
                }
            }.execute(feedUrl)
        }
    }
}