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

package com.silverkeytech.android_rivers

import android.content.Context
import android.util.Log
import org.holoeverywhere.app.Activity
import org.holoeverywhere.preference.SharedPreferences

class Preferences{
    companion object{
        val CONTENT: String = "CONTENT"
        val CONTENT_RIVER_BOOKMARKS_SORTING: String = "CONTENT_RIVER_BOOKMARKS_SORTING"

        val SETUP: String = "SETUP"
        val SETUP_DOWNLOAD_DEFAULT_RIVERS: String = "SETUP_DOWNLOAD_DEFAULT_RIVERS"

        val VISUAL: String = "PREFERENCE_VISUAL"
        val VISUAL_LIST_TEXT_SIZE: String = "PREFERENCE_VISUAL_LIST_TEXT_SIZE"
        val VISUAL_THEME: String = "PREFERENCE_VISUAL_THEME"

        val STORED: String = "STORED"
        val STORED_KAYAK_CITY: String = "STORED_KAYAK_CITY"
        val STORED_GOOGLE_NEWS_COUNTRY: String = "STORED_GOOGLE_NEWS_COUNTRY"
        val STORED_CRAIGS_LIST_CITY: String = "STORED_CRAIGS_LIST_CITY"
    }
}

class PreferenceValue{
    companion object {
        val SORT_DESC: Int = 100
        val SORT_NONE: Int = 101
        val SORT_ASC: Int = 102
    }
}

class PreferenceDefaults{
    companion object {
        val CONTENT_RIVER_BOOKMARKS_SORTING: Int = PreferenceValue.SORT_NONE
        val CONTENT_BOOKMARK_COLLECTION_LATEST_DATE_FILTER_IN_DAYS: Int = 30
        val CONTENT_BOOKMARK_COLLECTION_MAX_ITEMS_FILTER: Int = 12

        val CONTENT_OUTLINE_HELP_SOURCE: String = "http://hobieu.apphb.com/api/1/opml/androidrivershelp"
        val CONTENT_OUTLINE_MORE_NEWS_SOURCE: String = "http://hobieu.apphb.com/api/1/opml/root"

        val CONTENT_BODY_MAX_LENGTH: Int = 280
        val CONTENT_BODY_ARABIC_MAX_LENGTH: Int = 220

        val RSS_LATEST_DATE_FILTER_IN_DAYS: Int = 30
        val RSS_MAX_ITEMS_FILTER: Int = 20

        val SETUP_DOWNLOAD_DEFAULT_RIVERS: Boolean = true

        val VISUAL_LIST_TEXT_SIZE: Int = 20
        val VISUAL_THEME: Int = R.style.Holo_Theme

        val LINK_SHARE_TITLE_MAX_LENGTH: Int = 80

        val OPML_NEWS_SOURCES_LISTING_CACHE_IN_MINUTES: Int = 60 * 24


        val STANDARD_NEWS_COLOR: Int = android.graphics.Color.GRAY
        val STANDARD_NEWS_IMAGE: Int = android.graphics.Color.CYAN
        val STANDARD_NEWS_PODCAST: Int = android.graphics.Color.MAGENTA
        val STANDARD_NEWS_SOURCE: Int = android.graphics.Color.BLUE
    }
}

fun Activity.getContentPref(): ContentPreference =
        ContentPreference(this.getSharedPreferences(Preferences.CONTENT, Context.MODE_PRIVATE))

fun Activity.getSetupPref(): SetupPreference =
        SetupPreference(this.getSharedPreferences(Preferences.SETUP, Context.MODE_PRIVATE))

fun Activity.getVisualPref(): VisualPreference =
        VisualPreference(this.getSharedPreferences(Preferences.VISUAL, Context.MODE_PRIVATE))

fun Activity.getStoredPref(): StoredPreference =
        StoredPreference(this.getSharedPreferences(Preferences.STORED, Context.MODE_PRIVATE))

class StoredPreference(val pref: SharedPreferences){
    companion object {
        val TAG: String = StoredPreference::class.java.simpleName
    }

