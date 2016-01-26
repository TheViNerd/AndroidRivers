/*
Android Rivers is an app, read and discover news using RiverJs, RSS and OPML format.
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

package com.silverkeytech.android_rivers.creators

import java.util.*

data class EditionAndLanguage(val edition: String, val lang: String)


fun getEditionsAndLanguages(): TreeMap<String, EditionAndLanguage> {
    val mp = TreeMap<String, EditionAndLanguage>()

    mp.put("Argentina", EditionAndLanguage("es_ar", "es"))
    mp.put("Australia", EditionAndLanguage("au", "en"))
    mp.put("België", EditionAndLanguage("nl_be", "nl"))
    mp.put("Belgique", EditionAndLanguage("fr_be", "fr"))
    mp.put("Botswana", EditionAndLanguage("en_bw", "en"))
    mp.put("Brasil", EditionAndLanguage("pt-BR", "pt-BR"))
    mp.put("Canada English", EditionAndLanguage("ca", "en"))
    mp.put("Canada French", EditionAndLanguage("fr_ca", "fr"))
    mp.put("Chile", EditionAndLanguage("es_cl", "es"))
    mp.put("Colombia", EditionAndLanguage("es_co", "es"))
    mp.put("Cuba", EditionAndLanguage("es_cu", "es"))
    mp.put("Česká republika", EditionAndLanguage("cs_cz", "cs"))
    mp.put("Deutschland", EditionAndLanguage("de", "de"))
    mp.put("España", EditionAndLanguage("es", "es"))
    mp.put("Estados Unidos", EditionAndLanguage("es_us", "es"))
    mp.put("Ethiopia", EditionAndLanguage("en_et", "en"))
    mp.put("France", EditionAndLanguage("fr", "fr"))
    mp.put("Ghana", EditionAndLanguage("en_gh", "en"))
    mp.put("India", EditionAndLanguage("In", "en"))
    mp.put("Ireland", EditionAndLanguage("en_ie", "en"))
    mp.put("Israel English", EditionAndLanguage("en_il", "en"))
    mp.put("Italia", EditionAndLanguage("it", "it"))
    mp.put("Kenya", EditionAndLanguage("en_ke", "en"))
    mp.put("Magyarország", EditionAndLanguage("hu_hu", "hu"))
    mp.put("Malaysia", EditionAndLanguage("en_my", "en"))
    mp.put("Maroc", EditionAndLanguage("fr_ma", "fr"))
    mp.put("México", EditionAndLanguage("es_mx", "es"))
    mp.put("Namibia", EditionAndLanguage("en_na", "en"))
    mp.put("Nederland", EditionAndLanguage("nl_nl", "nl"))
    mp.put("New Zealand", EditionAndLanguage("nz", "en"))
    mp.put("Nigeria", EditionAndLanguage("en_ng", "en"))
    mp.put("Norge", EditionAndLanguage("no_no", "no"))
    mp.put("Österreich", EditionAndLanguage("de_at", "de"))
    mp.put("Pakistan", EditionAndLanguage("en_pk", "en"))
    mp.put("Perú", EditionAndLanguage("es_pe", "es"))
    mp.put("Philippines", EditionAndLanguage("en_ph", "en"))
    mp.put("Polska", EditionAndLanguage("pl_pl", "pl"))
    mp.put("Portugal", EditionAndLanguage("pt-PT_pt", "pt-PT"))
    mp.put("Schweiz", EditionAndLanguage("de_ch", "de"))
    mp.put("Sénégal", EditionAndLanguage("fr_sn", "fr"))
    mp.put("Singapore", EditionAndLanguage("en_sg", "en"))
    mp.put("South Africa", EditionAndLanguage("en_za", "en"))
    mp.put("Suisse", EditionAndLanguage("fr_ch", "fr"))
    mp.put("Sverige", EditionAndLanguage("sv_se", "sv"))
    mp.put("Tanzania", EditionAndLanguage("en_tz", "en"))
    mp.put("Türkiye", EditionAndLanguage("tr_tr", "tr"))
    mp.put("U.K.", EditionAndLanguage("uk", "en"))
    mp.put("U.S.", EditionAndLanguage("us", "en"))
    mp.put("Uganda", EditionAndLanguage("en_ug", "en"))
    mp.put("Venezuela", EditionAndLanguage("es_ve", "es"))
    mp.put("Việt Nam (Vietnam)", EditionAndLanguage("vi_vn", "vi"))
    mp.put("Zimbabwe", EditionAndLanguage("en_zw", "en"))
    mp.put("Ελλάδα (Greece)", EditionAndLanguage("el_gr", "el"))
    mp.put("Россия (Russia)", EditionAndLanguage("ru_ru", "ru"))
    mp.put("Србија (Serbia)", EditionAndLanguage("sr_rs", "sr"))
    mp.put("Украина / русский (Ukraine)", EditionAndLanguage("ru_ua", "ru"))
    mp.put("Україна / українська (Ukraine)", EditionAndLanguage("uk_ua", "uk"))
    mp.put("ישראל (Israel)", EditionAndLanguage("iw_il", "iw"))
    mp.put("الإمارات (UAE)", EditionAndLanguage("ar_ae", "ar"))
    mp.put("السعودية (KSA)", EditionAndLanguage("ar_sa", "ar"))
    mp.put("إصدار العالم العربي", EditionAndLanguage("ar_me", "ar"))
    mp.put("لبنان (Lebanon)", EditionAndLanguage("ar_lb", "ar"))
    mp.put("مصر (Egypt)", EditionAndLanguage("ar_eg", "ar"))
    mp.put("हिन्दी (India)", EditionAndLanguage("hi_in", "hi"))
    mp.put("தமிழ்(India)", EditionAndLanguage("ta_in", "ta"))
    mp.put("తెలుగు (India)", EditionAndLanguage("te_in", "te"))
    mp.put("മലയാളം (India)", EditionAndLanguage("ml_in", "ml"))
    mp.put("한국 (Korea)", EditionAndLanguage("kr", "kr"))
    mp.put("中国 (China)", EditionAndLanguage("cn", "zh-CN"))
    mp.put("台灣 (Taiwan)", EditionAndLanguage("tw", "zh-TW"))
    //mp.put("日本 (Japan)", EditionAndLanguage("jp", "jp"))
    mp.put("香港 (Hong Kong)", EditionAndLanguage("hk", "zh-TW"))

    return mp
}