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

package com.silverkeytech.news_engine.outlines

import java.util.*

class OpmlBuilder{
    companion object {
        val TAG: String = OpmlBuilder::class.java.simpleName
    }

    val opml : Opml = Opml()
    val body : BodyBuilder = BodyBuilder(opml)
    val head : HeadBuilder = HeadBuilder(opml)

    class HeadBuilder(val opml : Opml){
        init {
            opml.head = Head()
        }
        fun setTitle(title : String) { opml.head!!.title = title }
        fun setDateCreated(date : String) { opml.head!!.dateCreated = date }
        fun setDateModified(date : String) {  opml.head!!.dateModified = date }
        fun setOwnerName(owner: String) { opml.head!!.ownerName = owner }
        fun setOwnerEmail(email : String) { opml.head!!.ownerEmail = email }
    }

    class BodyBuilder (val opml : Opml){
        companion object {
            val TAG: String = BodyBuilder::class.java.simpleName
        }

        var currentLevel  = 0
        var currentOutline = Outline()
        var parentOutline : Outline? = null
        var parents : Stack<Outline> =  Stack<Outline>()
        var rootOutlines : ArrayList<Outline>? = null
        init {
            opml.body = Body()
            rootOutlines = opml.body!!.outline
        }

        fun startLevel(level : Int){
            if (level == 0){
                currentLevel = 0
                currentOutline = Outline()
                rootOutlines!!.add(currentOutline)
                parentOutline = null
                parents.clear()
            }
            else if (level > currentLevel){
                if (parentOutline != null)
                    parents.push(parentOutline!!)

                parentOutline = currentOutline
                currentOutline = Outline()
                parentOutline!!.outline!!.add(currentOutline)
                currentLevel = level
            }
            else if (level == currentLevel){
                currentOutline = Outline()
                if (parentOutline != null)      {
                    parentOutline!!.outline!!.add(currentOutline)
                }
            } else {  //level < current level
                throw Exception("level($level) < currentLevel($currentLevel)")
            }
        }

        fun endLevel(level : Int){
            if (level < currentLevel && level > 0){
                parentOutline = parents.pop()
                currentLevel = level
            }
            else if (level == 0){
                currentLevel = 0
            }
            else {
                //do nothing
            }
        }

        fun setText(text : String) { currentOutline.text = text }
        fun setUrl(url : String) { currentOutline.url = url }
        fun setXmlUrl(xmlUrl : String) { currentOutline.xmlUrl = xmlUrl }
        fun setHtmlUrl(htmlUrl : String) { currentOutline.htmlUrl = htmlUrl }
        fun setOpmlUrl(opmlUrl : String) { currentOutline.opmlUrl = opmlUrl }
        fun setType(tp : String) { currentOutline.outlineType = tp }
        fun setLanguage(lang : String) { currentOutline.language = lang }
        fun setName(name : String) { currentOutline.name = name }
    }

    fun build() : Opml{
        return opml
    }
}