package com.jacekpietras.zoo.core.text

import com.jacekpietras.zoo.core.R

object Dictionary {

    fun String.findReadableName(): Text =
        when (this) {
            "wejscie" -> Text(R.string.wejscie)
            "wyjscie" -> Text(R.string.wyjscie)
            "bazanty-papugi" -> Text(R.string.bazanty_papugi)
            "pazurzatki" -> Text(R.string.pazurzatki)
            "flamingi" -> Text(R.string.flamingi)
            "surykatki" -> Text(R.string.surykatki)
            "ptaki-w-wolierach" -> Text(R.string.ptaki_w_wolierach)
            "ptaki-wodne-duzy-staw" -> Text(R.string.ptaki_wodne_duzy_staw)
            "wydry" -> Text(R.string.wydry)
            "srednie-koty" -> Text(R.string.srednie_koty)
            "jezozwierze-psowate" -> Text(R.string.jezozwierze_psowate)
            "wilki" -> Text(R.string.wilki)
            "tapiry-strusie-zebry" -> Text(R.string.tapiry_strusie_zebry)
            "male-drapiezniki" -> Text(R.string.male_drapiezniki)
            "wielkie-koty" -> Text(R.string.wielkie_koty)
            "ptaki-wodne-maly-staw" -> Text(R.string.ptaki_wodne_maly_staw)
            "wielblady-i-gwanko" -> Text(R.string.wielblady_i_gwanko)
            "kob-sniady" -> Text(R.string.kob_sniady)
            "eland" -> Text(R.string.eland)
            "pingwiny" -> Text(R.string.pingwiny)
            "kangury-i-strusie-emu" -> Text(R.string.kangury_i_strusie_emu)
            "addaks" -> Text(R.string.addaks)
            "takin-miszmi" -> Text(R.string.takin_miszmi)
            "renifer" -> Text(R.string.renifer)
            "sitatunga" -> Text(R.string.sitatunga)
            "kob-liczi" -> Text(R.string.kob_liczi)
            "uchatka-patagonska" -> Text(R.string.uchatka_patagonska)
            "panda-mala" -> Text(R.string.panda_mala)
            "arui" -> Text(R.string.arui)
            "zyrafy" -> Text(R.string.zyrafy)
            "manul" -> Text(R.string.manul)
            "kulany" -> Text(R.string.kulany)
            "jelenie" -> Text(R.string.jelenie)
            "nilgau-markury" -> Text(R.string.nilgau_markury)
            "daniele-barasinga" -> Text(R.string.daniele_barasinga)
            "gastronomia" -> Text(R.string.gastronomia)
            "lemury" -> Text(R.string.lemury)
            "orly-1",
            "orly-2" -> Text(R.string.orly)
            "osly" -> Text(R.string.osly)
            "milu" -> Text(R.string.milu)
            "myszojelen" -> Text(R.string.myszojelen)
            "mini-zoo" -> Text(R.string.mini_zoo)
            "ul" -> Text(R.string.ul)
            "kondory" -> Text(R.string.kondory)
            "zurawie-mandzurskie" -> Text(R.string.zurawie_mandzurskie)
            "nocny-pawilon" -> Text(R.string.nocny_pawilon)
            "srednie-drapiezniki" -> Text(R.string.srednie_drapiezniki)
            "sowy" -> Text(R.string.sowy)
            "przewalskiego" -> Text(R.string.przewalskiego)
            "wikunie" -> Text(R.string.wikunie)
            "hipopotamy" -> Text(R.string.hipopotamy)
            "malpy" -> Text(R.string.malpy)
            "egzotarium" -> Text(R.string.egzotarium)
            "slonie" -> Text(R.string.slonie)
            "wc-1",
            "wc-2",
            "wc-3",
            "wc-4",
            "wc-5" -> Text(R.string.wc)
            else -> Text(this)
        }
}