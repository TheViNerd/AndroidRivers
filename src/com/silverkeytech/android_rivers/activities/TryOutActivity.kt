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

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.RemoteViews
import com.github.kevinsawicki.http.HttpRequest
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.asyncs.DownloadImageAsync
import com.silverkeytech.android_rivers.asyncs.DownloadOpmlAsync
import com.silverkeytech.android_rivers.creators.getAirportCodes
import com.silverkeytech.android_rivers.creators.getCraigsListCategories
import com.silverkeytech.android_rivers.creators.getCraigsListCities
import com.silverkeytech.android_rivers.db.*
import com.silverkeytech.android_rivers.meta_weblog.Blog
import com.silverkeytech.android_rivers.meta_weblog.linkPost
import com.silverkeytech.android_rivers.services.DownloadService
import com.silverkeytech.news_engine.transformXmlToOpml
import org.holoeverywhere.app.Activity
import org.holoeverywhere.widget.TextView
import org.xmlrpc.android.XMLRPCClient
import java.util.*

class TryOutActivity(): Activity()
{
    companion object {
        val TAG: String = TryOutActivity::class.java.simpleName
    }

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.tryout)
        handleCraigsListCitiesAndCategories()
        handleDialog4()
        handlePost()
        handlePing()
        handleGetAirportCodes()
        handleInsertPodcast()
        handleBookmarkCollectionCreation()
        handleDownloadAtom()
        handleDownloadRss()
        handleDownloadGifImage()
        handleDownloadFile()
        handleDownloadJpgImage()
        handleDownloadPngImage()
        handleHandleNotification()
        handleCreateBookmarkTable()
        handleInsertToBookmarkTable()
        handleOutliner()
        handleDownloadRecursiveOpml()
        handleRiverJsWithOpmlSource()
    }

    fun handleCraigsListCitiesAndCategories() {
        val btn = findView<Button>(R.id.tryout_craigslist_cities)

        btn.setOnClickListener {
            val cities = getCraigsListCities(this)

            Log.d(TAG, "Cities in ${cities.size}")

            for(x in cities){
                Log.d(TAG, "${x.toString()}")
            }

            var categories = getCraigsListCategories(this)

            for(x in categories){
                Log.d(TAG, "${x.toString()}")
            }
        }
    }

    fun handleDialog4() {
        val btn = findView<Button>(R.id.tryout_dialog_4)

        val msg = """* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */"""

        btn.setOnClickListener {

            val cnt = this.layoutInflater.inflate(R.layout.news_details, null)!!
            //take care of color
            cnt.drawingCacheBackgroundColor = this.getStandardDialogBackgroundColor()

            val main = cnt.findViewById(R.id.news_details_text_tv) as TextView
            main.text = msg
            val src = cnt.findViewById(R.id.news_details_source_tv) as TextView
            src.text = "CNN>COM"

            val dlg = createFlexibleContentDialog(context = this, content = cnt, dismissOnTouch = true, buttons = arrayOf(
                    DialogBtn("Go", { d -> d.dismiss() }),
                    DialogBtn("Share", { d -> d.dismiss() }),
                    DialogBtn("Podcast", { d -> d.dismiss() }),
                    DialogBtn("Blog", { d -> d.dismiss() }))
            )

            dlg.show()
        }
    }
    fun handlePost() {
        val btn = findView<Button>(R.id.tryout_post_rss)

        btn.setOnClickListener {

            //val post = imageLinkPost("Sunrise", "http://fitdeck.com/Portals/24254/images/Sunrise.jpg")
            //val post = statusPost("Wow, the pope resigned. The last time was hundreds of years ago")
            val post = linkPost("Android Rivers 1.08 is launched", "http://goo.gl/kShgp")
            //val post = simplePost("Greetings from Cairo Taher ${DateHelper.Now()} ", "There is no fighting today")
            val blg = Blog(null, "https://androidrivers.wordpress.com/xmlrpc.php", "username", "passwords")
            blg.newPost(post)
        }
    }

    fun handlePing() {
        val btn = findView<Button>(R.id.tryout_ping_rss_cloud)

        btn.setOnClickListener {
            val rpc = XMLRPCClient("http://rpc.rsscloud.org:5337/RPC2", "", "")
            val res = rpc.call("rssCloud.ping", "http://rivers.silverkeytech.com/blogs/android-rivers/rss.xml")
            Log.d(TAG, "Return content $res")
        }
    }

    fun handleGetAirportCodes() {
        val btn = findView<Button>(R.id.tryout_get_airport_codes)

        btn.setOnClickListener {
            val codes = getAirportCodes(this)
            toastee("There are ${codes.size} codes", Duration.LONG)
        }
    }

    fun handleInsertPodcast() {
        val btn = findView<Button>(R.id.tryout_insert_podcast_entry)

        btn.setOnClickListener {
            val res = savePodcastToDb("New Music From Tom Waits & Keith Richards, Ra Ra Riot, Villagers, More",
                    "http://podcastdownload.npr.org/anon.npr-podcasts/podcast/510019/168865214/npr_168865214.mp3",
                    "NPR: All Songs Considered Podcast",
                    "http://www.npr.org/rss/podcast.php?id=510019",
                    "/mnt/podcasts/storage.mp3",
                    "<p>On this edition of All Songs Considered we've got a bunch of new-year premieres for you",
                    "audio/mpeg",
                    47647644
            )

            if (res.isTrue())
                toastee("${res.value!!.title} - ${res.value!!.id}")
            else
                toastee("Exception ${res.exception?.message}")
        }
    }


    fun handleBookmarkCollectionCreation() {
        val btn = findView<Button>(R.id.tryout_bookmark_collection_btn)

        btn.setOnClickListener {
            var coll = BookmarkCollection()
            coll.title = "Channel 1"
            coll.kind = BookmarkCollectionKind.RIVER.toString()

            DatabaseManager.bookmarkCollection!!.create(coll)

            Log.d(TAG, "BookmarkCollection id ${coll.id}")

            var bk = DatabaseManager.bookmark!!.first()
            Log.d(TAG, "Loading bookmark with id ${bk.id}")
            bk.collection = coll

            DatabaseManager.bookmark!!.update(bk)

            bk = DatabaseManager.bookmark!!.first()
            Log.d(TAG, "Loading bookmark with id ${bk.id} and collection id ${bk.collection?.id}")

            var bookmarks = DatabaseManager.query().bookmark().all()

            for(b in bookmarks.values!!.iterator()){
                Log.d(TAG, "Bookmark ${b.title} - ${b.collection?.id}")
            }
        }
    }

    fun handleDownloadAtom() {
        val btn = findView<Button>(R.id.tryout_download_atom_btn)

        val list = ArrayList<Pair<String, String>>()
        list.add(Pair("Daring Fireball", "http://daringfireball.net/index.xml"))
        list.add(Pair("Nomadlife", "http://nomadone.nomadlife.org/atom.xml"))

        val names = Array<String>(list.size, { "" })
        var i = 0
        list.forEach {
            names[i] = it.first
            i++
        }

        btn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setItems(names) { p0, p1 ->
                val url = list.get(p1).second

                Log.d(TAG, "Opening $url")

                val ix = Intent(this@TryOutActivity, FeedActivity::class.java)
                ix.putExtra(Params.FEED_URL, url)
                ix.putExtra(Params.FEED_NAME, "Display ATOM Feeds")
                ix.putExtra(Params.FEED_LANGUAGE, "en")

                startActivity(ix)
            }

            dialog.create().show()
        }
    }

    fun handleDownloadRss() {
        val btn = findView<Button>(R.id.tryout_download_rss_btn)

        val list = ArrayList<Pair<String, String>>()
        list.add(Pair("Sample", "http://cyber.law.harvard.edu/rss/examples/rss2sample.xml"))
        list.add(Pair("Scripting", "http://static.scripting.com/rss.xml"))
        list.add(Pair("NPR Songs", "http://www.npr.org/rss/podcast.php?id=510019"))
        list.add(Pair("Times of India", "http://timesofindia.feedsportal.com/c/33039/f/533965/index.rss"))

        val names = Array<String>(list.size, { "" })
        var i = 0
        list.forEach {
            names[i] = it.first
            i++
        }

        btn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setItems(names) { p0, p1 ->
                val url = list.get(p1).second

                Log.d(TAG, "Opening $url")

                val ix = Intent(this@TryOutActivity, FeedActivity::class.java)
                ix.putExtra(Params.FEED_URL, url)
                ix.putExtra(Params.FEED_NAME, "Display RSS Feed")
                ix.putExtra(Params.FEED_LANGUAGE, "en")

                startActivity(ix)
            }

            dialog.create().show()
        }
    }

    fun handleDownloadGifImage() {
        val btn = findView<Button>(R.id.tryout_download_gif_image_btn)
        btn.setOnClickListener {
            Log.d(TAG, "Start downloading gif image")
            DownloadImageAsync(this).execute("http://www.science.mcmaster.ca/brainbee/images/stories/announcements/brainbee-logo-small.gif")
        }
    }

    fun handleDownloadJpgImage() {
        val btn = findView<Button>(R.id.tryout_download_jpg_image_btn)
        btn.setOnClickListener {
            Log.d(TAG, "Start downloading jpeg image")
            DownloadImageAsync(this).execute("http://www.fantom-xp.com/wallpapers/42/Iceberg_Very_Large.jpg")
        }
    }

    fun handleDownloadPngImage() {
        val btn = findView<Button>(R.id.tryout_download_png_image_btn)
        btn.setOnClickListener {
            Log.d(TAG, "Start downloading png image")
            DownloadImageAsync(this).execute("http://bartelme.at/material/news/NetNewsWire256.png")
        }
    }

    fun handleDownloadFile() {
        val btn = findView<Button>(R.id.tryout_download_file_btn)
        btn.setOnClickListener {
            Log.d(TAG, "Start downloading file")

            val messenger = Messenger(object : Handler(){
                override fun handleMessage(msg: Message) {
                    var path = msg.obj as String

                    if (msg.arg1 == android.app.Activity.RESULT_OK && !path.isNullOrBlank()){
                        toastee("File is successfully downloaded at $path")
                    }else{
                        toastee("Download failed")
                    }
                }
            })

            val ix = Intent(this, DownloadService::class.java)
            ix.putExtra(Params.DOWNLOAD_URL, "http://podcastdownload.npr.org/anon.npr-podcasts/podcast/13/166038315/npr_166038315.mp3")
            ix.putExtra(Params.MESSENGER, messenger)
            this.startService(ix)
        }
    }

    fun handleCreateBookmarkTable() {
        val btn = findView<Button>(R.id.tryout_setup_bookmark_table_btn)

        btn.setOnClickListener {
            val total = DatabaseManager.query().bookmark().byKind(BookmarkKind.RIVER, SortingOrder.NONE)

            if (total.exist)
                toastee("all ${total.values?.count()}", Duration.LONG)
            else
                toastee("There is no record", Duration.LONG)
        }
    }

    fun handleInsertToBookmarkTable() {
        val btn = findView<Button>(R.id.tryout_insert_data_bookmark_table_btn)

        btn.setOnClickListener {
            var bk = Bookmark()
            bk.title = "good morning america"
            bk.url = "http://www.cnn.com " + Random().nextInt()
            bk.kind = "book"
            DatabaseManager.bookmark!!.create(bk)
        }
    }

    var counter: Int = 1

    fun handleHandleNotification() {
        var btn = findView<Button>(R.id.tryout_show_notification_btn)
        btn.setOnClickListener {

            var notificationIntent = Intent(Intent.ACTION_MAIN)
            notificationIntent.setClass(applicationContext!!, MainWithFragmentsActivity::class.java)
            notificationIntent.putExtra(Params.DOWNLOAD_LOCATION_PATH, "Location PATH")

            var contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

            counter++

            var notification = NotificationCompat.Builder(this)
                    //.setContent()
                    .setTicker("This is the ticker")
            ?.setContentTitle("Android Rivers")
            ?.setContentText("Downloading a file")
            ?.setSmallIcon(android.R.drawable.gallery_thumb)
            ?.setProgress(100, 10, true)
            ?.setWhen(System.currentTimeMillis())
            ?.setContentIntent(contentIntent)
            ?.build()

            notification!!.contentView = RemoteViews(applicationContext!!.packageName, R.layout.notification_download_progress)

            notification.contentView!!.setImageViewResource(R.id.notification_download_progress_status_icon, android.R.drawable.btn_star);
            notification.contentView!!.setProgressBar(R.id.notification_download_progress_status_progress, 100, 10, false)
            notification.contentView!!.setTextViewText(R.id.notification_download_progress_status_text, "Download in progress")

            var nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(counter, notification)

            var thread = Thread(Runnable {
                for(i in 11..100){
                    notification.contentView!!.setProgressBar(R.id.notification_download_progress_status_progress, 100, i, false)
                    nm.notify(counter, notification)

                    Log.d(TAG, "We are progressing $i / 100")
                    try{
                        Thread.sleep(50)
                    }
                    catch(e: InterruptedException){
                        Log.d(TAG, "Exception ${e.message}")
                    }
                }
                //nm.cancel(counter);
            })

            thread.run()
        }
    }

    fun handleOutliner() {
        var btn = findView<Button>(R.id.tryout_show_outline_btn)
        btn.setOnClickListener {

            val url = "http://scripting.com/toc.opml"

            DownloadOpmlAsync(this)
                    .executeOnProcessedCompletion({
                res ->
                if (res.isTrue()){
                    var intent = Intent(Intent.ACTION_MAIN)
                    intent.setClass(applicationContext!!, OutlinerActivity::class.java)
                    intent.putExtra(Params.OUTLINES_DATA, res.value!!)

                    startActivity(intent)
                }
                else{
                    toastee("Downloading url fails becaue of ${res.exception?.message}", Duration.LONG)
                }
            }, { outline -> outline.text != "<rules>" })
                    .execute(url)

            //val url = "http://opmlviewer.com/Content/Directories.opml"
            //val url = "http://static.scripting.com/denver/wo/dave/2012/11/22/archive018.opml"
        }
    }

    fun handleRiverJsWithOpmlSource() {
        var btn = findView<Button>(R.id.tryout_download_riverjs_with_opml_btn)

        btn.setOnClickListener(object: OnClickListener{
            override fun onClick(p0: View) {
                val url = "http://hobieu.apphb.com/api/1/samples/riverjswithopml"

                var i = Intent(this@TryOutActivity, RiverActivity::class.java)
                i.putExtra(Params.RIVER_URL, url)
                i.putExtra(Params.RIVER_NAME, "Sample River with OPML")
                i.putExtra(Params.RIVER_LANGUAGE, "en")

                startActivity(i)
            }
        })
    }

    fun handleDownloadRecursiveOpml() {
        var btn = findView<Button>(R.id.tryout_download_recursive_opml_btn)

        btn.setOnClickListener(object: OnClickListener{
            override fun onClick(p0: View) {
                var req: String? = ""
                val url = "http://opmlviewer.com/Content/Directories.opml"

                try{
                    req = HttpRequest.get(url)?.body()
                }
                catch(e: HttpRequestException){
                    Log.d(TAG, "Error in downloading OPML $url ${e.cause}")
                    toastee("Error in downloading OPML from $url")
                }

                Log.d(TAG, "Text : $req")

                val opml = transformXmlToOpml(req?.replace("<?xml version=\"1.0\" encoding=\"utf-8\" ?>", ""))

                if(opml.isTrue()){
                    val sorted = opml.value!!.traverse()
                    toastee("Opml parsing is Great ${sorted.count()}")
                }   else{
                    Log.d(TAG, "Error in parsing opml  ${opml.exception?.message}")
                    toastee("Error in parsing opml ${opml.exception?.message}")
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

