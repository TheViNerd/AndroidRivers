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

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.asyncs.DownloadImageAsync
import com.silverkeytech.news_engine.syndications.SyndicationFeedItem
import org.holoeverywhere.app.Activity
import java.util.*

//Manage the rendering of each news item in the river list
class FeedContentRenderer(val context: Activity, val language: String){
    companion object {
        val TAG: String = FeedContentRenderer::class.java.simpleName
    }

    //hold the view data for the list
    data class ViewHolder (var news: TextView, val indicator: TextView)

    //show and prepare the interaction for each individual news item
    fun handleNewsListing(listViewId: Int, feedTitle: String, feedUrl: String, feedItems: List<SyndicationFeedItem>) {
        val textSize = context.getVisualPref().listTextSize

        //now sort it so people always have the latest news first
        var list = context.findView<ListView>(listViewId)

        var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var adapter = object : ArrayAdapter<SyndicationFeedItem>(context, R.layout.news_item, feedItems) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var currentView = convertView
                var holder: ViewHolder? = null

                val currentNewsItem = feedItems[position]
                val news = currentNewsItem.toString().trim()

                fun showIndicator() {
                    if (currentNewsItem.enclosure != null){
                        val enclosure = currentNewsItem.enclosure!!
                        if (isSupportedImageMime(enclosure.mimeType)){
                            holder?.indicator?.setBackgroundColor(PreferenceDefaults.STANDARD_NEWS_IMAGE)
                        }
                        else{
                            holder?.indicator?.setBackgroundColor(PreferenceDefaults.STANDARD_NEWS_PODCAST)
                        }
                    }else{
                        holder?.indicator?.setBackgroundColor(PreferenceDefaults.STANDARD_NEWS_COLOR)
                    }
                }

                if (currentView == null){
                    currentView = inflater.inflate(R.layout.news_item, parent, false)

                    currentView!!.findViewById(R.id.news_item_source_tv)!!.visibility = android.view.View.GONE
                    holder = ViewHolder(currentView.findView<TextView>(R.id.news_item_text_tv),
                            currentView.findView<TextView>(R.id.news_item_indicator_tv))

                    handleForeignTextStyle(this@FeedContentRenderer.context, language, holder.news, textSize.toFloat())

                    currentView!!.tag = holder
                }else{
                    holder = currentView.tag as ViewHolder
                }

                handleForeignText(language, holder.news, news)

                showIndicator()

                if (news.isNullOrEmpty()){
                    currentView?.visibility = View.GONE
                }   else{
                    currentView?.visibility = View.VISIBLE
                }
                return currentView
            }
        }

        list.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, p2, p3 ->
            val currentNews = feedItems.get(p2);

            var dlg: View = inflater.inflate(R.layout.feed_details, null)!!
            var msg: String

            if (currentNews.description.isNullOrBlank() && !currentNews.title.isNullOrBlank())
                msg = scrubHtml(currentNews.title)
            else {
                if (language == "ar")
                    msg = scrubHtml(currentNews.description)//.limitText(PreferenceDefaults.CONTENT_BODY_ARABIC_MAX_LENGTH))
                else
                    msg = scrubHtml(currentNews.description)//.limitText(PreferenceDefaults.CONTENT_BODY_MAX_LENGTH))
            }

            dlg.setBackgroundColor(context.getStandardDialogBackgroundColor())

            var body = dlg.findView<TextView>(R.id.feed_details_text_tv)
            body.movementMethod = ScrollingMovementMethod()
            handleForeignText(language, body, msg)
            handleForeignTextStyle(context, language, body, textSize.toFloat())
            handleTextColorBasedOnTheme(context, body)

            var createdDialog : Dialog? = null //important to be declared here because the content click must have access to the future to be created dialog box to close it

            val detector = ScrollMotionDetector()
            val listener = detector.attach(onClickEvent = { ->
                createdDialog?.dismiss()
            }, onMoveEvent = null)

            body.setOnTouchListener(listener)

            val buttons = ArrayList<DialogBtn>()

            if (currentNews.hasLink()){
                buttons.add(DialogBtn(context.getString(R.string.go), { dlg ->
                    startOpenBrowserActivity(context, currentNews.link!!)
                    dlg.dismiss()
                }))

                buttons.add(DialogBtn(context.getString(R.string.share), { dlg ->
                    var intent = shareActionIntent(currentNews.title!!, currentNews.link!!)
                    context.startActivity(Intent.createChooser(intent, "Share link"))
                }))
            }

            if (currentNews.hasEnclosure()){
                var enclosure = currentNews.enclosure!!
                if (isSupportedImageMime(enclosure.mimeType)){
                    buttons.add(DialogBtn("Image", { dlg ->
                        Log.d(TAG, "I am downloading a ${enclosure.url} with type ${enclosure.mimeType}")
                        DownloadImageAsync(context).execute(enclosure.url)
                    }))
                } else
                    if (currentNews.isPodcast()){
                        buttons.add(DialogBtn(context.getString(R.string.podcast), { dlg ->
                            var messenger = Messenger(object : Handler(){
                                override fun handleMessage(msg: Message) {
                                    val path = msg.obj as String

                                    if (msg.arg1 == android.app.Activity.RESULT_OK && !path.isNullOrBlank()){
                                        context.toastee("File is successfully downloaded at $path", Duration.LONG)
                                        MediaScannerWrapper.scanPodcasts(context, path)
                                    }else{
                                        context.toastee("Download failed", Duration.LONG)
                                    }
                                }
                            })

                            startDownloadService(context, currentNews.title!!, currentNews.enclosure!!.url,
                                    feedTitle, feedUrl, currentNews.description!!,
                                    messenger)

                            dlg.dismiss()
                        }))
                    }
            }

            createdDialog = createFlexibleContentDialog(context = context, content = dlg,
                    dismissOnTouch = true, buttons = buttons.toArray(arrayOf<DialogBtn>()))
            createdDialog.show()
        }

        list.adapter = adapter
    }
}