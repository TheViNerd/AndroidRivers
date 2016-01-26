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

package com.silverkeytech.news_engine.syndications.rss

class RssBuilder(){
    private val rss: Rss = Rss()
    val channel: ChannelBuilder = ChannelBuilder(rss)

    fun build(): Rss {
        return rss
    }

    class ChannelBuilder(private val rss: Rss){
        init {
            rss.channel = Channel()
        }

        var item: ItemBuilder = ItemBuilder(Item())

        fun setTitle(title: String) {
            rss.channel!!.title = title;
        }

        fun setLink(link: String) {
            rss.channel!!.link = link
        }

        fun setDescription(description: String) {
            rss.channel!!.description = description
        }

        fun setLanguage(lang: String) {
            rss.channel!!.language = lang
        }

        fun setPubDate(pubDate: String) {
            rss.channel!!.pubDate = pubDate
        }

        fun setLastBuildDate(lastBuildDate: String) {
            rss.channel!!.lastBuildDate = lastBuildDate
        }

        fun setDocs(docs: String) {
            rss.channel!!.docs = docs
        }

        fun setGenerator(generator: String) {
            rss.channel!!.generator = generator
        }

        fun setManagingDirector(managing: String) {
            rss.channel!!.managingEditor = managing
        }

        fun setWebMaster(master: String) {
            rss.channel!!.webMaster = master
        }

        fun setTitle(ttl: Int) {
            rss.channel!!.ttl = ttl
        }

        fun getCloud(): Cloud {
            if (rss.channel!!.cloud == null)
                rss.channel!!.cloud = Cloud()

            return rss.channel!!.cloud!!
        }

        fun startItem() {
            item = ItemBuilder(Item())
        }

        fun endItem() {
            rss.channel!!.item!!.add(item.data)
        }

        class ItemBuilder(val data: Item){
            fun setTitle(title: String) {
                data.title = title
            }

            fun setLink(link: String) {
                data.link = link
            }

            fun setDescription(desc: String) {
                data.description = desc
            }

            fun setAuthor(author: String) {
                data.author = author
            }

            fun setGuid(guid: String) {
                data.guid = guid
            }

            fun setIsPermaLink(isPermaLink: String) {
                data.isPermalink = isPermaLink
            }

            fun setPubDate(pubDate: String) {
                data.pubDate = pubDate
            }

            fun setExtension(name: String, content: String) {
                if (!data.extensions!!.containsKey(name))
                    data.extensions!!.put(name, content);
            }

            fun getEnclosure(): Enclosure {
                if (data.enclosure == null)
                    data.enclosure = Enclosure()

                return data.enclosure!!
            }
        }
    }
}

