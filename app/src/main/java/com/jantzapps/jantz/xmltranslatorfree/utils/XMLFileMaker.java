package com.jantzapps.jantz.xmltranslatorfree.utils;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by jantz on 7/10/2017.
 */

public class XMLFileMaker {
    private static final String FOLDER_NAME = "XMLTranslatorFILES";
    static String resourceBegin = "<resources>\n";              //first in file
    static String resourceEnd = "\n</resources>";               //last in file
    static String stringBegin = "<string name=\"";              //opening string tag, beginning of xml string definition line
    static String stringDivider = "\">";                        //divider between xml string name and value
    static String stringEnd = "</string>\n";                    //closing string tag, ending of xml string definition line
    static DriveId driveId = null;

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

    public static void checkFolderExists(final GoogleApiClient mGoogleApiClient) {

        Query query =
                new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, FOLDER_NAME), Filters.eq(SearchableField.TRASHED, false)))
                        .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override public void onResult(DriveApi.MetadataBufferResult result) {
                boolean isFound = false;

                for (Metadata m : result.getMetadataBuffer()) {
                    if (m.getTitle().equals(FOLDER_NAME)) {
                        Log.e(TAG, "Main Folder exists");
                        isFound = true;
                        driveId = m.getDriveId();
                        //create_file_in_folder(driveId,toLang,xmlFile);
                        break;
                    }
                }
                if (!isFound) {
                    Log.i(TAG, "Folder not found; creating it.");
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build();
                    Drive.DriveApi.getRootFolder(mGoogleApiClient)
                            .createFolder(mGoogleApiClient, changeSet)
                            .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                @Override public void onResult(DriveFolder.DriveFolderResult result) {
                                    if (!result.getStatus().isSuccess()) {
                                        Log.e(TAG, "Error while trying to create the folder");
                                    } else {
                                        Log.i(TAG, "Created Main Folder");
                                        driveId = result.getDriveFolder().getDriveId();
                                        //create_file_in_folder(driveId,toLang,xmlFile);
                                    }
                                }
                            });
                }
            }
        });
    }
}
