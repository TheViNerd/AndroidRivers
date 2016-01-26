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
import com.actionbarsherlock.view.Menu
import com.actionbarsherlock.view.MenuItem
import com.pl.polidea.treeview.InMemoryTreeStateManager
import com.pl.polidea.treeview.TreeBuilder
import com.pl.polidea.treeview.TreeViewList
import com.silverkeytech.android_rivers.Params
import com.silverkeytech.android_rivers.PreferenceDefaults
import com.silverkeytech.android_rivers.R
import com.silverkeytech.android_rivers.asyncs.DownloadOpmlAsync
import com.silverkeytech.android_rivers.asyncs.downloadOpmlAsync
import com.silverkeytech.android_rivers.db.BookmarkKind
import com.silverkeytech.android_rivers.db.checkIfUrlAlreadyBookmarked
import com.silverkeytech.android_rivers.db.saveBookmarkToDb
import com.silverkeytech.android_rivers.getVisualPref
import com.silverkeytech.android_rivers.outliner.SimpleAdapter
import com.silverkeytech.news_engine.outliner.OutlineContent
import org.holoeverywhere.app.Activity
import java.util.*

class OutlinerActivity(): Activity()
{
    companion object {
        val TAG: String = OutlinerActivity::class.java.simpleName
    }

    val LEVEL_NUMBER: Int = 12

    var outlinesUrl: String? = null
    var outlinesTitle: String? = ""
    var expandAll: Boolean = false

    var outlinesData: ArrayList<OutlineContent>? = null
    var treeManager = InMemoryTreeStateManager<Long?>()

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.outliner)

        var actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.

        val extras = intent?.extras

        if (extras != null){
            outlinesTitle = extras.getString(Params.OUTLINES_TITLE)
            if (outlinesTitle != null)
                title = outlinesTitle

            outlinesUrl = extras.getString(Params.OUTLINES_URL)

            expandAll = extras.getBoolean(Params.OUTLINES_EXPAND_ALL)
            outlinesData = extras.getSerializable(Params.OUTLINES_DATA)!! as ArrayList<OutlineContent>

            displayOutlines(treeManager, outlinesData!!, expandAll)
        }
        else
            toastee("There is no data to be displayed in outline mode ", Duration.LONG)
    }

    fun displayOutlines(manager: InMemoryTreeStateManager<Long?>, outlines: ArrayList<OutlineContent>, expandAllInitially: Boolean) {
        manager.clear()
        manager.setVisibleByDefault(expandAllInitially)

        var treeBuilder = TreeBuilder(manager)

        var counter: Long = 0
        for(e in outlines){
            treeBuilder.sequentiallyAddNextNode(counter, e.level)
            counter++
        }

        var treeView = findView<TreeViewList>(R.id.outliner_main_tree)
        val textSize = getVisualPref().listTextSize

        var simpleAdapter = SimpleAdapter(this, treeManager, LEVEL_NUMBER, outlines, textSize)
        treeView.adapter = simpleAdapter
    }

    fun collapseOutlines(manager: InMemoryTreeStateManager<Long?>, outlines: ArrayList<OutlineContent>) {
        var counter: Long = 0
        for(e in outlines){
            if (e.level == 0){
                manager.collapseChildren(counter)
            }
            counter++
        }
    }

    fun expandOutlines(manager: InMemoryTreeStateManager<Long?>, outlines: ArrayList<OutlineContent>) {
        var counter: Long = 0
        for(e in outlines){
            if (e.level == 0){
                manager.expandEverythingBelow(counter)
            }
            counter++
        }
    }

    val REFRESH: Int = 1

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = supportMenuInflater!!
        inflater.inflate(R.menu.outliner_menu, menu)

        if (outlinesUrl != null){
            menu?.add(0, REFRESH, 0, "Refresh")
            ?.setIcon(R.drawable.ic_menu_refresh)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "Title $outlinesTitle and Url $outlinesUrl")
        val showBookmark = outlinesUrl != null && outlinesTitle != null && !checkIfUrlAlreadyBookmarked(outlinesUrl!!)

        val bookmarkMenu = menu?.findItem(R.id.outliner_menu_bookmark)!!
        bookmarkMenu.isVisible = showBookmark
        return true
    }

    internal fun refreshContent(url: String) {
        DownloadOpmlAsync(this)
                .executeOnProcessedCompletion({
            res ->
            if (res.isTrue()){
                displayOutlines(treeManager, res.value!!, expandAll)
                this.getMain().setOpmlCache(url, res.value)
            }
            else{
                toastee("Downloading url fails because of ${res.exception?.message}", Duration.LONG)
            }
        }, { outline -> outline.text != "<rules>" })
                .execute(url)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.outliner_menu_help -> {
                downloadOpmlAsync(this, PreferenceDefaults.CONTENT_OUTLINE_HELP_SOURCE, getString(R.string.help))
                return true
            }
            REFRESH -> {
                refreshContent(outlinesUrl!!)
                return true
            }
            R.id.outliner_menu_collapse -> {
                collapseOutlines(treeManager, outlinesData!!)
                return true
            }
            R.id.outliner_menu_expand -> {
                expandOutlines(treeManager, outlinesData!!)
                return true
            }
            R.id.outliner_menu_bookmark -> {
                Log.d(TAG, "At save !! Title $outlinesTitle and Url $outlinesUrl")

                var res = saveBookmarkToDb(outlinesTitle!!, outlinesUrl!!, BookmarkKind.OPML, "en", null)

                if (res.isTrue()){
                    toastee("'$outlinesTitle' is added to your bookmark.")
                    return true
                } else{
                    toastee("Sorry, we cannot add this $outlinesUrl", Duration.LONG)
                    return false
                }
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}