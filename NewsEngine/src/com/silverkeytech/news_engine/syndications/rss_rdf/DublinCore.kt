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

import java.util.Date
import com.silverkeytech.news_engine.syndications.RssDate
import com.silverkeytech.news_engine.syndications.ParsedDateFormat
import com.silverkeytech.news_engine.syndications.parseDate
import com.silverkeytech.news_engine.syndications.getDateInFormat

class DublinCore(){
    var date: String? = null
    var language: String? = null
    var rights: String? = null
    var source: String? = null
    var title: String? = null
    var publisher: String? = null
    var creator: String? = null
    var subject: String? = null

    fun getDate(): RssDate {
        return parseDate(date)
    }
    fun geDateInFormat(status: ParsedDateFormat): Date? {
        return getDateInFormat(status, date!!)
    }
}