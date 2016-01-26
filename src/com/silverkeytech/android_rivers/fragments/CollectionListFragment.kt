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

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.actionbarsherlock.view.Menu
import com.actionbarsherlock.view.MenuInflater
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.activities.*
import com.silverkeytech.android_rivers.db.*
import org.holoeverywhere.LayoutInflater
import org.holoeverywhere.app.Activity
import org.holoeverywhere.app.AlertDialog

class CollectionListFragment: MainListFragment() {
    companion object {
        val TAG: String = CollectionListFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val vw = inflater!!.inflate(R.layout.collection_list_fragment, container, false)

        return vw
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.collection_list_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: com.actionbarsherlock.view.MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.collection_list_fragment_menu_add_new -> {
                showAddNewCollectionDialog()
                return true
            }
            else -> return false
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.d(TAG, "OnHiddenChanged $hidden")
        if (!hidden){
            displayBookmarkCollection()
        }
        super.onHiddenChanged(hidden)
    }

    fun showMessage(msg: String) {
        val txt = view.findView<TextView>(R.id.collection_list_fragment_message_tv)
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

    private fun displayBookmarkCollection() {
        val coll = getBookmarkCollectionFromDb(SortingOrder.ASC)
        handleCollection(coll)
    }

    fun handleCollection(coll: List<BookmarkCollection>) {
        if (coll.count() == 0){
            showMessage("""Collection is useful to group your feeds from 'more news'. Each collection presents the news in one river of news. Use menu option to add a new collection.""")
        }
        else
            showMessage("")

        val textSize = parent.getVisualPref().listTextSize

        val adapter = object : ArrayAdapter<BookmarkCollection>(parent, android.R.layout.simple_list_item_1, android.R.id.text1, coll){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val text = coll[position].toString()
                return currentTextViewItem(text, convertView, parent, textSize.toFloat(), false, this@CollectionListFragment.layoutInflater!!)
            }
        }

        val list = view.findView<ListView>(android.R.id.list)
        list.adapter = adapter
        list.onItemClickListener = OnItemClickListener { p0, p1, p2, p3 ->
            val current = coll[p2]
            //make the local url and delegate to riveractivity to figure out whether
            //to use this collection data from cache or perform the arduous task of
            //downloading and transforming rss feeds into river
            val localUrl = makeLocalUrl(current.id)
            startRiverActivity(parent, localUrl, current.title, "en")
        }

        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { p0, p1, p2, p3 ->
            val current = coll[p2]
            showCollectionQuickActionPopup(parent, current, p1!!, list)
            true
        }
    }

    fun showCollectionQuickActionPopup(context: Activity, collection: BookmarkCollection, item: View, list: View) {
        //overlay popup at top of clicked overview position
        val popupWidth = item.width
        val popupHeight = item.height

        val x = context.layoutInflater.inflate(R.layout.main_collection_quick_actions, null, false)!!
        val pp = PopupWindow(x, popupWidth, popupHeight, true)

        x.setBackgroundColor(android.graphics.Color.LTGRAY)

        x.setOnClickListener {
            pp.dismiss()
        }

        val delete = x.findView<ImageView>(R.id.main_collection_quick_action_delete_icon)
        delete.setOnClickListener {
            val dlg = createConfirmationDialog(context = context, message = "Are you sure about deleting this collection?", positive = {
                try{
                    Log.d("showCollectionQuickActionPopup", "Start clearing bookmarks from collection ${collection.id}")
                    clearBookmarksFromCollection(collection.id)
                    Log.d("showCollectionQuickActionPopup", "Start deleting collection ${collection.id}")

                    val res = DatabaseManager.cmd().bookmarkCollection().deleteById(collection.id)
                    if (res.isFalse())
                        context.toastee("Error in removing this bookmark collection ${res.exception?.message}")
                    else {
                        //assume that this collection is bookmarked. So remove the id from the bookmark
                        //and refresh the cache so when user view the rivers bookmark view, it is already removed
                        //This operation doesn't fail even if the collection was never added to the RIVER bookmark
                        val url = makeLocalUrl(collection.id)
                        removeItemByUrlFromBookmarkDb(url)
                        context.getMain().clearRiverBookmarksCache()
                        context.toastee("Bookmark collection removed")
                        displayBookmarkCollection()
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

        val edit = x.findView<ImageView>(R.id.main_collection_quick_action_edit_icon)
        edit.setOnClickListener{
            pp.dismiss()
            startCollectionActivity(context, collection.id, collection.title)
        }

        val itemLocation = getLocationOnScreen(item)
        pp.showAtLocation(list, Gravity.TOP or Gravity.LEFT, itemLocation.x, itemLocation.y)
    }


    fun showAddNewCollectionDialog() {
        val dlg: View = parent.layoutInflater.inflate(R.layout.collection_add_new, null)!!

        //take care of color
        dlg.drawingCacheBackgroundColor = parent.getStandardDialogBackgroundColor()

        val dialog = AlertDialog.Builder(parent)
        dialog.setView(dlg)
        dialog.setTitle("Add new collection")

        var input = dlg.findView<EditText>(R.id.collection_add_new_title_et)

        dialog.setPositiveButton("OK", DialogInterface.OnClickListener { p0, p1 ->
            val text = input.text.toString()
            if (text.isNullOrEmpty()){
                parent.toastee("Please enter collection title", Duration.AVERAGE)
                return@OnClickListener
            }

            val res = addNewCollection(text, BookmarkCollectionKind.RIVER)

            if (res.isTrue()){
                val url = makeLocalUrl(res.value!!.id)
                //when a collection is added as a river, bookmark it immediately
                saveBookmarkToDb(text, url, BookmarkKind.RIVER, "en", null)
                parent.getMain().clearRiverBookmarksCache()

                parent.toastee("Collection is successfully added")
                displayBookmarkCollection()
            } else{
                parent.toastee("Sorry, I have problem adding this new collection", Duration.AVERAGE)
            }
        })

        dialog.setNegativeButton("Cancel", { p0, p1 -> p0.dismiss() })

        val createdDialog = dialog.create()!!
        createdDialog.show()
    }
}