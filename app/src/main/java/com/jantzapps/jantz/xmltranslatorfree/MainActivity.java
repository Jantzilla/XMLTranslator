package com.jantzapps.jantz.xmltranslatorfree;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
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

import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.R.string.ok;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MultiSelectionSpinner.OnItemSelected {

    private static final int READ_REQUEST_CODE = 42;
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
    private TextView orTextView, fileTextView;
    private FrameLayout chosenFileView;
    private ImageView deleteButton, googleButton, clearButton;
    private boolean translateReady;
    private MultiSelectionSpinner spinner, spinner2;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;
    private ArrayList<String> list, xmlNamesList;
    private String locale;
    private InputStream inputStream;
    private String fileString;
    private FrameLayout pasteEntryLayout;

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
        } else if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                try {
                    uri = data.getData();
                    inputStream = getContentResolver().openInputStream(uri);

                    fileString = parseFileToString(inputStream);

                    if(!validateFileText(fileString)) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Invalid File")
                                .setMessage("Please try another xml file.")
                                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).setIcon(android.R.drawable.ic_dialog_alert).show();

                        inputStream = null;

                    } else {
                        showChosenFile(uri);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
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

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public String parseFileToString(InputStream inputStream) {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder text = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (Exception e) {

        }

        return text.toString();
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
        spinner = (MultiSelectionSpinner)findViewById(R.id.input1);
        spinner2 = (MultiSelectionSpinner)findViewById(R.id.input2);
        final Handler handler = new Handler();
        dbHelper = new DbHelper(this);
        int day = 86400000;
        final int dailyLimit = 3000;
        final File Xml_limit_path = new File(Environment.getExternalStorageDirectory() + "/App_data/");
        final File Xml_limit = new File(Xml_limit_path, "Char.txt");

        toolbar = findViewById(R.id.toolbar);
        buttonBlock = findViewById(R.id.ll_button_block);
        parentLayout = findViewById(R.id.root);
        openFileButton = findViewById(R.id.btn_open_file);
        deleteButton = findViewById(R.id.iv_delete);
        googleButton = findViewById(R.id.iv_google_drive);
        clearButton = findViewById(R.id.iv_clear);
        rawEditText = findViewById(R.id.etEmailMessage);
        orTextView = findViewById(R.id.tv_or_label);
        fileTextView = findViewById(R.id.tv_chosen_file);
        chosenFileView = findViewById(R.id.fl_chosen_file);
        pasteEntryLayout = findViewById(R.id.fl_paste_entry);

        constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);

        locale = Locale.getDefault().getLanguage();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setSupportActionBar(toolbar);

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            }
        });

        openFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputStream = null;
                animateTranslateButton();
            }
        });

        chosenFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        rawEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!translateReady && count > 0)
                    showRawXml();
                else if(translateReady && count == 0)
                    animateTranslateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rawEditText.setText("");
            }
        });

        list = new ArrayList<String>();
        list.add("Afrikaans af"); list.add("Albanian sq"); list.add("Amharic am"); list.add("Arabic ar"); list.add("Armenian hy"); list.add("Azerbaijan az"); list.add("Basque eu"); list.add("Belarusian be"); list.add("Bengali bn"); list.add("Bosnian bs"); list.add("Bulgarian bg");
        list.add("Catalan ca"); list.add("Cebuano ceb"); list.add("Chinese zh"); list.add("Croatian hr"); list.add("Czech cs"); list.add("Danish da"); list.add("Dutch nl"); list.add("English en"); list.add("Esperanto eo"); list.add("Estonian et"); list.add("Finnish fi");
        list.add("French fr"); list.add("Galician gl"); list.add("Georgian ka"); list.add("German de"); list.add("Greek el"); list.add("Gujarati gu"); list.add("Haitian Creole ht"); list.add("Hebrew he");
        list.add("Hill Mari mrj"); list.add("Hindi hi"); list.add("Hungarian hu"); list.add("Icelandic is"); list.add("Indonesian id"); list.add("Irish ga"); list.add("Italian it"); list.add("Japanese ja"); list.add("Javanese jv"); list.add("Kannada kn"); list.add("Kazakh kk"); list.add("Khmer km"); list.add("Korean ko");
        list.add("Kyrgyz ky"); list.add("Laotian lo"); list.add("Latin la"); list.add("Latvian lv"); list.add("Lithuanian lt"); list.add("Luxembourgish lb"); list.add("Macedonian mk"); list.add("Malagasy mg"); list.add("Malay ms"); list.add("Malayalam ml"); list.add("Maltese mt"); list.add("Maori mi"); list.add("Marathi mr"); list.add("Mari mhr"); list.add("Mongolian mn"); list.add("Myanmar (Burmese) my");
        list.add("Nepali ne"); list.add("Norwegian no"); list.add("Papiamento pap"); list.add("Persian fa"); list.add("Polish pl"); list.add("Portuguese pt"); list.add("Punjabi pa"); list.add("Romanian ro"); list.add("Russian ru"); list.add("Scottish gd"); list.add("Serbian sr"); list.add("Sinhala si");
        list.add("Slovak sk"); list.add("Slovenian sl"); list.add("Spanish es"); list.add("Sundanese su"); list.add("Swahili sw"); list.add("Swedish sv"); list.add("Tagalog tl"); list.add("Tajik tg"); list.add("Tamil ta"); list.add("Tatar tt"); list.add("Telugu te"); list.add("Thai th"); list.add("Turkish tr"); list.add("Udmurt udm"); list.add("Ukrainian uk"); list.add("Urdu ur"); list.add("Uzbek uz"); list.add("Vietnamese vi"); list.add("Welsh cy"); list.add("Xhosa xh");
        list.add("Yiddish yi");

        List<String> languages = new ArrayList<String>();

        for(String language : list) {
            int i = language.lastIndexOf(' ');
            languages.add(language.substring(0, i));
        }

        spinner.singleChoice = true;
        spinner.setItems(languages);

        spinner2.setItems(languages);

        if(sharedPreferences.contains("index")) {
            spinner.setSelection(sharedPreferences.getInt("index", 0));
        } else {
            setTranslationToLocale();
            setDefaultTranslation();
        }

        for(int i = 0; i < list.size(); i++) {
            if(sharedPreferences.getBoolean(String.valueOf(i), false)) {
                spinner2.mSelection[i] = true;
            }
        }

        spinner2.validateInputs();

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

                final List<Integer> toIndices = spinner2.getSelectedIndicies();

                final String fromLang = list.get(spinner.selectedIndex);

                StringBuilder toLangBuilder = new StringBuilder();
                for(int index : toIndices) {
                    toLangBuilder.append(list.get(index));
                    toLangBuilder.append(',');
                }

                String toLang = toLangBuilder.toString();


                    if (!xmlStrings.getText().equals("") || inputStream != null) {
                        if (isNetworkConnected()) {

                            if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

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
                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (checkPermission()) {
                                        if (!Xml_limit_path.exists()) {
                                            Xml_limit_path.mkdir();
                                        }
                                        if (!Xml_limit.getParentFile().exists()) {
                                            try {

                                                Xml_limit.getParentFile().createNewFile();
                                                Log.e("Success", "DailyTrac File Created");
                                            } catch (Exception e) {

                                            }
                                        } else {
                                            Log.e("DailyCharCount", "File Exists");
                                        }

                                    } else {
                                        requestPermission(); // Code for permission
                                        if (checkPermission()) {
                                            if (!Xml_limit_path.exists()) {
                                                Xml_limit_path.mkdir();
                                            }
                                            if (!Xml_limit.getParentFile().exists()) {
                                                try {

                                                    Xml_limit.getParentFile().createNewFile();
                                                    Log.e("Success", "DailyTrac File Created");
                                                } catch (Exception e) {

                                                }
                                            } else {
                                                Log.e("DailyCharCount", "File Exists");
                                            }

                                        }
                                    }
                                } else {
                                    // Code for Below 23 API Oriented Device
                                    // Do next code

                                    if (!Xml_limit_path.exists()) {
                                        Xml_limit_path.mkdir();
                                    }
                                    if (!Xml_limit.getParentFile().exists()) {
                                        try {

                                            Xml_limit.getParentFile().createNewFile();


                                        } catch (Exception e) {

                                        }
                                    } else {
                                        Log.e("DailyCharCount", "File Exists");
                                    }
                                }


                                String delims = "[,]";                                                                       //Important!!!  //XML string parsing
                                final String[] toLangs = toLang.split(delims);
                                final ArrayList<String> xmlStringsList;
                                int toLangCount = 0;
                                for (int i = 0; i < toLangs.length; i++) {
                                    toLangCount += 1;
                                }
                                if (!xmlStrings.getText().toString().equals("")) {
                                    xmlStringsList = storeValues(xmlStrings.getText().toString());
                                    xmlNamesList = storeNames(xmlStrings.getText().toString());
                                } else {
                                    xmlStringsList = storeValues(fileString);
                                    xmlNamesList = storeNames(fileString);
                                }

                                int checkChar = 0;

                                for (int i2 = 0; i2 < xmlStringsList.size(); i2++) {

                                    checkChar += xmlStringsList.get(i2).length();

                                }

                                if ((checkChar * toLangCount) + dbHelper.getCharCount() > dailyLimit) {
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

                                    xmlStrings.setText("");

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
                                mGoogleApiClient.clearDefaultAccountAndReconnect();
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
        });
    }

    public boolean validateFileText(String text) {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(text)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void animateTranslateButton() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(parentLayout);

        if(translateReady) {
            constraintSet.clear(R.id.ll_button_block, ConstraintSet.BOTTOM);
            constraintSet.connect(R.id.ll_button_block, ConstraintSet.TOP, R.id.root, ConstraintSet.BOTTOM, 0);
            constraintSet.constrainHeight(R.id.fl_paste_entry, (int) dPToPx(100));
            clearButton.setVisibility(View.GONE);
        } else {
            constraintSet.clear(R.id.ll_button_block, ConstraintSet.TOP);
            constraintSet.connect(R.id.ll_button_block, ConstraintSet.BOTTOM, R.id.root, ConstraintSet.BOTTOM, 0);
            constraintSet.constrainHeight(R.id.fl_paste_entry,ConstraintSet.MATCH_CONSTRAINT);
        }

        translateReady = !translateReady;
        constraintSet.applyTo(parentLayout);
    }

    private float dPToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void showChosenFile(Uri uri) {
        String chosenFile = getFileName(uri);
        animateTranslateButton();
        pasteEntryLayout.setVisibility(View.GONE);
        openFileButton.setVisibility(View.GONE);
        chosenFileView.setVisibility(View.VISIBLE);
        orTextView.setVisibility(View.GONE);

        fileTextView.setText(chosenFile);
    }

    private void showRawXml() {
        constraintSet.constrainHeight(R.id.etEmailMessage,ConstraintSet.MATCH_CONSTRAINT);
        animateTranslateButton();
        openFileButton.setVisibility(View.GONE);
        orTextView.setVisibility(View.GONE);
        clearButton.setVisibility(View.VISIBLE);
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

    public void performFileSearch() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void setTranslationToLocale() {

        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).substring(list.get(i).lastIndexOf(' ') + 1).equals(locale)) {
                spinner.setSelection(i);
                sharedPreferences.edit().putInt("index", i).apply();
                return;
            }
        }

        spinner.setSelection(0);
        sharedPreferences.edit().putInt("index", 0).apply();
    }

    private void setDefaultTranslation() {
        if(!locale.equals("ar")) {
            spinner.setSelection(3);
            sharedPreferences.edit().putBoolean("3", true).apply();
        } if(!locale.equals("zh")) {
            spinner.setSelection(13);
            sharedPreferences.edit().putBoolean("13", true).apply();
        } if(!locale.equals("en")) {
            spinner.setSelection(18);
            sharedPreferences.edit().putBoolean("18", true).apply();
        } if(!locale.equals("fr")) {
            spinner.setSelection(22);
            sharedPreferences.edit().putBoolean("22", true).apply();
        } if(!locale.equals("de")) {
            spinner.setSelection(25);
            sharedPreferences.edit().putBoolean("25", true).apply();
        } if(!locale.equals("es")) {
            spinner.setSelection(73);
            sharedPreferences.edit().putBoolean("73", true).apply();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        locale = newConfig.locale.getLanguage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_privacy_policy:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://jantz.000webhostapp.com/Privacy-Policy/")));
                return true;
            default:

        }
        return super.onMenuItemSelected(item.getItemId(),item);
    }

    @Override
    public void onItemSelectedListener() {
        spinner2.validateInputs();
    }
}

