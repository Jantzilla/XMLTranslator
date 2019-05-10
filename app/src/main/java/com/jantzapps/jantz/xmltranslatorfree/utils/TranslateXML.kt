package com.jantzapps.jantz.xmltranslatorfree.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import com.google.android.gms.common.api.GoogleApiClient
import com.jantzapps.jantz.xmltranslatorfree.R
import com.jantzapps.jantz.xmltranslatorfree.services.TranslationService

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

object TranslateXML {

    private val REQUEST_CODE = 132
    private val COMPLETED_UNITS = "completedUnits"
    private val TOTAL_UNITS = "totalUnits"
    private var service: TranslationService? = null
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var translating: Boolean = false
    private var totalStrings: Int = 0

    fun translateXML(fromLang: String, toLangs: Array<String>?, xmlStringsList: ArrayList<String>?,
                     mGoogleApiClient: GoogleApiClient?, xmlNamesList: ArrayList<String>?, translationService: TranslationService, broadcaster: LocalBroadcastManager?) {

        translating = true
        totalStrings = 0

        val toLangIds = ArrayList<String>()
        val fromLangId = getLangId(fromLang)
        val langDirectDivide = "-"
        service = translationService
        localBroadCastManager = broadcaster

        val translatedStrings = ArrayList<String>()

        val r = Runnable {
            var langDirection: String


            for (i in toLangs!!.indices) {
                toLangIds.add(getLangId(toLangs[i]))
            }

            label@ while (translating) {

                for (i in toLangIds.indices) {
                    langDirection = fromLangId + langDirectDivide + toLangIds[i]

                    for (i2 in xmlStringsList!!.indices) {

                        if (!translating)
                            break@label

                        translatedStrings.add(this.translate(xmlStringsList[i2], langDirection)!!)
                        totalStrings++
                        showProgressNotification(translationService.getString(R.string.translating), totalStrings, xmlStringsList.size * toLangIds.size)

                        if (totalStrings == xmlStringsList.size * toLangIds.size) {
                            showProgressNotification(translationService.getString(R.string.translation_complete), totalStrings, xmlStringsList.size * toLangIds.size)
                            finishTranslation()
                        }

                    }


                    val xmlFile = XMLFileMaker.xmlFileCreate(xmlNamesList, translatedStrings)
                    if (mGoogleApiClient != null) {
                        XMLFileMaker.createFileInFolder(toLangIds[i], xmlFile, mGoogleApiClient)
                    }
                    translatedStrings.clear()

                }

            }
        }

        val t = Thread(r)
        t.start()                       // starts thread in background..
    }

    private fun finishTranslation() {
        val intent = Intent(service, TranslationService::class.java)
        service!!.stopService(intent)
    }

