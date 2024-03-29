package com.jacekpietras.zoo.core.text

import com.jacekpietras.zoo.core.R

object Dictionary {

    fun String.findReadableName(): RichText =
        when {
            startsWith("wielkie-koty-") -> RichText(R.string.wielkie_koty)
            startsWith("orly-") -> RichText(R.string.orly)
            startsWith("wc-") -> RichText(R.string.wc)
            startsWith("wejscie") -> RichText(R.string.entry)
            startsWith("wyjscie") -> RichText(R.string.exit)
            else -> when (this) {
                "bazanty-papugi" -> RichText(R.string.bazanty_papugi)
                "pazurzatki" -> RichText(R.string.pazurzatki)
                "flamingi" -> RichText(R.string.flamingi)
                "surykatki" -> RichText(R.string.surykatki)
                "ptaki-w-wolierach" -> RichText(R.string.ptaki_w_wolierach)
                "ptaki-wodne-duzy-staw" -> RichText(R.string.ptaki_wodne_duzy_staw)
                "wydry" -> RichText(R.string.wydry)
                "srednie-koty" -> RichText(R.string.srednie_koty)
                "jezozwierze-psowate" -> RichText(R.string.jezozwierze_psowate)
                "wilki" -> RichText(R.string.wilki)
                "tapiry-strusie-zebry" -> RichText(R.string.tapiry_strusie_zebry)
                "male-drapiezniki" -> RichText(R.string.male_drapiezniki)
                "ptaki-wodne-maly-staw" -> RichText(R.string.ptaki_wodne_maly_staw)
                "wielblady-i-gwanko" -> RichText(R.string.wielblady_i_gwanko)
                "kob-sniady" -> RichText(R.string.kob_sniady)
                "eland" -> RichText(R.string.eland)
                "pingwiny" -> RichText(R.string.pingwiny)
                "kangury-i-strusie-emu" -> RichText(R.string.kangury_i_strusie_emu)
                "addaks" -> RichText(R.string.addaks)
                "takin-miszmi" -> RichText(R.string.takin_miszmi)
                "renifer" -> RichText(R.string.renifer)
                "sitatunga" -> RichText(R.string.sitatunga)
                "kob-liczi" -> RichText(R.string.kob_liczi)
                "uchatka-patagonska" -> RichText(R.string.uchatka_patagonska)
                "panda-mala" -> RichText(R.string.panda_mala)
                "arui" -> RichText(R.string.arui)
                "zyrafy" -> RichText(R.string.zyrafy)
                "manul" -> RichText(R.string.manul)
                "kulany" -> RichText(R.string.kulany)
                "jelenie" -> RichText(R.string.jelenie)
                "nilgau-markury" -> RichText(R.string.nilgau_markury)
                "daniele-barasinga" -> RichText(R.string.daniele_barasinga)
                "gastronomia" -> RichText(R.string.gastronomia)
                "lemury" -> RichText(R.string.lemury)
                "osly" -> RichText(R.string.osly)
                "milu" -> RichText(R.string.milu)
                "myszojelen" -> RichText(R.string.myszojelen)
                "mini-zoo" -> RichText(R.string.mini_zoo)
                "ul" -> RichText(R.string.ul)
                "kondory" -> RichText(R.string.kondory)
                "zurawie-mandzurskie" -> RichText(R.string.zurawie_mandzurskie)
                "nocny-pawilon" -> RichText(R.string.nocny_pawilon)
                "srednie-drapiezniki" -> RichText(R.string.srednie_drapiezniki)
                "sowy" -> RichText(R.string.sowy)
                "przewalskiego" -> RichText(R.string.przewalskiego)
                "wikunie" -> RichText(R.string.wikunie)
                "hipopotamy" -> RichText(R.string.hipopotamy)
                "malpy" -> RichText(R.string.malpy)
                "egzotarium" -> RichText(R.string.egzotarium)
                "slonie" -> RichText(R.string.slonie)
                else -> RichText(this)
            }
        }

    fun String.findFacilityDrawableRes(): Int? =
        when {
            startsWith("wc-") -> R.drawable.ic_wc_24
            startsWith("wyjscie") -> R.drawable.ic_door_24
            else -> null
        }
}