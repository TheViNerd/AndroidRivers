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

package com.silverkeytech.news_engine.xml

import com.thebuzzmedia.sjxp.XMLParser
import com.thebuzzmedia.sjxp.rule.DefaultRule
import com.thebuzzmedia.sjxp.rule.ParsingMode

fun <T: Any> textRule(path: String, action: (text: String, rss: T) -> Unit): DefaultRule<T> {
    return object: DefaultRule<T> (ParsingMode.CHARACTER, path){
        override fun handleParsedCharacters(parser: XMLParser<T>?, text: String?, userObject: T?) {
            if (!text.isNullOrBlank())
                action(text!!, userObject!!)
        }
    }
}

fun <T: Any> attributeRule(path: String, action: (attrName: String, attrValue: String, rss: T) -> Unit, vararg attrNames: String?): DefaultRule<T> {
    return object: DefaultRule<T> (ParsingMode.ATTRIBUTE, path, *attrNames){
        override fun handleParsedAttribute(parser: XMLParser<T>?, index: Int, value: String?, userObject: T?) {
            if (!value.isNullOrBlank()){
                val attrName = this.attributeNames!!.get(index)
                action(attrName, value!!, userObject!!)
            }
        }
    }
}

fun <T: Any> tagRule(path: String, action: (isStartTag: Boolean, rss: T) -> Unit): DefaultRule<T> {
    return object: DefaultRule<T> (ParsingMode.TAG, path){
        override fun handleTag(parser: XMLParser<T>?, isStartTag: Boolean, userObject: T?) {
            action(isStartTag, userObject!!)
        }
    }
}