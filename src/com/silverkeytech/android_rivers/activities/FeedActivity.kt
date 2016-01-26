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

package com.silverkeytech.android_rivers.activities

import android.os.Bundle
import android.util.Log
import com.actionbarsherlock.view.ActionMode
import com.actionbarsherlock.view.Menu
import com.actionbarsherlock.view.MenuItem
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.asyncs.DownloadFeedAsync
import com.silverkeytech.android_rivers.asyncs.downloadOpmlAsync
import com.silverkeytech.android_rivers.db.checkIfUrlAlreadyBookmarked
import org.holoeverywhere.app.Activity
import org.holoeverywhere.app.ListActivity

//Responsible of downloading, caching and viewing a news river content
class FeedActivity(): ListActivity(), WithVisualModificationPanel
{
    companion object {
        val TAG: String = FeedActivity::class.java.simpleName
    }

    var feedUrl: String = ""
    var feedName: String = ""
    var feedLanguage: String = ""
    var mode: ActionMode? = null

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feeds)

        var actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.

        var i = intent!!
        feedUrl = i.getStringExtra(Params.FEED_URL)!!
        feedName = i.getStringExtra(Params.FEED_NAME)!!
        feedLanguage = i.getStringExtra(Params.FEED_LANGUAGE)!!

        title = feedName

        downloadFeed(false)
    }

    var feedDateIsParseable: Boolean = false

    fun downloadFeed(ignoreCache: Boolean) {
        DownloadFeedAsync(this, ignoreCache)
                .executeOnComplete {
            res ->
            if (res.isTrue()){
                var feed = res.value!!
                feedDateIsParseable = feed.isDateParseable
                Log.d(TAG, "$feedUrl is parseable = $feedDateIsParseable with items ${feed.items.size}")
                if (!feed.language.isNullOrBlank()){
                    Log.d(TAG, "Obtained feed language is ${feed.language}")
                    feedLanguage = feed.language
                }
                FeedContentRenderer(this, feedLanguage)
                        .handleNewsListing(android.R.id.list, feedName, feedUrl, feed.items)
            }else{
                toastee("Error ${res.exception?.message}", Duration.LONG)
            }
        }
                .execute(feedUrl)
    }

    val REFRESH: Int = 1
    val RESIZE_TEXT: Int = 2

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val feedBookmarked = checkIfUrlAlreadyBookmarked(feedUrl)

        val bookmarkMenu = menu!!.findItem(R.id.feed_menu_bookmark)!!
        bookmarkMenu.isVisible = !feedBookmarked
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, RESIZE_TEXT, 0, "Resize Text")
        ?.setIcon(android.R.drawable.ic_menu_preferences)
        ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu?.add(0, REFRESH, 0, "Refresh")
        ?.setIcon(R.drawable.ic_menu_refresh)
        ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        var inflater = supportMenuInflater!!
        inflater.inflate(R.menu.feed_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.feed_menu_help -> {
                downloadOpmlAsync(this, PreferenceDefaults.CONTENT_OUTLINE_HELP_SOURCE, getString(R.string.help))
                return true
            }
            REFRESH -> {
                downloadFeed(true)
                return true
            }
            RESIZE_TEXT -> {
                mode = startActionMode(ResizeTextActionMode(this, mode))
                return true
            }
            R.id.feed_menu_bookmark -> {
                addBookmarkOption(this, feedDateIsParseable) {
                    collection ->
                    saveBookmark(this, feedName, feedUrl, feedLanguage, collection)
                }
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    override fun getActivity(): Activity {
        return this
    }

    override fun refreshContent() {
        downloadFeed(false)
    }
}