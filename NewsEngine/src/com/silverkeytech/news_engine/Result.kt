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
package com.silverkeytech.news_engine

class None(){
}

class Result<T: Any>(p1: T?, p2: Exception? = null){
    companion object{
        //return True result
        fun <T: Any> right(value: T?): Result<T> {
            return Result(value)
        }

        //return false result
        fun <T: Any> wrong(exception: Exception?): Result<T> {
            return Result(null, exception)
        }
    }

    val value: T? = p1
    val exception: Exception? = p2

    fun isTrue(): Boolean {
        return exception == null
    }

    fun isFalse(): Boolean {
        return exception != null
    }
}