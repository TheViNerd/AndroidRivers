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
import com.silverkeytech.android_rivers.activities.getMain
import com.silverkeytech.android_rivers.activities.toastee
import com.silverkeytech.android_rivers.asyncs.DownloadBookmarksAsync
import com.silverkeytech.android_rivers.asyncs.DownloadRiverContentAsync
import com.silverkeytech.android_rivers.db.*
import com.silverkeytech.news_engine.outlines.Opml
import com.silverkeytech.news_engine.outlines.Outline
import com.silverkeytech.news_engine.outlines.sortOutlineAsc
import com.silverkeytech.news_engine.outlines.sortOutlineDesc
import com.silverkeytech.news_engine.riverjs.getSortedNewsItems
import org.holoeverywhere.LayoutInflater
import org.holoeverywhere.app.Activity
import java.util.*

class RiverListFragment(): MainListFragment() {
    companion object {
        val TAG: String = RiverListFragment::class.java.simpleName
    }

    val DEFAULT_SUBSCRIPTION_LIST = "http://hobieu.apphb.com/api/1/default/riverssubscription"

    var lastEnteredUrl: String? = ""

    var isFirstLoad: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val vw = inflater!!.inflate(R.layout.river_list_fragment, container, false)

        return vw
    }

    override fun onStart() {
        super.onStart()

        val downloadIf = true//parent.getSetupPref().getDownloadDefaultRiversIfNecessary()
        displayRiverBookmarks(downloadIf)
    }

    override fun onResume() {
        Log.d(TAG, "OnResume")

        if (userVisibleHint){
            Log.d(TAG, "OnResume - RiverListFragment visible")
            displayRiverBookmarks(false)
        }

        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.d(TAG, "OnHiddenChanged $hidden")

        if (!hidden && !isFirstLoad){
            displayRiverBookmarks(false)
        }

        isFirstLoad = false
        super.onHiddenChanged(hidden)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (menu != null){
            val sort: MenuItem = menu.findItem(R.id.river_list_fragment_menu_sort)!!
            val nextSort = nextSortCycle()
            Log.d(TAG, "The next sort cycle from current ${parent.getContentPref().riverBookmarksSorting} is $nextSort")
            setSortButtonText(sort, nextSort)

            val refresh = menu.findItem(R.id.river_list_fragment_menu_refresh)!!
            val list = view!!.findView<ListView>(android.R.id.list)

            if (list.count != 0)
                refresh.isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.river_list_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: com.actionbarsherlock.view.MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.river_list_fragment_menu_download_all_rivers -> {
                val subscriptionList = parent.getMain().getRiverBookmarksCache()

                if (subscriptionList != null){
                    val titleList = subscriptionList.body?.outline?.asSequence()?.map { it.text!! }?.toCollection(arrayListOf<String>())!!
                    val urlList = subscriptionList.body?.outline?.asSequence()?.map { it.url!! }?.toCollection(arrayListOf<String>())!!

                    startDownloadAllRiverService(parent, titleList, urlList)
                    return true
                }
                else {
                    parent.toastee("Sorry, there's nothing to download.", Duration.AVERAGE)
                    return true
                }
            }
            R.id.river_list_fragment_menu_show_add_dialog -> {
                displayAddNewRiverDialog()
                return true
            }
            R.id.river_list_fragment_menu_refresh -> {
                refreshRiverBookmarks(true)
                return true
            }
            R.id.river_list_fragment_menu_sort -> {
                val nextSort = nextSortCycle()
                Log.d(TAG, "The new sort is $nextSort")
                parent.getContentPref().riverBookmarksSorting = nextSort
                displayRiverBookmarks(false)
                return true
            }

            else -> return false
        }
    }

    override fun onPause() {
        Log.d(TAG, "OnPause")
        super.onPause()
    }

    fun showMessage(msg: String) {
        val txt = view!!.findViewById(R.id.river_list_fragment_message_tv) as TextView
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

    fun setSortButtonText(item: MenuItem?, sort: Int) {
        when (sort){
            PreferenceValue.SORT_ASC -> item?.title = this.getString(R.string.sort_asc)
            PreferenceValue.SORT_DESC -> item?.title = this.getString(R.string.sort_desc)
            PreferenceValue.SORT_NONE -> item?.title = this.getString(R.string.sort_cancel)
            else -> {

            }
        }
    }

    fun nextSortCycle(): Int {
        val sort = parent.getContentPref().riverBookmarksSorting
        return when (sort){
            PreferenceValue.SORT_ASC -> PreferenceValue.SORT_DESC
            PreferenceValue.SORT_DESC -> PreferenceValue.SORT_NONE
            PreferenceValue.SORT_NONE -> PreferenceValue.SORT_ASC
            else -> {
                PreferenceValue.SORT_ASC
            }
        }
    }

    private fun displayAddNewRiverDialog() {
        val (hasClip, uri) = this.tryGetUriFromClipboard()

        if (hasClip)
            lastEnteredUrl = uri

        val dlg = createSingleInputDialog(parent, "Add new river", lastEnteredUrl, "Set url here", {
            dlg, url ->
            if (url.isNullOrBlank()){
                parent.toastee("Please enter url of the river")
            }
            else {
                var currentUrl = url!!
                if (!currentUrl.contains("http://"))
                    currentUrl = "http://" + currentUrl

                lastEnteredUrl = currentUrl

                val u = safeUrlConvert(currentUrl)
                if (u.isTrue()){
                    DownloadRiverContentAsync(parent, "en")
                            .executeOnComplete {
                        res, lang ->
                        if (res.isTrue()){
                            var river = res.value!!
                            var sortedNewsItems = river.getSortedNewsItems()
                            parent.getMain().setRiverCache(currentUrl, sortedNewsItems)

                            var res2 = saveBookmarkToDb(currentUrl, currentUrl, BookmarkKind.RIVER, "en", null)

                            if (res2.isTrue()){
                                lastEnteredUrl = "" //reset because the op is successful
                                parent.toastee("$currentUrl river is successfully bookmarked", Duration.LONG)
                                parent.getMain().clearRiverBookmarksCache()
                                refreshRiverBookmarks(false)
                            } else{
                                parent.toastee("Sorry, we cannot add this $currentUrl river", Duration.LONG)
                            }
                        }
                        else{
                            parent.toastee("$currentUrl is not a valid river", Duration.LONG)
                        }
                    }
                            .execute(currentUrl)

                    dlg?.dismiss()
                }else{
                    Log.d(TAG, "RIVER $currentUrl conversion generates ${u.exception?.message}")
                    parent.toastee("The url you entered is not valid. Please try again", Duration.LONG)
                }
            }
        })

        dlg.show()
    }


    private fun displayRiverBookmarks(retrieveDefaultFromInternet: Boolean) {
        val cache = parent.getMain().getRiverBookmarksCache()

        if (cache != null){
            Log.d(TAG, "Get bookmarks from cache")
            handleRiversListing(cache, parent.getContentPref().riverBookmarksSorting)
        }  else{
            Log.d(TAG, "Try to retrieve bookmarks from DB")
            val bookmarks = getBookmarksFromDbAsOpml(BookmarkKind.RIVER, SortingOrder.NONE)

            if (bookmarks.body!!.outline!!.count() > 0){
                Log.d(TAG, "Now bookmarks come from the db")
                parent.getMain().setRiverBookmarksCache(bookmarks)
                handleRiversListing(bookmarks, parent.getContentPref().riverBookmarksSorting)
            }
            else if (retrieveDefaultFromInternet){
                Log.d(TAG, "Start downloading bookmarks from the Internet")
                DownloadBookmarksAsync(parent, true)
                        .executeOnComplete({
                    res ->
                    Log.d(TAG, "Using downloaded bookmark data from the Internt")
                    val res2 = saveOpmlAsBookmarks(res.value!!)

                    if (res2.isTrue()){
                        handleRiversListing(res2.value!!, parent.getContentPref().riverBookmarksSorting)
                        Log.d(TAG, "Bookmark data from the Internet is successfully saved")
                        parent.getSetupPref().downloadDefaultRiversIfNecessary = false//we only download standard rivers from the internet by default when at setup the first time
                    }
                    else{
                        Log.d(TAG, "Saving opml bookmark to db fails ${res2.exception?.message}")
                        parent.toastee("Sorry, we cannot download your initial bookmarks at the moment ${res2.exception?.message}", Duration.LONG)
                    }
                })
                        .execute(DEFAULT_SUBSCRIPTION_LIST)
            }
            else{
                //this happen when you remove the last item from the river bookmark. So have it render an empty opml
                handleRiversListing(Opml(), parent.getContentPref().riverBookmarksSorting)
            }
        }
    }

    fun handleRiversListing(opml: Opml, sort: Int) {
        var outlines = ArrayList<Outline>()

        opml.body?.outline?.forEach {
            outlines.add(it)
        }

        if (outlines.count() == 0){
            showMessage(parent.getString(R.string.empty_river_items_list))
        } else
            showMessage("")

        //perform sorting in the outlines (three choices, asc, desc and none)
        if (sort == PreferenceValue.SORT_ASC){
            outlines = sortOutlineAsc(outlines) as ArrayList<Outline>
        } else if (sort == PreferenceValue.SORT_DESC){
            outlines = sortOutlineDesc(outlines) as ArrayList<Outline>
        }

        val textSize = parent.getVisualPref().listTextSize

        val adapter = object : ArrayAdapter<Outline>(parent, android.R.layout.simple_list_item_1, android.R.id.text1, outlines){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val text = outlines[position].toString()
                return currentTextViewItem(text, convertView, parent, textSize.toFloat(), false, this@RiverListFragment.layoutInflater!!)
            }
        }

        val list = view!!.findViewById(android.R.id.list) as ListView
        list.adapter = adapter

        list.onItemClickListener = OnItemClickListener { p0, p1, p2, p3 ->
            val currentOutline = outlines.get(p2)

            var lang =
                    if (!currentOutline.language.isNullOrEmpty())
                        currentOutline.language!!
                    else
                        "en"

            startRiverActivity(parent, currentOutline.url!!, currentOutline.text!!, lang)
        }

        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { p0, p1, p2, p3 ->
            val currentOutline = outlines.get(p2)
            showRiverBookmarksQuickActionPopup(parent, currentOutline, p1!!, list)
            true
        }
    }

    fun refreshRiverBookmarks(retrieveRiversFromInternetIfNoneExisted: Boolean)
    {
        parent.getMain().clearRiverBookmarksCache()
        displayRiverBookmarks(retrieveRiversFromInternetIfNoneExisted)
    }

    fun showRiverBookmarksQuickActionPopup(context: Activity, currentOutline: Outline, item: View, list: View) {
        //overlay popup at top of clicked overview position
        val popupWidth = item.width
        val popupHeight = item.height

        val x = this.layoutInflater!!.inflate(R.layout.main_river_quick_actions, null, false)!!
        val pp = PopupWindow(x, popupWidth, popupHeight, true)

        x.setBackgroundColor(android.graphics.Color.LTGRAY)

        x.setOnClickListener {
            pp.dismiss()
        }

        val removeIcon = x.findViewById(R.id.main_river_quick_action_delete_icon) as ImageView
        removeIcon.setOnClickListener {
            val dlg = createConfirmationDialog(context = context, message = "Are you sure about removing this River bookmark?", positive = {
                try{
                    val res = DatabaseManager.cmd().bookmark().deleteByUrl(currentOutline.url!!)
                    if (res.isFalse())
                        context.toastee("Error in removing this bookmark ${res.exception?.message}")
                    else {
                        context.toastee("Bookmark removed")
                        refreshRiverBookmarks(false)
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

        val editIcon = x.findViewById(R.id.main_river_quick_action_edit_icon) as ImageView
        editIcon.setOnClickListener {
            val dlg = createSingleInputDialog(context, "Edit River Title", currentOutline.text, "Set title here", {
                dlg, title ->
                if (title.isNullOrBlank()){
                    context.toastee("Please enter title", Duration.LONG)
                }
                else {
                    val record = DatabaseManager.query().bookmark().byUrl(currentOutline.url!!)

                    val isLocalUrl = isLocalUrl(currentOutline.url!!)

                    if (record.exists){
                        record.value!!.title = title!!

                        try{
                            //perform this additional step so that when you modify a local RIVER, it updates the collection name as well
                            if (isLocalUrl){
                                val collectionId = extractIdFromLocalUrl(currentOutline.url!!)
                                if (collectionId != null){
                                    val collectionRecord = DatabaseManager.query().bookmarkCollection().byId(collectionId)

                                    if (collectionRecord.exists){
                                        collectionRecord.value!!.title = title
                                        DatabaseManager.bookmarkCollection!!.update(collectionRecord.value)
                                    }
                                }
                            }

                            DatabaseManager.bookmark!!.update(record.value)
                            context.toastee("Bookmark title is successfully modified")
                            refreshRiverBookmarks(false)
                            pp.dismiss()
                        }catch (ex: Exception){
                            context.toastee("Sorry, I cannot update the title of this river for the following reason ${ex.message}", Duration.LONG)
                        }
                    } else {
                        context.toastee("Sorry, I cannot update the title of this river", Duration.LONG)
                    }
                }

            })
            dlg.show()
        }

        val itemLocation = getLocationOnScreen(item)
        pp.showAtLocation(list, Gravity.TOP or Gravity.LEFT, itemLocation.x, itemLocation.y)
    }
}