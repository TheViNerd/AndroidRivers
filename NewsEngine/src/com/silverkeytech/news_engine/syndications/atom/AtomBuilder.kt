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

package com.silverkeytech.news_engine.syndications.atom

import java.util.*

class AtomBuilder (){
    val feed = Feed()
    var author : PersonElementBuilder = PersonElementBuilder(feed.author!!)
    var link : LinkElementBuilder = LinkElementBuilder(feed.link!!)
    val entry: EntryBuilder = EntryBuilder(feed)

    fun build() : Feed {
        return feed
    }

    fun setId(id : String){
        feed.id = id
    }

    fun setTitle(title : String){
        feed.title = title
    }

    fun setUpdated(updated : String){
        feed.updated = updated
    }

    fun setIcon(icon : String){
        feed.icon = icon
    }

    fun setLogo(logo : String){
        feed.logo = logo
    }

    fun setSubtitle(subTitle : String){
        feed.subtitle = subTitle
    }

    class PersonElementBuilder(val author : ArrayList<PersonElement>){
        private var person : PersonElement = PersonElement()

        fun startItem(){
            person = PersonElement()
        }

        fun endItem(){
            author.add(person)
        }

        fun setName(name : String){
            person.name = name
        }

        fun setUri(uri : String){
            person.uri = uri
        }

        fun setEmail(email : String){
            person.email = email
        }
    }

    class ContentElementBuilder(val content : ContentElement){
        fun setValue (value : String){
            content.value = value
        }

        fun setType(contentType : String){
            content.`type` = contentType
        }

        fun setSource(uri : String){
            content.src = uri
        }
    }

    class LinkElementBuilder(val links : ArrayList<LinkElement>){
        var link : LinkElement = LinkElement()

        fun startItem(){
            link = LinkElement()
        }

        fun endItem(){
            links.add(link)
        }

        fun setHref(href : String) {
            link.href = href
        }

        fun setRel(rel : String){
            link.rel = rel
        }

        fun setType(linkType : String){
            link.`type` = linkType
        }

        fun setHrefLang(lang : String){
            link.hreflang = lang
        }

        fun setTitle(title : String){
            link.title = title
        }

        fun setLength(length : Int){
            link.length = length
        }
    }

    class EntryBuilder(private val feed: Feed){
        var entry : Entry = Entry()
        var author : PersonElementBuilder = PersonElementBuilder(entry.author!!)
        var link : LinkElementBuilder = LinkElementBuilder(entry.link!!)
        var content : ContentElementBuilder = ContentElementBuilder(ContentElement())
        var summary : ContentElementBuilder = ContentElementBuilder(ContentElement())

        fun setId(id : String){
            entry.id = id
        }

        fun setTitle(title : String){
            entry.title = title
        }

        fun setUpdated(updated : String){
            entry.updated = updated
        }

        fun setPublished(published : String){
            entry.published = published
        }

        fun startContent(){
            entry.content = ContentElement()
            content = ContentElementBuilder(entry.content!!)
        }

        fun startSummary(){
            entry.summary = ContentElement()
            summary = ContentElementBuilder(entry.summary!!)
        }

        fun startItem(){
            entry = Entry()
            author = PersonElementBuilder(entry.author!!)
            link = LinkElementBuilder(entry.link!!)
        }

        fun endItem(){
            feed.entry!!.add(entry)
        }
    }
}