    var kayakCity: String
        get() = pref.getString(Preferences.STORED_KAYAK_CITY, "")!!
        set(city: String) {
            if (city.length == 0)
                return
            var edit = pref.edit()
            edit.putString(Preferences.STORED_KAYAK_CITY, city)
            edit.commit()
            Log.d(TAG, "Saving kayak city $city")
        }


    var craigsListCity: String
        get() = pref.getString(Preferences.STORED_CRAIGS_LIST_CITY, "")!!
        set(city: String) {
            if (city.length == 0)
                return
            var edit = pref.edit()
            edit.putString(Preferences.STORED_CRAIGS_LIST_CITY, city)
            edit.commit()
            Log.d(TAG, "Saving craigslist city $city")
        }

    var googleNewsCountry: String
        get() = pref.getString(Preferences.STORED_GOOGLE_NEWS_COUNTRY, "")!!
        set(country: String) {
            if (country.length == 0)
                return
            var edit = pref.edit()
            edit.putString(Preferences.STORED_GOOGLE_NEWS_COUNTRY, country)
            edit.commit()
            Log.d(TAG, "Saving google news country $country")
        }
}

class ContentPreference(val pref: SharedPreferences){
    companion object {
        val TAG: String = ContentPreference::class.java.simpleName
    }

    var riverBookmarksSorting: Int
        get() = pref.getInt(Preferences.CONTENT_RIVER_BOOKMARKS_SORTING, PreferenceDefaults.CONTENT_RIVER_BOOKMARKS_SORTING)
        set(sort: Int) {
            var edit = pref.edit()
            edit.putInt(Preferences.CONTENT_RIVER_BOOKMARKS_SORTING, sort)
            edit.commit()
            Log.d(TAG, "Saving bookmark sorting value $sort")
        }
}

class SetupPreference(val pref: SharedPreferences){
    companion object {
        val TAG: String = SetupPreference::class.java.simpleName
    }

    var downloadDefaultRiversIfNecessary: Boolean
        get() = pref.getBoolean(Preferences.SETUP_DOWNLOAD_DEFAULT_RIVERS, PreferenceDefaults.SETUP_DOWNLOAD_DEFAULT_RIVERS)
        set(yes: Boolean){
            val edit = pref.edit()
            edit.putBoolean(Preferences.SETUP_DOWNLOAD_DEFAULT_RIVERS, yes)
            edit.commit()
            Log.d(TAG, "Saving Download Default Rivers $yes")
        }
}

class VisualPreference (val pref: SharedPreferences){
    companion object {
        val TAG: String = VisualPreference::class.java.simpleName
    }

    var listTextSize: Int
        get() = pref.getInt(Preferences.VISUAL_LIST_TEXT_SIZE, PreferenceDefaults.VISUAL_LIST_TEXT_SIZE)
        set(size: Int) {
            if (size < 12 || size > 30) //http://developer.android.com/design/style/typography.html
                return

            var edit = pref.edit()
            edit.putInt(Preferences.VISUAL_LIST_TEXT_SIZE, size)
            edit.commit()
            Log.d(TAG, "Saving list text size $size")
        }

    val theme: Int
        get(){
            val currentTheme = pref.getInt(Preferences.VISUAL_THEME, PreferenceDefaults.VISUAL_THEME)

            when (currentTheme){
                R.style.Theme_Sherlock_Light_DarkActionBar -> return R.style.Holo_Theme_Light
                R.style.Theme_Sherlock -> return R.style.Holo_Theme
                R.style.Theme_Sherlock_Light -> return R.style.Holo_Theme_Light
                else -> return currentTheme
            }
        }

    fun switchTheme() {
        val currentTheme = theme

        var newTheme = when(currentTheme){
            R.style.Holo_Theme -> R.style.Holo_Theme_Light
            R.style.Holo_Theme_Light -> R.style.Holo_Theme
            else -> R.style.Holo_Theme
        }

        var edit = pref.edit()
        edit.putInt(Preferences.VISUAL_THEME, newTheme)
        edit.commit()
        Log.d(TAG, "Switch theme $newTheme")
    }
}