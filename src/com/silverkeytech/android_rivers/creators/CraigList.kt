package com.silverkeytech.android_rivers.creators

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

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
data class CraigsListCity(
        val code: String,
        val areaId: String,
        val url: String,
        val location: String,
        val grouping: String
){
    override fun toString(): String {
        return "$code,$areaId,$url,$location,$grouping"
    }
}

abstract class LineParser<T>{
    fun parse(input: InputStream): ArrayList<T> {
        val r = BufferedReader(InputStreamReader(input))
        var x = r.readLine()

        val list = ArrayList<T>()

        while (x != null) {
            val city = parseLine(x)
            list.add(city)
            x = r.readLine()
        }

        return list
    }

    protected abstract fun parseLine(text: String): T
}

class CraigsListCityParser: LineParser<CraigsListCity>(){
    override fun parseLine(text: String): CraigsListCity {
        val sections = text.split(",")
        val code = sections[0].trim()
        val areaId = sections[1].trim()
        val url = sections[2].trim()
        val location = sections[3].trim().capitalize() + ", " + sections[4].trim().capitalize()
        var cat = ""
        for (i in 5..(sections.size - 1)) {
            cat += "${sections[i].trim()}, "
        }

        cat = cat.trim().replace("[,]+$".toRegex(), "");//trim the last character at the end

        return CraigsListCity(code, areaId, url, location, cat)
    }
}

data class CraigsListCategory(
        val code: String,
        val name: String
){
    override fun toString(): String {
        return "$code - $name"
    }
}

class CraigsListCategoryParser: LineParser<CraigsListCategory>(){

    override fun parseLine(text: String): CraigsListCategory {
        val sections = text.split(",")
        val code = sections[0].trim()
        var name = ""
        for (i in 1..(sections.size - 1)){
            name += "${sections[i].trim()}"
        }

        name = name.trim().replace("[,]+$".toRegex(), "");//trim the last character at the end

        return CraigsListCategory(code, name)
    }
}