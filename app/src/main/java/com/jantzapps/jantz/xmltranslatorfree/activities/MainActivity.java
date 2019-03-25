package com.jantzapps.jantz.xmltranslatorfree.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.jantzapps.jantz.xmltranslatorfree.helpers.GoogleApiHelper;
import com.jantzapps.jantz.xmltranslatorfree.receivers.AlarmReceiver;
import com.jantzapps.jantz.xmltranslatorfree.helpers.DbHelper;
import com.jantzapps.jantz.xmltranslatorfree.services.TranslationService;
import com.jantzapps.jantz.xmltranslatorfree.views.MultiSelectionSpinner;
import com.jantzapps.jantz.xmltranslatorfree.R;
import com.jantzapps.jantz.xmltranslatorfree.utils.XMLFileMaker;

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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.R.string.ok;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MultiSelectionSpinner.OnItemSelected {

    private static final int READ_REQUEST_CODE = 42;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public DriveFile file;

    Button submit;
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
    private Button openFileButton, stopButton;
    private ConstraintLayout buttonBlock, parentLayout;
    private ConstraintSet constraintSet;
    private EditText rawEditText;
    private TextView orTextView, fileTextView, translatingLabel;
    private FrameLayout chosenFileView;
    private ImageView deleteButton, googleButton, clearButton;
    private boolean translateReady, translating;
    private MultiSelectionSpinner spinner, spinner2;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;
    private ArrayList<String> list, xmlNamesList;
    private String locale;
    private InputStream inputStream;
    private String fileString;
    private FrameLayout pasteEntryLayout;
    private TranslationService translateService;
    private GoogleApiHelper googleApiHelper;
    private BroadcastReceiver receiver;
    private ProgressBar progressBar;

    @Override protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {

            int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

            if (resultCode != ConnectionResult.SUCCESS) {

                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Log.i(TAG, "This device is not supported.");
                    finish();
                }

            } else {

                googleApiHelper = new GoogleApiHelper(this);
                mGoogleApiClient = googleApiHelper.getGoogleApiClient();

                mGoogleApiClient.connect();
            }
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
                    updateDailyLimit(Xml_limit);

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
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter()
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
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
        stopButton = findViewById(R.id.btn_stop);
        progressBar = findViewById(R.id.pb);
        deleteButton = findViewById(R.id.iv_delete);
        googleButton = findViewById(R.id.iv_google_drive);
        clearButton = findViewById(R.id.iv_clear);
        rawEditText = findViewById(R.id.etEmailMessage);
        orTextView = findViewById(R.id.tv_or_label);
        fileTextView = findViewById(R.id.tv_chosen_file);
        translatingLabel = findViewById(R.id.tv_translating);
        chosenFileView = findViewById(R.id.fl_chosen_file);
        pasteEntryLayout = findViewById(R.id.fl_paste_entry);

        constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);

        locale = Locale.getDefault().getLanguage();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setSupportActionBar(toolbar);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // do something here.
            }
        };

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    googleApiHelper.clearDefaultAccountAndReconnect();
                    mGoogleApiClient = googleApiHelper.getGoogleApiClient();
                } else
                    mGoogleApiClient.connect();
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
                if(!translating) {
                    inputStream = null;
                    animateTranslateButton();
                }
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
                if(!translating) {
                    if (!translateReady && count > 0) {
                        showRawXml();
                        rawEditText.setGravity(Gravity.START);
                    } else if (translateReady && count == 0) {
                        animateTranslateButton();
                        rawEditText.setGravity(Gravity.CENTER);
                    }
                }
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
                if (checkPermission2())
                    updateDailyLimit(Xml_limit);
                else
                    requestPermission2();
            else
                updateDailyLimit(Xml_limit);

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


                    if (!rawEditText.getText().toString().equals("")) {

                        if(validateFileText(rawEditText.getText().toString())) {

                            if (isNetworkConnected()) {

                                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

                                    startTranslation(fromLang, toLang, Xml_limit_path, Xml_limit, dailyLimit, handler);

                                } else {
                                    mGoogleApiClient.connect();
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

                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Invalid Text")
                                    .setMessage("Please enter text in XML format.")
                                    .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                        }

                    } else if(inputStream != null) {

                        if (isNetworkConnected()) {

                            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

                                startTranslation(fromLang, toLang, Xml_limit_path, Xml_limit, dailyLimit, handler);

                            } else {
                                mGoogleApiClient.connect();
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

    private void updateDailyLimit(File xml_limit) {
        String permChar;
        try {
            FileInputStream fis = new FileInputStream(xml_limit);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                permChar = strLine;
                Log.e("DailyCharCount", "File Is Read");
                dbHelper.addCharCount(Integer.valueOf(permChar));
                Log.e("DailyCharCount", String.valueOf(dbHelper.getCharCount()));
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTranslation(String fromLang, String toLang, File xml_limit_path, File xml_limit, int dailyLimit, final Handler handler) {

        translating = true;

        showInterstitial();

        checkDailyLimitExists(xml_limit_path, xml_limit);

        String delims = "[,]";                                                                       //Important!!!  //XML string parsing
        final String[] toLangs = toLang.split(delims);
        final ArrayList<String> xmlStringsList;
        int toLangCount = 0;
        for (int i = 0; i < toLangs.length; i++) {
            toLangCount += 1;
        }
        if (!rawEditText.getText().toString().equals("")) {
            xmlStringsList = storeValues(rawEditText.getText().toString());
            xmlNamesList = storeNames(rawEditText.getText().toString());
            animateProgressBar();
        } else {
            xmlStringsList = storeValues(fileString);
            xmlNamesList = storeNames(fileString);
            inputStream = null;
            animateProgressBar();
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

            xml_limit.delete();
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(xml_limit, false));
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
                    XMLFileMaker.checkFolderExists(mGoogleApiClient);
                }
            }, 1000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Drive.DriveApi.requestSync(mGoogleApiClient);                                                             //Drive Sync request
                }
            }, 1000);

            rawEditText.setText("");

            Intent intent = new Intent(this, TranslationService.class);
            intent.putExtra("fromLang", fromLang);
            intent.putExtra("toLangs", toLangs);
            intent.putStringArrayListExtra("xmlStringsList", xmlStringsList);
            intent.putStringArrayListExtra("xmlNamesList", xmlNamesList);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else
                startService(intent);
        }
    }

    public void checkDailyLimitExists(File xml_limit_path, File xml_limit) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                if (!xml_limit_path.exists()) {
                    xml_limit_path.mkdir();
                }
                if (!xml_limit.getParentFile().exists()) {
                    try {
                        xml_limit.getParentFile().createNewFile();
                    } catch (Exception e) {}
                }
            } else {
                requestPermission(); // Code for permission
                if (checkPermission()) {
                    if (!xml_limit_path.exists())
                        xml_limit_path.mkdir();

                    if (!xml_limit.getParentFile().exists()) {
                        try {
                            xml_limit.getParentFile().createNewFile();
                        } catch (Exception e) {}
                    }
                }
            }
        } else {

            if (!xml_limit_path.exists())
                xml_limit_path.mkdir();

            if (!xml_limit.getParentFile().exists()) {
                try {
                    xml_limit.getParentFile().createNewFile();
                } catch (Exception e) {}
            }
        }
    }

    public void showInterstitial() {
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

    private void animateProgressBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(parentLayout);

        if(translating) {
            submit.setVisibility(View.GONE);
            translatingLabel.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            constraintSet.clear(R.id.iv_google_drive, ConstraintSet.END);
            constraintSet.connect(R.id.iv_google_drive, ConstraintSet.START, R.id.root, ConstraintSet.END, 0);
        } else {
            constraintSet.clear(R.id.iv_google_drive, ConstraintSet.START);
            constraintSet.connect(R.id.iv_google_drive, ConstraintSet.END, R.id.root, ConstraintSet.END, 0);
            submit.setVisibility(View.VISIBLE);
            translatingLabel.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

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
        if(!locale.equals("ar"))
            sharedPreferences.edit().putBoolean("3", true).apply();
        if(!locale.equals("zh"))
            sharedPreferences.edit().putBoolean("13", true).apply();
        if(!locale.equals("en"))
            sharedPreferences.edit().putBoolean("18", true).apply();
        if(!locale.equals("fr"))
            sharedPreferences.edit().putBoolean("22", true).apply();
        if(!locale.equals("de"))
            sharedPreferences.edit().putBoolean("25", true).apply();
        if(!locale.equals("es"))
            sharedPreferences.edit().putBoolean("73", true).apply();
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

