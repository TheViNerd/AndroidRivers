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

package com.silverkeytech.news_engine.syndications.rss_rdf

class RdfRssBuilder(){
    private val rdf: Rdf = Rdf()
    val channel: ChannelBuilder = ChannelBuilder(rdf)

    fun build(): Rdf {
        return rdf
    }

    class ChannelBuilder(private val rdf: Rdf){
        init {
            rdf.channel = Channel()
        }

        var item: ItemBuilder = ItemBuilder(Item())

        fun setTitle(title: String) {
            rdf.channel.title = title
        }

        fun setDescription(description: String) {
            rdf.channel.description = description
        }

        fun setLink(link: String) {
            rdf.channel.link = link
        }

        fun setDcPublisher(publisher: String) {
            rdf.channel.dc.publisher = publisher
        }

        fun setDcLanguage(lang: String) {
            rdf.channel.dc.language = lang
        }

        fun setDcRights(rights: String) {
            rdf.channel.dc.rights = rights
        }

        fun setDcTitle(title: String) {
            rdf.channel.dc.title = title
        }

        fun setDcCreator(creator: String) {
            rdf.channel.dc.creator = creator
        }

        fun setDcSource(source: String) {
            rdf.channel.dc.source = source
        }

        fun startItem() {
            item = ItemBuilder(Item())
        }

        fun endItem() {
            rdf.item.add(item.data)
        }
    }

    class ItemBuilder(val data: Item){
        fun setTitle(title: String) {
            data.title = title
        }

        fun setLink(link: String) {
            data.link = link
        }

        fun setDescription(description: String) {
            data.description = description
        }

        fun setAbout(about: String) {
            data.about = about
        }

        fun setDcDate(date: String) {
            data.dc.date = date
        }

        fun setDcLanguage(lang: String) {
            data.dc.language = lang
        }

        fun setDcRights(rights: String) {
            data.dc.rights = rights
        }

        fun setDcSource(source: String) {
            data.dc.source = source
        }

        fun setDcTitle(title: String) {
            data.dc.title = title
        }
    }
}