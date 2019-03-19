package com.jantzapps.jantz.xmltranslatorfree;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.R.string.ok;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public DriveFile file;

    Button submit;
    EditText xmlStrings;
    Context context;

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "<< DRIVE >>";
    protected static final int REQUEST_CODE_RESOLUTION = 1338;
    private String FOLDER_NAME = "XMLTranslatorFILES";
    DriveId driveId = null;
    DbHelper dbHelper;
    PendingIntent mAlarmIntent;
    AlarmManager alarm_manager;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE2 = 2;
    private static final String ADMOB_APP_ID = "ca-app-pub-5985384760144093~6592515362";
    InterstitialAd mInterstitialAd;
    private Button openFileButton;
    private ConstraintLayout buttonBlock, parentLayout;
    private ConstraintSet constraintSet;
    private EditText rawEditText;
    private TextView orTextView;

    private void upload_to_drive(String toLang, String xmlFile) {

        //async check if folder exists... if not, create it. continue after with create_file_in_folder(driveId);
        create_file_in_folder(toLang,xmlFile);
    }

    private void check_folder_exists() {

        Query query =
                new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, FOLDER_NAME), Filters.eq(SearchableField.TRASHED, false)))
                        .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override public void onResult(DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder in the root.");
                } else {
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
                    if (isFound == false) {
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
            }
        });
    }

    private void create_file_in_folder(final String toLang, final String xmlFile) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override public void onResult(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error while trying to create new file contents");
                    return;
                }


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
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Error while trying to create the folder");
                        } else {
                            Log.i(TAG, "Created a folder");
                            //boolean isFound = false;
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
                    }
                });



            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)                                                     // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "+++++++++++++++++++ onConnected +++++++++++++++++++");
    }

    @Override public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended [" + String.valueOf(i) + "]");
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    @Override protected void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // 1
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); // 2
        return networkInfo != null && networkInfo.isConnected(); // 3
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPermission2() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, R.string.write_permission_explain, Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void requestPermission2() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, R.string.read_permission_explain, Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can write local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot write local drive .");
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(getString(R.string.permission_necessary));
                    alertBuilder.setMessage(R.string.write_external_permission_necessary);
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermission();
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
                break;
            case PERMISSION_REQUEST_CODE2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can read local drive .");
                    String permChar;
                    final File Xml_limit_path = new File(Environment.getExternalStorageDirectory() + "/App_data/");
                    final File Xml_limit = new File(Xml_limit_path, "Char.txt");
                        try {
                            FileInputStream fis = new FileInputStream(Xml_limit);
                            DataInputStream in = new DataInputStream(fis);
                            BufferedReader br =
                                    new BufferedReader(new InputStreamReader(in));
                            String strLine;
                            while ((strLine = br.readLine()) != null) {
                                permChar = strLine;
                                Log.e("DailyCharCount","File Is Read");
                                dbHelper.addCharCount(Integer.valueOf(permChar));
                                Log.e("DailyCharCount", String.valueOf(dbHelper.getCharCount()));
                            }
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                } else {
                    Log.e("value", "Permission Denied, You cannot read local drive .");
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(R.string.permission_necessary);
                    alertBuilder.setMessage(R.string.read_external_permission_necessary);
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermission2();
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, ADMOB_APP_ID);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        submit = (Button) findViewById(R.id.btnSubmit);
        xmlStrings = (EditText) findViewById(R.id.etEmailMessage);
        final MultiSelectionSpinner spinner = (MultiSelectionSpinner)findViewById(R.id.input1);
        final MultiSelectionSpinner spinner2 = (MultiSelectionSpinner)findViewById(R.id.input2);
        final Handler handler = new Handler();
        dbHelper = new DbHelper(this);
        int day = 86400000;
        final int dailyLimit = 3000;
        final File Xml_limit_path = new File(Environment.getExternalStorageDirectory() + "/App_data/");
        final File Xml_limit = new File(Xml_limit_path, "Char.txt");

        buttonBlock = findViewById(R.id.ll_button_block);
        parentLayout = findViewById(R.id.root);
        openFileButton = findViewById(R.id.btn_open_file);
        rawEditText = findViewById(R.id.etEmailMessage);
        orTextView = findViewById(R.id.tv_or_label);

        constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);

        List<String> list = new ArrayList<String>();
        list.add("Afrikaans af"); list.add("Albanian sq"); list.add("Amharic am"); list.add("Arabic ar"); list.add("Armenian hy"); list.add("Azerbaijan az"); list.add("Basque eu"); list.add("Belarusian be"); list.add("Bengali bn"); list.add("Bosnian bs"); list.add("Bulgarian bg");
        list.add("Catalan ca"); list.add("Cebuano ceb"); list.add("Chinese zh"); list.add("Croatian hr"); list.add("Czech cs"); list.add("Danish da"); list.add("Dutch nl"); list.add("English en"); list.add("Esperanto eo"); list.add("Estonian et"); list.add("Finnish fi");
        list.add("French fr"); list.add("Galician gl"); list.add("Georgian ka"); list.add("German de"); list.add("Greek el"); list.add("Gujarati gu"); list.add("Haitian Creole ht"); list.add("Hebrew he");
        list.add("Hill Mari mrj"); list.add("Hindi hi"); list.add("Hungarian hu"); list.add("Icelandic is"); list.add("Indonesian id"); list.add("Irish ga"); list.add("Italian it"); list.add("Japanese ja"); list.add("Javanese jv"); list.add("Kannada kn"); list.add("Kazakh kk"); list.add("Khmer km"); list.add("Korean ko");
        list.add("Kyrgyz ky"); list.add("Laotian lo"); list.add("Latin la"); list.add("Latvian lv"); list.add("Lithuanian lt"); list.add("Luxembourgish lb"); list.add("Macedonian mk"); list.add("Malagasy mg"); list.add("Malay ms"); list.add("Malayalam ml"); list.add("Maltese mt"); list.add("Maori mi"); list.add("Marathi mr"); list.add("Mari mhr"); list.add("Mongolian mn"); list.add("Myanmar (Burmese) my");
        list.add("Nepali ne"); list.add("Norwegian no"); list.add("Papiamento pap"); list.add("Persian fa"); list.add("Polish pl"); list.add("Portuguese pt"); list.add("Punjabi pa"); list.add("Romanian ro"); list.add("Russian ru"); list.add("Scottish gd"); list.add("Serbian sr"); list.add("Sinhala si");
        list.add("Slovak sk"); list.add("Slovenian sl"); list.add("Spanish es"); list.add("Sundanese su"); list.add("Swahili sw"); list.add("Swedish sv"); list.add("Tagalog tl"); list.add("Tajik tg"); list.add("Tamil ta"); list.add("Tatar tt"); list.add("Telugu te"); list.add("Thai th"); list.add("Turkish tr"); list.add("Udmurt udm"); list.add("Ukrainian uk"); list.add("Urdu ur"); list.add("Uzbek uz"); list.add("Vietnamese vi"); list.add("Welsh cy"); list.add("Xhosa xh");
        list.add("Yiddish yi");
        spinner.setItems(list);


        List<String> list2 = new ArrayList<String>();
        list2.add("Afrikaans af"); list2.add("Albanian sq"); list2.add("Amharic am"); list2.add("Arabic ar"); list2.add("Armenian hy"); list2.add("Azerbaijan az"); list2.add("Basque eu"); list2.add("Belarusian be"); list2.add("Bengali bn"); list2.add("Bosnian bs"); list2.add("Bulgarian bg");
        list2.add("Catalan ca"); list2.add("Cebuano ceb"); list2.add("Chinese zh"); list2.add("Croatian hr"); list2.add("Czech cs"); list2.add("Danish da"); list2.add("Dutch nl"); list2.add("English en"); list2.add("Esperanto eo"); list2.add("Estonian et"); list2.add("Finnish fi");
        list2.add("French fr"); list2.add("Galician gl"); list2.add("Georgian ka"); list2.add("German de"); list2.add("Greek el"); list2.add("Gujarati gu"); list2.add("Haitian Creole ht"); list2.add("Hebrew he");
        list2.add("Hill Mari mrj"); list2.add("Hindi hi"); list2.add("Hungarian hu"); list2.add("Icelandic is"); list2.add("Indonesian id"); list2.add("Irish ga"); list2.add("Italian it"); list2.add("Japanese ja"); list2.add("Javanese jv"); list2.add("Kannada kn"); list2.add("Kazakh kk"); list2.add("Khmer km"); list2.add("Korean ko");
        list2.add("Kyrgyz ky"); list2.add("Laotian lo"); list2.add("Latin la"); list2.add("Latvian lv"); list2.add("Lithuanian lt"); list2.add("Luxembourgish lb"); list2.add("Macedonian mk"); list2.add("Malagasy mg"); list2.add("Malay ms"); list2.add("Malayalam ml"); list2.add("Maltese mt"); list2.add("Maori mi"); list2.add("Marathi mr"); list2.add("Mari mhr"); list2.add("Mongolian mn"); list2.add("Myanmar (Burmese) my");
        list2.add("Nepali ne"); list2.add("Norwegian no"); list2.add("Papiamento pap"); list2.add("Persian fa"); list2.add("Polish pl"); list2.add("Portuguese pt"); list2.add("Punjabi pa"); list2.add("Romanian ro"); list2.add("Russian ru"); list2.add("Scottish gd"); list2.add("Serbian sr"); list2.add("Sinhala si");
        list2.add("Slovak sk"); list2.add("Slovenian sl"); list2.add("Spanish es"); list2.add("Sundanese su"); list2.add("Swahili sw"); list2.add("Swedish sv"); list2.add("Tagalog tl"); list2.add("Tajik tg"); list2.add("Tamil ta"); list2.add("Tatar tt"); list2.add("Telugu te"); list2.add("Thai th"); list2.add("Turkish tr"); list2.add("Udmurt udm"); list2.add("Ukrainian uk"); list2.add("Urdu ur"); list2.add("Uzbek uz"); list2.add("Vietnamese vi"); list2.add("Welsh cy"); list2.add("Xhosa xh");
        list2.add("Yiddish yi");
        spinner2.setItems(list2);

        if(!isExternalStorageAvailable() && !isExternalStorageWritable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage(getString(R.string.external_storage_must) +"\n"+
                    getString(R.string.mount_external_please))
                    .setCancelable(false)
                    .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
                            MainActivity.this.startActivity(restartIntent);
                            finish();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }

        try {
            if(dbHelper.getTime() == 0L) {
            }
            else {

            }

        } catch (Exception e) {
            dbHelper.initialize();
        }

        if(dbHelper.getTime() == 0L) {
            alarm_manager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
            Intent intent2 = new Intent(getApplication(), AlarmReceiver.class);
            intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mAlarmIntent = PendingIntent.getBroadcast(getApplication(), 0, intent2, 0);
            Long time = System.currentTimeMillis();
            dbHelper.newTime();

            alarm_manager.set(AlarmManager.RTC_WAKEUP, time + day, mAlarmIntent);
            String permChar;

            if (Build.VERSION.SDK_INT >= 23)
            {
                if (checkPermission2())
                {
                    try {
                        FileInputStream fis = new FileInputStream(Xml_limit);
                        DataInputStream in = new DataInputStream(fis);
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(in));
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            permChar = strLine;
                            Log.e("DailyCharCount","File Is Read");
                            dbHelper.addCharCount(Integer.valueOf(permChar));
                            Log.e("DailyCharCount", String.valueOf(dbHelper.getCharCount()));
                        }
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    requestPermission2(); // Code for permission
                }
            }
            else
            {
                // Code for Below 23 API Oriented Device
                // Do next code

                try {
                    FileInputStream fis = new FileInputStream(Xml_limit);
                    DataInputStream in = new DataInputStream(fis);
                    BufferedReader br =
                            new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        permChar = strLine;
                        Log.e("DailyCharCount","File Is Read");
                        dbHelper.addCharCount(Integer.valueOf(permChar));
                        Log.e("DailyCharCount", String.valueOf(dbHelper.getCharCount()));
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


        TextView yandexLink = findViewById(R.id.yandex_link);
        yandexLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://translate.yandex.com/")));
            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fromLang = spinner.getSelectedItemsAsString();
                final String toLang = spinner2.getSelectedItemsAsString();

                if (xmlStrings.getText().toString().equals("")) {

                    xmlStrings.setError(getString(R.string.paste_xml_to_translate));
                }

                if (fromLang.isEmpty()) {

                    Toast.makeText(MainActivity.this, R.string.choose_lang_translate_from, Toast.LENGTH_SHORT).show();
                }
                if (fromLang.contains(",")) {

                    Toast.makeText(MainActivity.this, R.string.choose_one_translate_from, Toast.LENGTH_SHORT).show();
                }
                if (toLang.isEmpty()) {

                    Toast.makeText(MainActivity.this, R.string.choose_lang_translate_to, Toast.LENGTH_SHORT).show();
                } else {
                    if (!toLang.isEmpty() && !fromLang.contains(",") && !fromLang.isEmpty() &&
                            !xmlStrings.getText().toString().equals("")) {

                        if (isNetworkConnected()) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mInterstitialAd.isLoaded()) {
                                        mInterstitialAd.show();
                                    } else {
                                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                                    }
                                }
                            }, 4000);
                            if (Build.VERSION.SDK_INT >= 23)
                            {
                                if (checkPermission())
                                {
                                    if (!Xml_limit_path.exists()) {
                                        Xml_limit_path.mkdir();
                                    }
                                    if(!Xml_limit.getParentFile().exists()) {
                                        try {

                                            Xml_limit.getParentFile().createNewFile();
                                            Log.e("Success","DailyTrac File Created");
                                        } catch (Exception e) {

                                        }
                                    }
                                    else {
                                        Log.e("DailyCharCount","File Exists");
                                    }

                                } else {
                                    requestPermission(); // Code for permission
                                    if (checkPermission())
                                    {
                                        if (!Xml_limit_path.exists()) {
                                            Xml_limit_path.mkdir();
                                        }
                                        if(!Xml_limit.getParentFile().exists()) {
                                            try {

                                                Xml_limit.getParentFile().createNewFile();
                                                Log.e("Success","DailyTrac File Created");
                                            } catch (Exception e) {

                                            }
                                        }
                                        else {
                                            Log.e("DailyCharCount","File Exists");
                                        }

                                    }
                                }
                            }
                            else
                            {
                                // Code for Below 23 API Oriented Device
                                // Do next code

                                if (!Xml_limit_path.exists()) {
                                    Xml_limit_path.mkdir();
                                }
                                if(!Xml_limit.getParentFile().exists()) {
                                    try {

                                        Xml_limit.getParentFile().createNewFile();


                                    } catch (Exception e) {

                                    }
                                }
                                else {
                                    Log.e("DailyCharCount","File Exists");
                                }
                            }


                            String delims = "[,]";                                                                       //Important!!!  //XML string parsing
                            final String[] toLangs = toLang.split(delims);
                            int toLangCount = 0;
                            for (int i = 0; i < toLangs.length; i++) {
                                toLangCount += 1;
                            }
                            final ArrayList<String> xmlStringsList = storeValues(xmlStrings.getText().toString());

                            int checkChar = 0;

                            for (int i2 = 0; i2 < xmlStringsList.size(); i2++) {

                                checkChar += xmlStringsList.get(i2).length();

                            }

                            if ((checkChar * toLangCount) + dbHelper.getCharCount() > dailyLimit)

                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.app_name);
                                builder.setIcon(R.mipmap.ic_launcher);
                                builder.setMessage(R.string.daily_limit_reached)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.try_it, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("market://details?id=com.jantzapps.jantz.xmlanguagetranslator"));
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .setNegativeButton(R.string.mayber_later, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else {

                                dbHelper.addCharCount((checkChar * toLangCount));

                                Xml_limit.delete();
                                try {
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(Xml_limit, false));
                                    bw.write(String.valueOf(dbHelper.getCharCount()));
                                    bw.close();
                                    Log.e("Success", "DailyTrac File Written To");

                                    // Details omitted.

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }


                                Toast.makeText(MainActivity.this, R.string.translating_updating_wait, Toast.LENGTH_LONG).show();
                                Drive.DriveApi.requestSync(mGoogleApiClient);                                                             //Drive Sync request

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        check_folder_exists();
                                    }
                                }, 1000);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Drive.DriveApi.requestSync(mGoogleApiClient);                                                             //Drive Sync request
                                    }
                                }, 1000);

                                //progressDialog.setTitle("Creating Files");
                                //progressDialog.setMessage("Processing...");
                                //progressDialog.show();
                                final ArrayList<String> toLangIds = new ArrayList<String>();
                                final String fromLangId = getLangId(fromLang);
                                final String langDirectDivide = "-";

                                final ArrayList<String> translatedStrings = new ArrayList<String>();


                                final ArrayList<String> xmlNamesList = storeNames(xmlStrings.getText().toString());

                                xmlStrings.setText("");
                                spinner.setSelection(0);
                                spinner2.setSelection(0);

                                Runnable r = (new Runnable() {
                                    @Override
                                    public void run() {

                                        String langDirection;


                                        for (int i = 0; i < toLangs.length; i++) {
                                            toLangIds.add(getLangId(toLangs[i]));
                                        }

                                        for (int i = 0; i < toLangIds.size(); i++) {
                                            langDirection = fromLangId + langDirectDivide + toLangIds.get(i);

                                            for (int i2 = 0; i2 < xmlStringsList.size(); i2++) {

                                                translatedStrings.add(Translate(xmlStringsList.get(i2), langDirection));

                                            }

                                            String xmlFile = XMLFileMakerClass.xmlFileCreate(xmlNamesList, translatedStrings);
                                            if (mGoogleApiClient != null) {
                                                upload_to_drive(toLangIds.get(i), xmlFile);
                                            } else {
                                                Log.e(TAG, "Could not connect to google drive manager");
                                            }
                                            translatedStrings.clear();

                                        }
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                NotificationCompat.Builder mBuilder =
                                                        new NotificationCompat.Builder(MainActivity.this)
                                                                .setAutoCancel(true)
                                                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                                                .setLights(Color.BLUE, 12500, 12500)
                                                                .setColor(Color.argb(1, 66, 208, 217))
                                                                .setSmallIcon(R.drawable.finished)
                                                                .setContentTitle("XML Translator Free")
                                                                .setContentText(getString(R.string.translations_complete));

// Gets an instance of the NotificationManager service
                                                NotificationManager mNotifyMgr =
                                                        (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                                                mNotifyMgr.notify(0, mBuilder.setPriority(PRIORITY_MAX).build());

                                            }
                                        });

                                    }
                                });

                                Thread t = new Thread(r);
                                t.start();                       // starts thread in background..
                            }

                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.no_connection))
                                    .setMessage(getString(R.string.no_connection_direction))
                                    .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                        }

                    }
                }


            }
        });
    }

    private void showTranslateButton() {
        constraintSet.clear(R.id.ll_button_block, ConstraintSet.TOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(parentLayout);

        constraintSet.connect(R.id.ll_button_block,ConstraintSet.BOTTOM,R.id.root,ConstraintSet.BOTTOM,0);
        constraintSet.applyTo(parentLayout);
    }

    private void showChosenFile() {
        showTranslateButton();
        rawEditText.setVisibility(View.GONE);
        orTextView.setVisibility(View.GONE);
    }

    private void showRawXml() {
        constraintSet.constrainHeight(R.id.etEmailMessage,ConstraintSet.MATCH_CONSTRAINT);
        showTranslateButton();
        openFileButton.setVisibility(View.GONE);
        orTextView.setVisibility(View.GONE);
    }

    public String getLangId(String lang) {
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

    public String Translate(String textToBeTranslated,String languagePair){
        TranslatorBackgroundTask translatorBackgroundTask= new TranslatorBackgroundTask(context);
        String translationResult = null; // Returns the translated text as a String
        try {
            translationResult = String.valueOf(translatorBackgroundTask.execute(textToBeTranslated,languagePair).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return translationResult;
    }

    public ArrayList storeNames(String string) {
        int i = 0;
        ArrayList<String> xmlNames = new ArrayList<String>();
        while (true) {
            try {

                int found = string.indexOf("name=\"", i);
                if (found == -1) break;
                int start = found + 6; // start of actual name
                int end = string.indexOf("\"", start);

                xmlNames.add(string.substring(start, end));

                i = end + 1;  // advance i to start the next iteration
            } catch (Exception e) {}

        }
        return xmlNames;
    }
    public ArrayList storeValues(String string) {
        int i = 0;
        ArrayList<String> xmlStrings = new ArrayList<String>();
        while (true) {
            try {

                int found = string.indexOf("\">", i);
                if (found == -1) break;
                int start = found + 2; // start of actual name
                int end = string.indexOf("</", start);

                xmlStrings.add(string.substring(start, end));

                i = end + 1;  // advance i to start the next iteration
            }catch (Exception e) {}
        }
        return xmlStrings;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_help:
                Intent helpIntent = new Intent(MainActivity.this, InstructionActivity.class);
                MainActivity.this.startActivity(helpIntent);
                return true;
            case R.id.action_disclaimer:
                Intent disclaimerIntent = new Intent(MainActivity.this, DisclaimerActivity.class);
                MainActivity.this.startActivity(disclaimerIntent);
                return true;

            default:

        }
        return super.onMenuItemSelected(item.getItemId(),item);
    }

}