    private fun getLangId(lang: String): String {
        var langId = ""
        when (lang) {
            "Afrikaans af" -> {
                langId = "af"
                return langId
            }
            "Albanian sq" -> {
                langId = "sq"
                return langId
            }
            "Amharic am" -> {
                langId = "am"
                return langId
            }
            "Arabic ar" -> {
                langId = "ar"
                return langId
            }
            "Armenian hy" -> {
                langId = "hy"
                return langId
            }
            "Azerbaijan az" -> {
                langId = "az"
                return langId
            }
            "Basque eu" -> {
                langId = "eu"
                return langId
            }
            "Belarusian be" -> {
                langId = "be"
                return langId
            }
            "Bengali bn" -> {
                langId = "bn"
                return langId
            }
            "Bosnian bs" -> {
                langId = "bs"
                return langId
            }
            "Bulgarian bg" -> {
                langId = "bg"
                return langId
            }
            "Catalan ca" -> {
                langId = "ca"
                return langId
            }
            "Cebuano ceb" -> {
                langId = "ceb"
                return langId
            }
            "Chinese zh" -> {
                langId = "zh"
                return langId
            }
            "Croatian hr" -> {
                langId = "hr"
                return langId
            }
            "Czech cs" -> {
                langId = "cs"
                return langId
            }
            "Danish da" -> {
                langId = "da"
                return langId
            }
            "Dutch nl" -> {
                langId = "nl"
                return langId
            }
            "English en" -> {
                langId = "en"
                return langId
            }
            "Esperanto eo" -> {
                langId = "eo"
                return langId
            }
            "Estonian et" -> {
                langId = "et"
                return langId
            }
            "Finnish fi" -> {
                langId = "fi"
                return langId
            }
            "French fr" -> {
                langId = "fr"
                return langId
            }
            "Galician gl" -> {
                langId = "gl"
                return langId
            }
            "Georgian ka" -> {
                langId = "ka"
                return langId
            }
            "German de" -> {
                langId = "de"
                return langId
            }
            "Greek el" -> {
                langId = "el"
                return langId
            }
            "Gujarati gu" -> {
                langId = "gu"
                return langId
            }
            "Haitian Creole ht" -> {
                langId = "ht"
                return langId
            }
            "Hebrew he" -> {
                langId = "he"
                return langId
            }
            "Hill Mari mrj" -> {
                langId = "mrj"
                return langId
            }
            "Hindi hi" -> {
                langId = "hi"
                return langId
            }
            "Hungarian hu" -> {
                langId = "hu"
                return langId
            }
            "Icelandic is" -> {
                langId = "is"
                return langId
            }
            "Indonesian id" -> {
                langId = "id"
                return langId
            }
            "Irish ga" -> {
                langId = "ga"
                return langId
            }
            "Italian it" -> {
                langId = "it"
                return langId
            }
            "Japanese ja" -> {
                langId = "ja"
                return langId
            }
            "Javanese jv" -> {
                langId = "jv"
                return langId
            }
            "Kannada kn" -> {
                langId = "kn"
                return langId
            }
            "Kazakh kk" -> {
                langId = "kk"
                return langId
            }
            "Khmer km" -> {
                langId = "km"
                return langId
            }
            "Korean ko" -> {
                langId = "ko"
                return langId
            }
            "Kyrgyz ky" -> {
                langId = "ky"
                return langId
            }
            "Laotian lo" -> {
                langId = "lo"
                return langId
            }
            "Latin la" -> {
                langId = "la"
                return langId
            }
            "Latvian lv" -> {
                langId = "lv"
                return langId
            }
            "Lithuanian lt" -> {
                langId = "lt"
                return langId
            }
            "Luxembourgish lb" -> {
                langId = "lb"
                return langId
            }
            "Macedonian mk" -> {
                langId = "mk"
                return langId
            }
            "Malagasy mg" -> {
                langId = "mg"
                return langId
            }
            "Malay ms" -> {
                langId = "ms"
                return langId
            }
            "Malayalam ml" -> {
                langId = "ml"
                return langId
            }
            "Maltese mt" -> {
                langId = "mt"
                return langId
            }
            "Maori mi" -> {
                langId = "mi"
                return langId
            }
            "Marathi mr" -> {
                langId = "mr"
                return langId
            }
            "Mari mhr" -> {
                langId = "mhr"
                return langId
            }
            "Mongolian mn" -> {
                langId = "mn"
                return langId
            }
            "Myanmar (Burmese) my" -> {
                langId = "my"
                return langId
            }
            "Nepali ne" -> {
                langId = "ne"
                return langId
            }
            "Norwegian no" -> {
                langId = "no"
                return langId
            }
            "Papiamento pap" -> {
                langId = "pap"
                return langId
            }
            "Persian fa" -> {
                langId = "fa"
                return langId
            }
            "Polish pl" -> {
                langId = "pl"
                return langId
            }
            "Portuguese pt" -> {
                langId = "pt"
                return langId
            }
            "Punjabi pa" -> {
                langId = "pa"
                return langId
            }
            "Romanian ro" -> {
                langId = "ro"
                return langId
            }
            "Russian ru" -> {
                langId = "ru"
                return langId
            }
            "Scottish gd" -> {
                langId = "gd"
                return langId
            }
            "Serbian sr" -> {
                langId = "sr"
                return langId
            }
            "Sinhala si" -> {
                langId = "si"
                return langId
            }
            "Slovak sk" -> {
                langId = "sk"
                return langId
            }
            "Slovenian sl" -> {
                langId = "sl"
                return langId
            }
            "Spanish es" -> {
                langId = "es"
                return langId
            }
            "Sundanese su" -> {
                langId = "su"
                return langId
            }
            "Swahili sw" -> {
                langId = "sw"
                return langId
            }
            "Swedish sv" -> {
                langId = "sv"
                return langId
            }
            "Tagalog tl" -> {
                langId = "tl"
                return langId
            }
            "Tajik tg" -> {
                langId = "tg"
                return langId
            }
            "Tamil ta" -> {
                langId = "ta"
                return langId
            }
            "Tatar tt" -> {
                langId = "tt"
                return langId
            }
            "Telugu te" -> {
                langId = "te"
                return langId
            }
            "Thai th" -> {
                langId = "th"
                return langId
            }
            "Turkish tr" -> {
                langId = "tr"
                return langId
            }
            "Udmurt udm" -> {
                langId = "udm"
                return langId
            }
            "Ukrainian uk" -> {
                langId = "uk"
                return langId
            }
            "Urdu ur" -> {
                langId = "ur"
                return langId
            }
            "Uzbek uz" -> {
                langId = "uz"
                return langId
            }
            "Vietnamese vi" -> {
                langId = "vi"
                return langId
            }
            "Welsh cy" -> {
                langId = "cy"
                return langId
            }
            "Xhosa xh" -> {
                langId = "xh"
                return langId
            }
            "Yiddish yi" -> {
                langId = "yi"
                return langId
            }
        }
        return langId
    }

