package com.silverkeytech.android_rivers.creators

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.activities.Duration
import com.silverkeytech.android_rivers.activities.FeedContentRenderer
import com.silverkeytech.android_rivers.activities.toastee
import com.silverkeytech.android_rivers.asyncs.DownloadFeedAsync
import com.silverkeytech.android_rivers.db.checkIfUrlAlreadyBookmarked
import org.holoeverywhere.ArrayAdapter
import org.holoeverywhere.app.Activity
import org.holoeverywhere.widget.Button
import org.holoeverywhere.widget.Spinner

class CraigslistListingActivity (): Activity(){
    companion object {
        val TAG: String = CraigslistListingActivity::class.java.simpleName
    }

    var feedUrl: String = ""
    var feedName: String = ""
    var feedLanguage: String = ""
    var feedDateIsParseable: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.craigslist_listing)

        var actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.

        title = this.getString(R.string.title_craigslist)

        //handle UI

        //city auto complete
        val cities = getCraigsListCities(this)
        val cityNames = cities.asSequence().map { x -> x.location }.toCollection(arrayListOf<String>())
        val completion = CityAutoComplete.getUI(this, R.id.craigslist_listing_city, cityNames)!!

        var storedCity = this.getStoredPref().craigsListCity
        if (storedCity != "")
            completion.setText(storedCity)

        //categories
        var categories = getCraigsListCategories(this)
        val categoryNames = categories.asSequence().map { x -> x.name }.toCollection(arrayListOf<String>())

        val adapter = ArrayAdapter<String>(this, org.holoeverywhere.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(org.holoeverywhere.R.layout.simple_spinner_dropdown_item)

        val categoryList = findViewById(R.id.craigslist_listing_category)!! as Spinner
        categoryList.adapter = adapter

        val search = findViewById(R.id.craigslist_listing_keywords)!! as EditText
        search.hint = this.getString(R.string.optional_search_term)

        val bookmark = findViewById(R.id.craigslist_listing_bookmark_btn)!! as Button
        bookmark.isEnabled = false

        bookmark.setOnClickListener{
            addBookmarkOption(this, feedDateIsParseable) {
                collection ->
                saveBookmark(this, feedName, feedUrl, feedLanguage, collection)
                bookmark.isEnabled = false
            }
        }

        val go = findViewById(R.id.craigslist_listing_go_btn)!! as Button
        go.setOnClickListener {

            val input = completion.text.toString()

            if (input.isNullOrEmpty()){
                toastee("Please enter a city location")
            }else{
                val chosenCityUrl = cities.find { x -> x.location == input.trim() }?.url
                val cityUrl = if (chosenCityUrl != null) chosenCityUrl else ""

                //get selected categories
                val catPosition = categoryList.selectedItemPosition
                val categoryCode = categories.get(catPosition).code

                val term = search.text.toString()

                if (term.isNullOrEmpty())
                    feedUrl = "$cityUrl/$categoryCode/index.rss"
                else
                    feedUrl = "$cityUrl/search/$categoryCode?format=rss&query=$term"

                Log.d(TAG, "Fetching $feedUrl")

                DownloadFeedAsync(this, false)
                        .executeOnComplete {
                    res ->
                    if (res.isTrue()){
                        val feed = res.value!!
                        feedDateIsParseable = feed.isDateParseable
                        if (!feed.language.isNullOrBlank()){
                            feedLanguage = feed.language
                        }

                        feedName = feed.title

                        if (feed.items.size > 0)
                            this@CraigslistListingActivity.getStoredPref().craigsListCity = input.trim()

                        if (feed.items.size > 0 && !checkIfUrlAlreadyBookmarked(feedUrl))
                            bookmark.isEnabled = true
                        else
                            bookmark.isEnabled = false

                        FeedContentRenderer(this, feedLanguage)
                                .handleNewsListing(R.id.craigslist_listing_results_lv, feedName, feedUrl, feed.items)
                    }else{
                        toastee("Error ${res.exception?.message}", Duration.LONG)
                    }
                }.execute(feedUrl)
            }
        }
    }
}