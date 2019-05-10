package com.jantzapps.jantz.xmltranslatorfree.utils

import android.os.Environment
import android.util.Log

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.ArrayList

import android.support.constraint.Constraints.TAG

/**
 * Created by jantz on 7/10/2017.
 */

object XMLFileMaker {
    private val FOLDER_NAME = "XMLTranslatorFILES"
    internal var resourceBegin = "<resources>\n"              //first in file
    internal var resourceEnd = "\n</resources>"               //last in file
    internal var stringBegin = "<string name=\""              //opening string tag, beginning of xml string definition line
    internal var stringDivider = "\">"                        //divider between xml string name and value
    internal var stringEnd = "</string>\n"                    //closing string tag, ending of xml string definition line
    internal var driveId: DriveId? = null

    fun xmlFileCreate(NamesArray: ArrayList<String>?, ValuesArray: ArrayList<String>): String {
        var xmlFile = ""
        val poweredBy = "Powered By "
        val yandexTranslate = "Yandex.Translate"
        val translatorShoutOut = "\n\n<!--    $poweredBy$yandexTranslate    http://translate.yandex.com/    -->"
        xmlFile += resourceBegin
        var newValue: String
        var newValue2: String
        for (i in NamesArray.indices) {
            xmlFile += stringBegin
            xmlFile += NamesArray[i]
            xmlFile += stringDivider
            newValue = ValuesArray[i].replace("\\", "")
            newValue2 = newValue.replace("\'", "\\\'")
            xmlFile += newValue2
            xmlFile += stringEnd
        }
        xmlFile += resourceEnd
        xmlFile += translatorShoutOut

        return xmlFile
    }

    fun checkFolderExists(mGoogleApiClient: GoogleApiClient) {

        val query = Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, FOLDER_NAME), Filters.eq(SearchableField.TRASHED, false)))
                .build()

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback { result ->
            var isFound = false

            for (m in result.metadataBuffer) {
                if (m.title == FOLDER_NAME) {
                    Log.e(TAG, "Main Folder exists")
                    isFound = true
                    driveId = m.driveId
                    //create_file_in_folder(driveId,toLang,xmlFile);
                    break
                }
            }
            if (!isFound) {
                Log.i(TAG, "Folder not found; creating it.")
                val changeSet = MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build()
                Drive.DriveApi.getRootFolder(mGoogleApiClient)!!
                        .createFolder(mGoogleApiClient, changeSet)
                        .setResultCallback { result ->
                            if (!result.status.isSuccess) {
                                Log.e(TAG, "Error while trying to create the folder")
                            } else {
                                Log.i(TAG, "Created Main Folder")
                                driveId = result.driveFolder.driveId
                                //create_file_in_folder(driveId,toLang,xmlFile);
                            }
                        }
            }
        }
    }

    fun createFileInFolder(toLang: String, xmlFile: String, mGoogleApiClient: GoogleApiClient) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(ResultCallback { driveContentsResult ->
            try {
                val folder2 = driveId!!.asDriveFolder()
                val changeSet2 = MetadataChangeSet.Builder()
                        .setTitle("values-$toLang").build()
                folder2.createFolder(mGoogleApiClient, changeSet2)
            } catch (e: Exception) {
                val changeSet2 = MetadataChangeSet.Builder()
                        .setTitle("values-$toLang").build()
                Drive.DriveApi.getRootFolder(mGoogleApiClient)!!
                        .createFolder(mGoogleApiClient, changeSet2)
            }

            val query = Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, "values-$toLang"), Filters.eq(SearchableField.TRASHED, false)))
                    .build()
            Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(ResultCallback { result ->
                var driveId2: DriveId? = null
                for (m in result.metadataBuffer) {
                    if (m.title == "values-$toLang") {
                        Log.e(TAG, "Folder exists")
                        //isFound = true;
                        driveId2 = m.driveId
                        //create_file_in_folder(driveId);
                        break
                    }
                }
                val outputStream = driveContentsResult.driveContents.outputStream
                val writer = OutputStreamWriter(outputStream)


                //------ THIS IS AN EXAMPLE FOR FILE --------
                val theFile = File(Environment.getExternalStorageDirectory().absolutePath + "/xmlfiles/strings.xml") //>>>>>> WHAT FILE ?

                try {
                    val fileInputStream = FileInputStream(theFile)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                } catch (e1: IOException) {
                    Log.i(TAG, "Unable to write file contents.")
                }

                try {
                    writer.write(xmlFile)
                    writer.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.message)
                }

                val changeSet = MetadataChangeSet.Builder().setTitle(theFile.name).setMimeType("text/plain").setStarred(false).build()
                val folder = driveId2!!.asDriveFolder()
                folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.driveContents)
                        .setResultCallback(ResultCallback { driveFileResult ->
                            if (!driveFileResult.status.isSuccess) {
                                Log.e(TAG, "Error while trying to create the file")
                                return@ResultCallback
                            }
                            Log.v(TAG, "Created a file: " + driveFileResult.driveFile.driveId)
                        })
            })
        })
    }
}
