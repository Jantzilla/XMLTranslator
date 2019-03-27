package com.jantzapps.jantz.xmltranslatorfree.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.jantzapps.jantz.xmltranslatorfree.R;
import com.jantzapps.jantz.xmltranslatorfree.services.TranslationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class TranslateXML {

    private static final int REQUEST_CODE = 132;
    private static final String COMPLETED_UNITS = "completedUnits";
    private static final String TOTAL_UNITS = "totalUnits";
    private static TranslationService service;
    private static LocalBroadcastManager localBroadCastManager;
    private static boolean translating;
    private static int totalStrings;

    public static void translateXML(String fromLang, final String[] toLangs, final ArrayList<String> xmlStringsList,
                                    final GoogleApiClient mGoogleApiClient, final ArrayList<String> xmlNamesList, final TranslationService translationService, LocalBroadcastManager broadcaster) {

        translating = true;
        totalStrings = 0;

        final ArrayList<String> toLangIds = new ArrayList<String>();
        final String fromLangId = getLangId(fromLang);
        final String langDirectDivide = "-";
        service = translationService;
        localBroadCastManager = broadcaster;

        final ArrayList<String> translatedStrings = new ArrayList<String>();

        Runnable r = (new Runnable() {
            @Override
            public void run() {

                String langDirection;


                for (int i = 0; i < toLangs.length; i++) {
                    toLangIds.add(getLangId(toLangs[i]));
                }

                label:
                while(translating) {

                    for (int i = 0; i < toLangIds.size(); i++) {
                        langDirection = fromLangId + langDirectDivide + toLangIds.get(i);

                        for (int i2 = 0; i2 < xmlStringsList.size(); i2++) {

                            if(!translating)
                                break label;

                            translatedStrings.add(translate(xmlStringsList.get(i2), langDirection));
                            totalStrings++;
                            showProgressNotification(translationService.getString(R.string.translating), totalStrings, (xmlStringsList.size() * toLangIds.size()));

                            if (totalStrings == (xmlStringsList.size() * toLangIds.size())) {
                                showProgressNotification(translationService.getString(R.string.translation_complete), totalStrings, (xmlStringsList.size() * toLangIds.size()));
                                finishTranslation();
                            }

                        }


                        String xmlFile = XMLFileMaker.xmlFileCreate(xmlNamesList, translatedStrings);
                        if (mGoogleApiClient != null) {
                            XMLFileMaker.createFileInFolder(toLangIds.get(i), xmlFile, mGoogleApiClient);
                        }
                        translatedStrings.clear();

                    }

                }
            }
        });

        Thread t = new Thread(r);
        t.start();                       // starts thread in background..
    }

    private static void finishTranslation() {
        Intent intent = new Intent(service, TranslationService.class);
        service.stopService(intent);
    }

    private static String getLangId(String lang) {
        String langId = "";
        switch(lang) {
            case "Afrikaans af": langId = "af"; return langId; case "Albanian sq": langId = "sq"; return langId; case "Amharic am": langId = "am"; return langId; case "Arabic ar": langId = "ar"; return langId; case "Armenian hy": langId = "hy"; return langId; case "Azerbaijan az": langId = "az"; return langId; case "Basque eu": langId = "eu"; return langId; case "Belarusian be": langId = "be"; return langId; case "Bengali bn": langId = "bn"; return langId; case "Bosnian bs": langId = "bs"; return langId; case "Bulgarian bg": langId = "bg"; return langId;
            case "Catalan ca": langId = "ca"; return langId; case "Cebuano ceb": langId = "ceb"; return langId; case "Chinese zh": langId = "zh"; return langId; case "Croatian hr": langId = "hr"; return langId;case "Czech cs": langId = "cs"; return langId; case "Danish da": langId = "da"; return langId; case "Dutch nl": langId = "nl"; return langId; case "English en": langId = "en"; return langId; case "Esperanto eo": langId = "eo"; return langId; case "Estonian et": langId = "et"; return langId; case "Finnish fi": langId = "fi"; return langId;
            case "French fr": langId = "fr"; return langId; case "Galician gl": langId = "gl"; return langId; case "Georgian ka": langId = "ka"; return langId; case "German de": langId = "de"; return langId; case "Greek el": langId = "el"; return langId; case "Gujarati gu": langId = "gu"; return langId; case "Haitian Creole ht": langId = "ht"; return langId; case "Hebrew he": langId = "he"; return langId;case "Hill Mari mrj": langId = "mrj";return langId;case "Hindi hi": langId = "hi";return langId;case "Hungarian hu": langId = "hu";return langId;
            case "Icelandic is": langId = "is";return langId;case "Indonesian id": langId = "id";return langId;case "Irish ga": langId = "ga";return langId;case "Italian it": langId = "it";return langId;case "Japanese ja": langId = "ja";return langId;case "Javanese jv": langId = "jv";return langId;case "Kannada kn": langId = "kn";return langId;case "Kazakh kk": langId = "kk";return langId;case "Khmer km": langId = "km";return langId;case "Korean ko": langId = "ko";return langId;case "Kyrgyz ky": langId = "ky";return langId;case "Laotian lo": langId = "lo";return langId;
            case "Latin la": langId = "la";return langId;case "Latvian lv": langId = "lv";return langId;case "Lithuanian lt": langId = "lt";return langId;case "Luxembourgish lb": langId = "lb";return langId;case "Macedonian mk": langId = "mk";return langId;case "Malagasy mg": langId = "mg";return langId;case "Malay ms": langId = "ms";return langId;case "Malayalam ml": langId = "ml";return langId;case "Maltese mt": langId = "mt";return langId;case "Maori mi": langId = "mi";return langId;case "Marathi mr": langId = "mr";return langId;
            case "Mari mhr": langId = "mhr";return langId;case "Mongolian mn": langId = "mn";return langId;case "Myanmar (Burmese) my": langId = "my";return langId;case "Nepali ne": langId = "ne";return langId;case "Norwegian no": langId = "no";return langId;case "Papiamento pap": langId = "pap";return langId;case "Persian fa": langId = "fa";return langId;case "Polish pl": langId = "pl";return langId;case "Portuguese pt": langId = "pt";return langId;case "Punjabi pa": langId = "pa";return langId;case "Romanian ro": langId = "ro";return langId;case "Russian ru": langId = "ru";return langId;
            case "Scottish gd": langId = "gd";return langId;case "Serbian sr": langId = "sr";return langId;case "Sinhala si": langId = "si";return langId;case "Slovak sk": langId = "sk";return langId;case "Slovenian sl": langId = "sl";return langId;case "Spanish es": langId = "es"; return langId; case "Sundanese su": langId = "su";return langId;case "Swahili sw": langId = "sw";return langId;case "Swedish sv": langId = "sv";return langId;case "Tagalog tl": langId = "tl";return langId;case "Tajik tg": langId = "tg";return langId;
            case "Tamil ta": langId = "ta";return langId;case "Tatar tt": langId = "tt";return langId;case "Telugu te": langId = "te";return langId;case "Thai th": langId = "th";return langId;case "Turkish tr": langId = "tr";return langId;case "Udmurt udm": langId = "udm";return langId;case "Ukrainian uk": langId = "uk";return langId;case "Urdu ur": langId = "ur";return langId;case "Uzbek uz": langId = "uz";return langId;case "Vietnamese vi": langId = "vi";return langId;case "Welsh cy": langId = "cy";return langId;case "Xhosa xh": langId = "xh";return langId;case "Yiddish yi": langId = "yi";return langId;
        }
        return langId;
    }

    private static String translate(String textToBeTranslated, String languagePair) {

        String jsonString;

        try {
            //Set up the translation call URL
            String yandexKey = "trnsl.1.1.20170726T103657Z.d7d13d14935a0109.481b8afc3d0f0e40310853ad9c59323784aaa3db";
            String yandexUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + yandexKey
                    + "&text=" + textToBeTranslated + "&lang=" + languagePair;
            URL yandexTranslateURL = new URL(yandexUrl);

            //Set Http Conncection, Input Stream, and Buffered Reader
            HttpURLConnection httpJsonConnection = (HttpURLConnection) yandexTranslateURL.openConnection();
            InputStream inputStream = httpJsonConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //Set string builder and insert retrieved JSON result into it
            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n");
            }

            //Close and disconnect
            bufferedReader.close();
            inputStream.close();
            httpJsonConnection.disconnect();

            //Making result human readable
            String resultString = jsonStringBuilder.toString().trim();
            //Getting the characters between [ and ]
            resultString = resultString.substring(resultString.indexOf('[')+1);
            resultString = resultString.substring(0,resultString.indexOf("]"));
            //Getting the characters between " and "
            resultString = resultString.substring(resultString.indexOf("\"")+1);
            resultString = resultString.substring(0,resultString.indexOf("\""));

            Log.d("Translation Result:", resultString);
            return resultString;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendProgress(int completedUnits, int totalUnits) {
        Intent intent = new Intent("translation update");
        intent.putExtra(COMPLETED_UNITS, completedUnits);
        intent.putExtra(TOTAL_UNITS, totalUnits);
        localBroadCastManager.sendBroadcast(intent);
    }

    private static void showProgressNotification(String caption, int completedUnits, int totalUnits) {

        NotificationManager mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            //Update the notification bar progress

            if(!translating) {
                mNotificationManager.cancelAll();
                mNotificationManager.notify(REQUEST_CODE,  service.createNotification(100,100, service.getString(R.string.translation_stopped)));
                sendProgress(100, 100);
            } else {
                mNotificationManager.notify(REQUEST_CODE, service.createNotification(totalUnits, completedUnits, caption));
                sendProgress(completedUnits, totalUnits);
            }
        }
    }

    public static void stopTranslation() {
        translating = false;
    }
}