    private fun translate(textToBeTranslated: String, languagePair: String): String? {

        var jsonString: String

        try {
            //Set up the translation call URL
            val yandexKey = "trnsl.1.1.20170726T103657Z.d7d13d14935a0109.481b8afc3d0f0e40310853ad9c59323784aaa3db"
            val yandexUrl = ("https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + yandexKey
                    + "&text=" + textToBeTranslated + "&lang=" + languagePair)
            val yandexTranslateURL = URL(yandexUrl)

            //Set Http Conncection, Input Stream, and Buffered Reader
            val httpJsonConnection = yandexTranslateURL.openConnection() as HttpURLConnection
            val inputStream = httpJsonConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            //Set string builder and insert retrieved JSON result into it
            val jsonStringBuilder = StringBuilder()
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n")
            }

            //Close and disconnect
            bufferedReader.close()
            inputStream.close()
            httpJsonConnection.disconnect()

            //Making result human readable
            var resultString = jsonStringBuilder.toString().trim { it <= ' ' }
            //Getting the characters between [ and ]
            resultString = resultString.substring(resultString.indexOf('[') + 1)
            resultString = resultString.substring(0, resultString.indexOf("]"))
            //Getting the characters between " and "
            resultString = resultString.substring(resultString.indexOf("\"") + 1)
            resultString = resultString.substring(0, resultString.indexOf("\""))

            Log.d("Translation Result:", resultString)
            return resultString

        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun sendProgress(completedUnits: Int, totalUnits: Int) {
        val intent = Intent("translation update")
        intent.putExtra(COMPLETED_UNITS, completedUnits)
        intent.putExtra(TOTAL_UNITS, totalUnits)
        localBroadCastManager!!.sendBroadcast(intent)
    }

    private fun showProgressNotification(caption: String, completedUnits: Int, totalUnits: Int) {

        val mNotificationManager = service!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mNotificationManager != null) {
            //Update the notification bar progress

            if (!translating) {
                mNotificationManager.cancelAll()
                mNotificationManager.notify(REQUEST_CODE, service!!.createNotification(100, 100, service!!.getString(R.string.translation_stopped)))
                sendProgress(100, 100)
            } else {
                mNotificationManager.notify(REQUEST_CODE, service!!.createNotification(totalUnits, completedUnits, caption))
                sendProgress(completedUnits, totalUnits)
            }
        }
    }

    fun stopTranslation() {
        translating = false
    }
}
