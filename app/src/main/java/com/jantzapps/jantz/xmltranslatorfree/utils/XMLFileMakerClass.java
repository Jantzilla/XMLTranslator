package com.jantzapps.jantz.xmltranslatorfree.utils;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by jantz on 7/10/2017.
 */

public class XMLFileMakerClass {
    static String resourceBegin = "<resources>\n";              //first in file
    static String resourceEnd = "\n</resources>";               //last in file
    static String stringBegin = "<string name=\"";              //opening string tag, beginning of xml string definition line
    static String stringDivider = "\">";                        //divider between xml string name and value
    static String stringEnd = "</string>\n";                    //closing string tag, ending of xml string definition line

    public static String xmlFileCreate(ArrayList<String> NamesArray, ArrayList<String> ValuesArray) {
        String xmlFile = "";
        String poweredBy = "Powered By ";
        String yandexTranslate = "Yandex.Translate";
        String translatorShoutOut = "\n\n<!--    "+poweredBy+yandexTranslate+"    http://translate.yandex.com/    -->";
        xmlFile += resourceBegin;
        String newValue, newValue2;
        for (int i = 0; i < NamesArray.size(); i++) {
            xmlFile += stringBegin;
            xmlFile += NamesArray.get(i);
            xmlFile += stringDivider;
            newValue = ValuesArray.get(i).replace("\\", "");
            newValue2 = newValue.replace("\'", "\\\'");
            xmlFile += newValue2;
            xmlFile += stringEnd;
        }
        xmlFile += resourceEnd;
        xmlFile += translatorShoutOut;

        return xmlFile;
    }
}
