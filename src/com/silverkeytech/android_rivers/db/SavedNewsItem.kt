package com.silverkeytech.android_rivers.db

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.*

val SAVED_NEWS_ITEM_ID: String = "id"
val SAVED_NEWS_ITEM_TITLE: String = "title"
val SAVED_NEWS_ITEM_EXCERPT: String = "excerpt"
val SAVED_NEWS_ITEM_CONTENT: String = "content"
val SAVED_NEWS_ITEM_URL: String = "url"
val SAVED_NEWS_ITEM_LANGUAGE: String = "language"
val SAVED_NEWS_ITEM_KIND: String = "kind"
val SAVED_NEWS_ITEM_DATE_CREATED: String = "date_created"


@DatabaseTable class SavedNewsItem(){

    @DatabaseField(generatedId = true, columnName = "id")
    var id: Int = 0

    @DatabaseField(canBeNull = false, columnName = "title", width = 255, dataType = DataType.STRING)
    var title: String = ""

    @DatabaseField(canBeNull = true, columnName = "excerpt", width = 550, dataType = DataType.LONG_STRING)
    var excerpt: String = ""

    @DatabaseField(canBeNull = true, columnName = "content", width = 550, dataType = DataType.LONG_STRING)
    var content: String = ""

    @DatabaseField(canBeNull = false, uniqueIndex = true, columnName = "url", width = 550, dataType = DataType.LONG_STRING)
    var url: String = ""

    @DatabaseField(canBeNull = false, columnName = "language", width = 8, dataType = DataType.STRING)
    var language: String = "en"

    @DatabaseField(canBeNull = false, columnName = "kind")
    var kind: String = ""

    @DatabaseField(canBeNull = false, columnName = "date_created", dataType = DataType.DATE)
    var dateCreated: Date = Calendar.getInstance().time

    override fun toString(): String {
        return title
    }
}
