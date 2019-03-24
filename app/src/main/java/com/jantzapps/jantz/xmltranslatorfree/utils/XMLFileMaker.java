package com.jantzapps.jantz.xmltranslatorfree.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

    public static void createFileInFolder(final String toLang, final String xmlFile, final GoogleApiClient mGoogleApiClient) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override public void onResult(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {

                try {
                    DriveFolder folder2 = driveId.asDriveFolder();
                    MetadataChangeSet changeSet2 = new MetadataChangeSet.Builder()
                            .setTitle("values-"+toLang).build();
                    folder2.createFolder(mGoogleApiClient, changeSet2);
                } catch (Exception e) {
                    MetadataChangeSet changeSet2 = new MetadataChangeSet.Builder()
                            .setTitle("values-"+toLang).build();
                    Drive.DriveApi.getRootFolder(mGoogleApiClient)
                            .createFolder(mGoogleApiClient, changeSet2);
                }
                Query query =
                        new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, "values-"+toLang), Filters.eq(SearchableField.TRASHED, false)))
                                .build();
                Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override public void onResult(DriveApi.MetadataBufferResult result) {
                        DriveId driveId2 = null;
                            for (Metadata m : result.getMetadataBuffer()) {
                                if (m.getTitle().equals("values-"+toLang)) {
                                    Log.e(TAG, "Folder exists");
                                    //isFound = true;
                                    driveId2 = m.getDriveId();
                                    //create_file_in_folder(driveId);
                                    break;
                                }
                            }
                            final OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);


                            //------ THIS IS AN EXAMPLE FOR FILE --------
                            final File theFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xmlfiles/strings.xml"); //>>>>>> WHAT FILE ?

                            try {
                                FileInputStream fileInputStream = new FileInputStream(theFile);
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            } catch (IOException e1) {
                                Log.i(TAG, "Unable to write file contents.");
                            }
                            try {
                                writer.write(xmlFile);
                                writer.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(theFile.getName()).setMimeType("text/plain").setStarred(false).build();
                            DriveFolder folder = driveId2.asDriveFolder();
                            folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                            if (!driveFileResult.getStatus().isSuccess()) {
                                                Log.e(TAG, "Error while trying to create the file");
                                                return;
                                            }
                                            Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                                        }
                                    });
                    }
                });



            }
        });
    }
}
