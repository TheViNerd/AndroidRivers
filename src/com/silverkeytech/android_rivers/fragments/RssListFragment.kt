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

package com.silverkeytech.android_rivers.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.actionbarsherlock.view.Menu
import com.actionbarsherlock.view.MenuInflater
import com.actionbarsherlock.view.MenuItem
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.activities.Duration
import com.silverkeytech.android_rivers.activities.getLocationOnScreen
import com.silverkeytech.android_rivers.activities.toastee
import com.silverkeytech.android_rivers.asyncs.DownloadFeedAsync
import com.silverkeytech.android_rivers.db.*
import org.holoeverywhere.LayoutInflater
import org.holoeverywhere.app.Activity

class RssListFragment(): MainListFragment() {
    companion object {
        val TAG: String = RssListFragment::class.java.simpleName
    }

    var lastEnteredUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val vw = inflater!!.inflate(R.layout.rss_list_fragment, container, false)

        return vw
    }

    override fun onResume() {
        Log.d(TAG, "OnResume")

        if (userVisibleHint){
            Log.d(TAG, "OnResume - RssListFragment visible")
            displayRssBookmarks()
        }

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.rss_list_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.rss_list_fragment_menu_show_add_dialog -> {
                displayAddNewRssDialog()
                return false
            }
            R.id.rss_list_fragment_menu_import_opml_dialog -> {
                displayImportOpmlDialog()
                return false
            }
            else -> return false
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.d(TAG, "OnHiddenChanged $hidden")
        if (!hidden){
            displayRssBookmarks()
        }
        super.onHiddenChanged(hidden)
    }

    fun displayImportOpmlDialog() {
        val (hasClip, uri) = this.tryGetUriFromClipboard()

        if (hasClip)
            lastEnteredUrl = uri

        val dlg = createSingleInputDialog(parent, "Import subscription list", lastEnteredUrl, "Set url here", {
            dlg, url ->
            if (url.isNullOrBlank()) {
                parent.toastee("Please enter url of the OPML subscription list", Duration.LONG)
            }
            else {
                var currentUrl = url!!
                if (!currentUrl.contains("http://"))
                    currentUrl = "http://" + currentUrl

                lastEnteredUrl = currentUrl

                val u = safeUrlConvert(currentUrl)

                if (u.isTrue()){
                    lastEnteredUrl = ""
                    startImportOpmlSubscriptionService(parent, u.value!!.toString())
                } else {
                    Log.d(TAG, "Opml download $currentUrl conversion generates ${u.exception?.message}")
                    parent.toastee("The url you entered is not valid. Please try again", Duration.LONG)
                }
            }
        })
        dlg.show()
    }

    fun displayAddNewRssDialog() {
        val (hasClip, uri) = this.tryGetUriFromClipboard()

        if (hasClip)
            lastEnteredUrl = uri

        val dlg = createSingleInputDialog(parent, "Add new RSS", lastEnteredUrl, "Set url here", {
            dlg, url ->
            if (url.isNullOrBlank()){
                parent.toastee("Please enter url of the river", Duration.LONG)
            }
            else {
                var currentUrl = url!!
                if (!currentUrl.contains("http://"))
                    currentUrl = "http://" + currentUrl

                lastEnteredUrl = currentUrl

                val u = safeUrlConvert(currentUrl)
                if (u.isTrue()){
                    DownloadFeedAsync(parent, true)
                            .executeOnComplete {
                        res ->
                        if (res.isTrue()){
                            var feed = res.value!!

                            val res2 = saveBookmarkToDb(feed.title, currentUrl, BookmarkKind.RSS, feed.language, null)

                            if (res2.isTrue()){
                                lastEnteredUrl = "" //reset when op is successful
                                parent.toastee("$currentUrl is successfully bookmarked")
                                displayRssBookmarks()
                            }
                            else{
                                parent.toastee("Sorry, we cannot add this $currentUrl river", Duration.LONG)
                            }
                        }else{
                            parent.toastee("Error ${res.exception?.message}", Duration.LONG)
                        }
                    }
                            .execute(currentUrl)
                    dlg?.dismiss()
                }else{
                    Log.d(TAG, "RSS $currentUrl conversion generates ${u.exception?.message}")
                    parent.toastee("The url you entered is not valid. Please try again", Duration.LONG)
                }
            }
        })

        dlg.show()
    }

    fun showMessage(msg: String) {
        val txt = view!!.findViewById(R.id.rss_list_fragment_message_tv) as TextView
        if (msg.isNullOrBlank()){
            txt.visibility = View.INVISIBLE
            txt.text = ""
        }
        else{
            val textSize = parent.getVisualPref().listTextSize
            txt.visibility = View.VISIBLE
            handleFontResize(txt, msg, textSize.toFloat())
        }
    }

    fun handleRssListing(bookmarks: List<Bookmark>) {
        if (bookmarks.count() == 0){
            showMessage(parent.getString(R.string.empty_rss_items_list))
        }
        else
            showMessage("")

        val textSize = parent.getVisualPref().listTextSize

        val adapter = object : ArrayAdapter<Bookmark>(parent, android.R.layout.simple_list_item_1, android.R.id.text1, bookmarks){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val text = bookmarks[position].toString()
                val inCollection = bookmarks[position].collection != null
                return currentTextViewItem(text, convertView, parent, textSize.toFloat(), inCollection, this@RssListFragment.layoutInflater!!)
            }
        }

        val list = view!!.findViewById(android.R.id.list) as ListView
        list.adapter = adapter
        list.onItemClickListener = OnItemClickListener { p0, p1, p2, p3 ->
            val bookmark = bookmarks.get(p2)
            startFeedActivity(parent, bookmark.url, bookmark.title, bookmark.language)
        }

        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { p0, p1, p2, p3 ->
            val currentBookmark = bookmarks.get(p2)
            showRssBookmarkQuickActionPopup(parent, currentBookmark, p1!!, list)
            true
        }
    }

    fun displayRssBookmarks() {
        val bookmarks = getBookmarksFromDb(BookmarkKind.RSS, SortingOrder.ASC)
        handleRssListing(bookmarks)
    }

    fun showRssBookmarkQuickActionPopup(context: Activity, currentBookmark: Bookmark, item: View, list: View) {
        //overlay popup at top of clicked overview position
        val popupWidth = item.width
        val popupHeight = item.height

        val x = context.layoutInflater.inflate(R.layout.main_feed_quick_actions, null, false)!!
        val pp = PopupWindow(x, popupWidth, popupHeight, true)

        x.setBackgroundColor(android.graphics.Color.LTGRAY)

        x.setOnClickListener {
            pp.dismiss()
        }

        val removeIcon = x.findViewById(R.id.main_feed_quick_action_delete_icon) as ImageView
        removeIcon.setOnClickListener {
            val dlg = createConfirmationDialog(context = context, message = "Are you sure about removing this RSS bookmark?", positive = {
                try{
                    val res = removeItemByUrlFromBookmarkDb(currentBookmark.url)
                    if (res.isFalse())
                        context.toastee("We have problem in removing this bookmark")
                    else {
                        context.toastee("Bookmark removed")
                        displayRssBookmarks()
                    }
                }
                catch(e: Exception){
                    context.toastee("Error in trying to remove this bookmark ${e.message}")
                }
                pp.dismiss()
            }, negative = {
                pp.dismiss()
            })

            dlg.show()
        }


        fun showCollectionAssignmentPopup(alreadyBelongsToACollection: Boolean) {
            var coll = getBookmarkCollectionFromDb(sortByTitleOrder = SortingOrder.ASC)

            if (coll.size == 0){
                context.toastee("Please create a collection before assigning a bookmark to it", Duration.LONG)
                pp.dismiss()
            }
            else if (coll.size == 1 && alreadyBelongsToACollection){
                context.toastee("This RSS already belongs to a collection and there is no other collection to reassign it to", Duration.LONG)
                pp.dismiss()
            }
            else {
                val dialog = AlertDialog.Builder(context)
                if (alreadyBelongsToACollection)
                    dialog.setTitle("Reassign bookmark to collection")
                else
                    dialog.setTitle("Assign bookmark to collection")

                val collectionWithoutCurrent = coll.filter { x -> x.id != currentBookmark.collection?.id }
                var collectionTitles = collectionWithoutCurrent.map { x -> x.title }.toTypedArray()

                dialog.setItems(collectionTitles, dlgClickListener {
                    dlg, idx ->
                    val selectedCollection = collectionWithoutCurrent[idx]

                    if (currentBookmark.collection == null){
                        currentBookmark.collection = BookmarkCollection()
                    }

                    currentBookmark.collection!!.id = selectedCollection.id

                    try{
                        DatabaseManager.bookmark!!.update(currentBookmark)
                        if (alreadyBelongsToACollection)
                            context.toastee("This RSS has been successfully reassigned to '${selectedCollection.title}' collection", Duration.LONG)
                        else
                            context.toastee("This RSS has been successfuly assigned to '${selectedCollection.title}' collection", Duration.LONG)


                    } catch(ex: Exception){
                        context.toastee("Sorry, I have problem updating this RSS bookmark record", Duration.LONG)
                    }

                    pp.dismiss()
                })

                var createdDialog = dialog.create()
                createdDialog.setCanceledOnTouchOutside(true)
                createdDialog.setCancelable(true)
                createdDialog.show()
            }
        }

        val editIcon = x.findViewById(R.id.main_feed_quick_action_edit_icon) as ImageView
        editIcon.setOnClickListener {
            //check if it already belongs to a collection so there is no need to download.
            val alreadyBelongsToACollection = currentBookmark.collection != null
            //do a verification that this feed can actually be part of a collection
            if (!alreadyBelongsToACollection){
                DownloadFeedAsync(context, true)
                        .executeOnComplete {
                    res ->
                    if (res.isTrue()){
                        var feed = res.value!!
                        if (!feed.isDateParseable){
                            context.toastee("Sorry, this feed cannot belong to a collection because we cannot determine its dates", Duration.LONG)
                        }
                        else{
                            showCollectionAssignmentPopup(alreadyBelongsToACollection)
                        }
                    }else{
                        context.toastee("Error ${res.exception?.message}", Duration.LONG)
                    }
                }
                        .execute(currentBookmark.url)
            }else
                showCollectionAssignmentPopup(alreadyBelongsToACollection)

        }

        val itemLocation = getLocationOnScreen(item)
        pp.showAtLocation(list, Gravity.TOP or Gravity.LEFT, itemLocation.x, itemLocation.y)
    }
}