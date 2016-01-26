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
import com.actionbarsherlock.view.Menu
import com.actionbarsherlock.view.MenuItem
import com.silverkeytech.android_rivers.Params
import com.silverkeytech.android_rivers.PreferenceDefaults
import com.silverkeytech.android_rivers.R
import com.silverkeytech.android_rivers.asyncs.downloadOpmlAsync
import com.silverkeytech.android_rivers.db.getBookmarksFromDbByCollection
import com.silverkeytech.android_rivers.getVisualPref
import org.holoeverywhere.app.ListActivity

open class BookmarkCollectionActivity(): ListActivity() {
    companion object {
        val TAG: String = BookmarkCollectionActivity::class.java.simpleName
    }

    var collectionTitle: String = ""
    var collectionId: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.collection)

        var i = intent!!
        collectionId = i.getIntExtra(Params.COLLECTION_ID, 0)
        collectionTitle = i.getStringExtra(Params.COLLECTION_TITLE)!!

        var actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.title = getString(R.string.edit_sources_of_collection, collectionTitle)

        displayCollection()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = supportMenuInflater!!
        inflater.inflate(R.menu.collection_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.collection_menu_help -> {
                downloadOpmlAsync(this, PreferenceDefaults.CONTENT_OUTLINE_HELP_SOURCE, getString(R.string.help))
                return true
            }
            else -> {
                return false
            }
        }
    }

    fun refreshCollection() {
        displayCollection()
    }

    fun displayCollection() {
        val bookmarks = getBookmarksFromDbByCollection(collectionId)
        BookmarkCollectionRenderer(this).handleListing(bookmarks)
    }
}