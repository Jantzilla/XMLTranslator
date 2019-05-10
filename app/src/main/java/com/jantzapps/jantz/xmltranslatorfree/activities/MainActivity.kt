package com.jantzapps.jantz.xmltranslatorfree.activities

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.jantzapps.jantz.xmltranslatorfree.fragments.ConfirmationDialog
import com.jantzapps.jantz.xmltranslatorfree.helpers.GoogleApiHelper
import com.jantzapps.jantz.xmltranslatorfree.receivers.AlarmReceiver
import com.jantzapps.jantz.xmltranslatorfree.helpers.DbHelper
import com.jantzapps.jantz.xmltranslatorfree.services.TranslationService
import com.jantzapps.jantz.xmltranslatorfree.views.MultiSelectionSpinner
import com.jantzapps.jantz.xmltranslatorfree.R
import com.jantzapps.jantz.xmltranslatorfree.utils.XMLFileMaker

import org.xml.sax.InputSource

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.ArrayList
import java.util.Locale

import javax.xml.parsers.DocumentBuilderFactory

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

import android.R.string.ok

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MultiSelectionSpinner.OnItemSelected {
    var file: DriveFile? = null

    private var mGoogleApiClient: GoogleApiClient? = null
    private var openFileButton: Button? = null
    private var stopButton: Button? = null
    private var parentLayout: ConstraintLayout? = null
    private var constraintSet: ConstraintSet? = null
    private var rawEditText: EditText? = null
    private var orTextView: TextView? = null
    private var fileTextView: TextView? = null
    private var translatingLabel: TextView? = null
    private var chosenFileView: FrameLayout? = null
    private var deleteButton: ImageView? = null
    private var googleButton: ImageView? = null
    private var clearButton: ImageView? = null
    private var translateReady: Boolean = false
    private var translating: Boolean = false
    private var spinner: MultiSelectionSpinner? = null
    private var spinner2: MultiSelectionSpinner? = null
    private var sharedPreferences: SharedPreferences? = null
    private var toolbar: Toolbar? = null
    private var list: ArrayList<String>? = null
    private var xmlNamesList: ArrayList<String>? = null
    private var locale: String? = null
    private var inputStream: InputStream? = null
    private var fileString: String? = null
    private var chosenFile: String? = null
    private var pasteEntryLayout: FrameLayout? = null
    private var googleApiHelper: GoogleApiHelper? = null
    private var receiver: BroadcastReceiver? = null
    private var progressBar: ProgressBar? = null
    private var alert: AlertDialog? = null

    private// 1
    // 2
    // 3
    val isNetworkConnected: Boolean
        get() {
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient == null) {

            val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

            if (resultCode != ConnectionResult.SUCCESS) {

                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show()
                } else {
                    finish()
                }

            } else {

                googleApiHelper = GoogleApiHelper(this)
                mGoogleApiClient = googleApiHelper!!.googleApiClient

                mGoogleApiClient!!.connect()
            }
        }

        if (translatingLabel!!.visibility == View.VISIBLE && !translating) {
            animateTranslateButton()
            animateProgressBar()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        Log.v(TAG, "+++++++++++++++++++ onConnected +++++++++++++++++++")
    }

    override fun onConnectionSuspended(i: Int) {
        Log.e(TAG, "onConnectionSuspended [$i]")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed: $result")
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0).show()
            return
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Exception while starting resolution activity", e)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            mGoogleApiClient!!.connect()
        } else if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri?
            if (data != null) {
                try {
                    uri = data.data
                    inputStream = contentResolver.openInputStream(uri!!)

                    fileString = parseFileToString(inputStream)

                    if (!validateFileText(fileString)) {
                        AlertDialog.Builder(this@MainActivity)
                                .setTitle(R.string.invalid_file)
                                .setMessage(R.string.select_different_file)
                                .setPositiveButton(ok) { dialog, which -> }.setIcon(android.R.drawable.ic_dialog_alert).show()

                        inputStream = null

                    } else {
                        val chosenFile = getFileName(uri)
                        showChosenFile(chosenFile)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.disconnect()
        }
        super.onDestroy()
    }

    public override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    fun parseFileToString(inputStream: InputStream?): String {
        val r = BufferedReader(InputStreamReader(inputStream))
        val text = StringBuilder()
        var line: String
        try {
            while ((line = r.readLine()) != null) {
                text.append(line).append('\n')
            }
        } catch (e: Exception) {

        }

        return text.toString()
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission2(): Boolean {
        val result = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this@MainActivity, R.string.write_permission_explain, Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestPermission2() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this@MainActivity, R.string.read_permission_explain, Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE2)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can write local drive .")
            } else {
                Log.e("value", "Permission Denied, You cannot write local drive .")
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setCancelable(true)
                alertBuilder.setTitle(getString(R.string.permission_necessary))
                alertBuilder.setMessage(R.string.write_external_permission_necessary)
                alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which -> requestPermission() }
                val alert = alertBuilder.create()
                alert.show()
            }
            PERMISSION_REQUEST_CODE2 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can read local drive .")
                val Xml_limit_path = File(Environment.getExternalStorageDirectory().toString() + "/App_data/")
                val Xml_limit = File(Xml_limit_path, "Char.txt")
                updateDailyLimit(Xml_limit)

            } else {
                Log.e("value", "Permission Denied, You cannot read local drive .")
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setCancelable(true)
                alertBuilder.setTitle(R.string.permission_necessary)
                alertBuilder.setMessage(R.string.read_external_permission_necessary)
                alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which -> requestPermission2() }
                val alert = alertBuilder.create()
                alert.show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                IntentFilter("translation update")
        )
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver!!)
        if (alert != null && alert!!.isShowing)
            alert!!.dismiss()
        translating = false
        animateProgressBar()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this, ADMOB_APP_ID)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        submit = findViewById<View>(R.id.btnSubmit) as Button
        spinner = findViewById<View>(R.id.input1) as MultiSelectionSpinner
        spinner2 = findViewById<View>(R.id.input2) as MultiSelectionSpinner
        val handler = Handler()
        dbHelper = DbHelper(this)
        val day = 86400000
        val dailyLimit = 3000
        val Xml_limit_path = File(Environment.getExternalStorageDirectory().toString() + "/App_data/")
        val Xml_limit = File(Xml_limit_path, "Char.txt")

        toolbar = findViewById(R.id.toolbar)
        parentLayout = findViewById(R.id.root)
        openFileButton = findViewById(R.id.btn_open_file)
        stopButton = findViewById(R.id.btn_stop)
        progressBar = findViewById(R.id.pb)
        deleteButton = findViewById(R.id.iv_delete)
        googleButton = findViewById(R.id.iv_google_drive)
        clearButton = findViewById(R.id.iv_clear)
        rawEditText = findViewById(R.id.et_paste_entry)
        orTextView = findViewById(R.id.tv_or_label)
        fileTextView = findViewById(R.id.tv_chosen_file)
        translatingLabel = findViewById(R.id.tv_translating)
        chosenFileView = findViewById(R.id.fl_chosen_file)
        pasteEntryLayout = findViewById(R.id.fl_paste_entry)

        constraintSet = ConstraintSet()
        constraintSet!!.clone(parentLayout!!)

        locale = Locale.getDefault().language

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setSupportActionBar(toolbar)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (translatingLabel!!.visibility == View.GONE) {
                    showActivelyTranslating()
                    translating = true
                    animateProgressBar()
                }

                val completedUnits = intent.getIntExtra("completedUnits", 0)
                val totalUnits = intent.getIntExtra("totalUnits", 100)
                progressBar!!.max = totalUnits
                progressBar!!.progress = completedUnits

                if (completedUnits == totalUnits) {
                    if (alert != null && alert!!.isShowing)
                        alert!!.dismiss()
                    translating = false
                    animateProgressBar()
                }
            }
        }

        googleButton!!.setOnClickListener {
            if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
                googleApiHelper!!.clearDefaultAccountAndReconnect()
                mGoogleApiClient = googleApiHelper!!.googleApiClient
            } else
                mGoogleApiClient!!.connect()
        }

        openFileButton!!.setOnClickListener { performFileSearch() }

        deleteButton!!.setOnClickListener {
            if (!translating) {
                inputStream = null
                animateTranslateButton()
            }
        }

        chosenFileView!!.setOnClickListener { performFileSearch() }

        rawEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!translating) {
                    if (!translateReady && s.length > 0) {
                        showRawXml()
                        rawEditText!!.gravity = Gravity.START
                    } else if (translateReady && s.length == 0) {
                        animateTranslateButton()
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        clearButton!!.setOnClickListener { rawEditText!!.setText("") }

        stopButton!!.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(R.string.stop_translation)
            builder.setIcon(R.mipmap.ic_launcher)
            builder.setCancelable(true)
                    .setPositiveButton(R.string.yes) { dialog, id ->
                        val intent = Intent(applicationContext, TranslationService::class.java)
                        intent.putExtra("stopped", true)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else
                            startService(intent)
                    }
                    .setNegativeButton(R.string.no) { dialog, id -> dialog.cancel() }
            alert = builder.create()
            alert!!.show()
        }

        list = ArrayList()
        list!!.add("Afrikaans af")
        list!!.add("Albanian sq")
        list!!.add("Amharic am")
        list!!.add("Arabic ar")
        list!!.add("Armenian hy")
        list!!.add("Azerbaijan az")
        list!!.add("Basque eu")
        list!!.add("Belarusian be")
        list!!.add("Bengali bn")
        list!!.add("Bosnian bs")
        list!!.add("Bulgarian bg")
        list!!.add("Catalan ca")
        list!!.add("Cebuano ceb")
        list!!.add("Chinese zh")
        list!!.add("Croatian hr")
        list!!.add("Czech cs")
        list!!.add("Danish da")
        list!!.add("Dutch nl")
        list!!.add("English en")
        list!!.add("Esperanto eo")
        list!!.add("Estonian et")
        list!!.add("Finnish fi")
        list!!.add("French fr")
        list!!.add("Galician gl")
        list!!.add("Georgian ka")
        list!!.add("German de")
        list!!.add("Greek el")
        list!!.add("Gujarati gu")
        list!!.add("Haitian Creole ht")
        list!!.add("Hebrew he")
        list!!.add("Hill Mari mrj")
        list!!.add("Hindi hi")
        list!!.add("Hungarian hu")
        list!!.add("Icelandic is")
        list!!.add("Indonesian id")
        list!!.add("Irish ga")
        list!!.add("Italian it")
        list!!.add("Japanese ja")
        list!!.add("Javanese jv")
        list!!.add("Kannada kn")
        list!!.add("Kazakh kk")
        list!!.add("Khmer km")
        list!!.add("Korean ko")
        list!!.add("Kyrgyz ky")
        list!!.add("Laotian lo")
        list!!.add("Latin la")
        list!!.add("Latvian lv")
        list!!.add("Lithuanian lt")
        list!!.add("Luxembourgish lb")
        list!!.add("Macedonian mk")
        list!!.add("Malagasy mg")
        list!!.add("Malay ms")
        list!!.add("Malayalam ml")
        list!!.add("Maltese mt")
        list!!.add("Maori mi")
        list!!.add("Marathi mr")
        list!!.add("Mari mhr")
        list!!.add("Mongolian mn")
        list!!.add("Myanmar (Burmese) my")
        list!!.add("Nepali ne")
        list!!.add("Norwegian no")
        list!!.add("Papiamento pap")
        list!!.add("Persian fa")
        list!!.add("Polish pl")
        list!!.add("Portuguese pt")
        list!!.add("Punjabi pa")
        list!!.add("Romanian ro")
        list!!.add("Russian ru")
        list!!.add("Scottish gd")
        list!!.add("Serbian sr")
        list!!.add("Sinhala si")
        list!!.add("Slovak sk")
        list!!.add("Slovenian sl")
        list!!.add("Spanish es")
        list!!.add("Sundanese su")
        list!!.add("Swahili sw")
        list!!.add("Swedish sv")
        list!!.add("Tagalog tl")
        list!!.add("Tajik tg")
        list!!.add("Tamil ta")
        list!!.add("Tatar tt")
        list!!.add("Telugu te")
        list!!.add("Thai th")
        list!!.add("Turkish tr")
        list!!.add("Udmurt udm")
        list!!.add("Ukrainian uk")
        list!!.add("Urdu ur")
        list!!.add("Uzbek uz")
        list!!.add("Vietnamese vi")
        list!!.add("Welsh cy")
        list!!.add("Xhosa xh")
        list!!.add("Yiddish yi")

        val languages = ArrayList<String>()

        for (language in list!!) {
            val i = language.lastIndexOf(' ')
            languages.add(language.substring(0, i))
        }

        spinner!!.singleChoice = true
        spinner!!.setItems(languages)

        spinner2!!.setItems(languages)

        if (sharedPreferences!!.contains("index")) {
            spinner!!.setSelection(sharedPreferences!!.getInt("index", 0))
        } else {
            setTranslationToLocale()
            setDefaultTranslation()
        }

        for (i in list!!.indices) {
            if (sharedPreferences!!.getBoolean(i.toString(), false)) {
                spinner2!!.mSelection[i] = true
            }
        }

        spinner2!!.validateInputs()

        if (!isExternalStorageAvailable && !isExternalStorageWritable) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(R.string.app_name)
            builder.setIcon(R.mipmap.ic_launcher)
            builder.setMessage(getString(R.string.external_storage_must) + "\n" +
                    getString(R.string.mount_external_please))
                    .setCancelable(false)
                    .setPositiveButton(ok) { dialog, id ->
                        val restartIntent = Intent(this@MainActivity, MainActivity::class.java)
                        this@MainActivity.startActivity(restartIntent)
                        finish()
                    }

            val alert = builder.create()
            alert.show()
        }

        try {
            dbHelper.time

        } catch (e: Exception) {
            dbHelper.initialize()
        }

        if (dbHelper.time == 0L) {
            alarm_manager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent2 = Intent(application, AlarmReceiver::class.java)
            intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            mAlarmIntent = PendingIntent.getBroadcast(application, 0, intent2, 0)
            val time = System.currentTimeMillis()
            dbHelper.newTime()

            alarm_manager.set(AlarmManager.RTC_WAKEUP, time + day, mAlarmIntent)

            if (Build.VERSION.SDK_INT >= 23)
                if (checkPermission2())
                    updateDailyLimit(Xml_limit)
                else
                    requestPermission2()
            else
                updateDailyLimit(Xml_limit)

        }


        val yandexLink = findViewById<TextView>(R.id.yandex_link)
        yandexLink.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://translate.yandex.com/"))) }

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }

        }

        submit.setOnClickListener {
            if (!sharedPreferences!!.getBoolean("hide_dialog", false)) {
                val confirmationDialog = ConfirmationDialog()
                confirmationDialog.show(supportFragmentManager, "Dialog")
                supportFragmentManager.executePendingTransactions()
                //                    Dialog d = confirmationDialog.getDialog();
                //                    confirmationDialog.show(getSupportFragmentManager(), "Confirmation");
                confirmationDialog.setPositiveButton {
                    initializeTranslation(Xml_limit_path, Xml_limit, dailyLimit, handler)
                    confirmationDialog.dismiss()
                }
                confirmationDialog.setNegativeButton { confirmationDialog.dismiss() }
                confirmationDialog.setOnCheckedChangeListener { compoundButton, isChecked ->
                    // Store the isChecked to Preference here
                    sharedPreferences!!.edit().putBoolean("hide_dialog", isChecked).apply()
                }

            } else
                initializeTranslation(Xml_limit_path, Xml_limit, dailyLimit, handler)
        }
    }

    fun initializeTranslation(xml_limit_path: File, xml_limit: File, dailyLimit: Int, handler: Handler) {
        val toIndices = spinner2!!.selectedIndicies

        val fromLang = list!![spinner!!.selectedIndex]

        val toLangBuilder = StringBuilder()
        for (index in toIndices) {
            toLangBuilder.append(list!![index])
            toLangBuilder.append(',')
        }

        val toLang = toLangBuilder.toString()


        if (rawEditText!!.text.toString() != "") {

            if (validateFileText(rawEditText!!.text.toString())) {

                if (isNetworkConnected) {

                    if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {

                        startTranslation(fromLang, toLang, xml_limit_path, xml_limit, dailyLimit)

                    } else {
                        mGoogleApiClient!!.connect()
                    }

                } else {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle(getString(R.string.no_connection))
                            .setMessage(getString(R.string.no_connection_direction))
                            .setPositiveButton(ok) { dialog, which -> }.setIcon(android.R.drawable.ic_dialog_alert).show()
                }

            } else {
                AlertDialog.Builder(this@MainActivity)
                        .setTitle(R.string.invalid_text)
                        .setMessage(R.string.enter_valid_text)
                        .setPositiveButton(ok) { dialog, which -> }.setIcon(android.R.drawable.ic_dialog_alert).show()
            }

        } else if (inputStream != null) {

            if (isNetworkConnected) {

                if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {

                    startTranslation(fromLang, toLang, xml_limit_path, xml_limit, dailyLimit)

                } else {
                    mGoogleApiClient!!.connect()
                }

            } else {
                AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.no_connection))
                        .setMessage(getString(R.string.no_connection_direction))
                        .setPositiveButton(ok) { dialog, which -> }.setIcon(android.R.drawable.ic_dialog_alert).show()
            }

        }
    }

    fun showActivelyTranslating() {
        if (sharedPreferences!!.contains("TranslationString") && sharedPreferences!!.getString("TranslationString", "") != "")
            rawEditText!!.setText(sharedPreferences!!.getString("TranslationString", ""))
        else if (sharedPreferences!!.contains("TranslationFile") && sharedPreferences!!.getString("TranslationFile", "") != "")
            showChosenFile(sharedPreferences!!.getString("TranslationFile", ""))
        else
            animateTranslateButton()
    }

    private fun updateDailyLimit(xml_limit: File) {
        var permChar: String
        try {
            val fis = FileInputStream(xml_limit)
            val `in` = DataInputStream(fis)
            val br = BufferedReader(InputStreamReader(`in`))
            var strLine: String
            while ((strLine = br.readLine()) != null) {
                permChar = strLine
                Log.e("DailyCharCount", "File Is Read")
                dbHelper.addCharCount(Integer.valueOf(permChar))
                Log.e("DailyCharCount", dbHelper.charCount.toString())
            }
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun startTranslation(fromLang: String, toLang: String, xml_limit_path: File, xml_limit: File, dailyLimit: Int) {

        if (!checkDailyLimitExists(xml_limit_path, xml_limit))
            return

        val delims = "[,]"                                                                       //Important!!!  //XML string parsing
        val toLangs = toLang.split(delims.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val xmlStringsList: ArrayList<String>
        var toLangCount = 0
        for (i in toLangs.indices) {
            toLangCount += 1
        }
        if (rawEditText!!.text.toString() != "") {
            sharedPreferences!!.edit().putString("TranslationString", rawEditText!!.text.toString()).apply()
            sharedPreferences!!.edit().putString("TranslationFile", "").apply()
            xmlStringsList = storeValues(rawEditText!!.text.toString())
            xmlNamesList = storeNames(rawEditText!!.text.toString())
        } else {
            sharedPreferences!!.edit().putString("TranslationFile", chosenFile).apply()
            sharedPreferences!!.edit().putString("TranslationString", "").apply()
            xmlStringsList = storeValues(fileString)
            xmlNamesList = storeNames(fileString)
        }

        var checkChar = 0

        for (i2 in xmlStringsList.indices) {

            checkChar += xmlStringsList[i2].length

        }

        if (checkChar * toLangCount + dbHelper.charCount!! > dailyLimit) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(R.string.limit_reached)
            builder.setIcon(R.mipmap.ic_launcher)
            builder.setMessage(R.string.daily_limit_reached)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        } else {

            translating = true

            inputStream = null

            showInterstitial()

            animateProgressBar()

            dbHelper.addCharCount(checkChar * toLangCount)

            xml_limit.delete()
            try {
                val bw = BufferedWriter(FileWriter(xml_limit, false))
                bw.write(dbHelper.charCount.toString())
                bw.close()
                Log.e("Success", "DailyTrac File Written To")

                // Details omitted.

            } catch (e: Exception) {
                e.printStackTrace()
                return
            }


            Drive.DriveApi.requestSync(mGoogleApiClient)                                                             //Drive Sync request

            Handler().postDelayed({ XMLFileMaker.checkFolderExists(mGoogleApiClient) }, 1000)

            Handler().postDelayed({
                Drive.DriveApi.requestSync(mGoogleApiClient)                                                             //Drive Sync request
            }, 1000)

            val intent = Intent(this, TranslationService::class.java)
            intent.putExtra("stopped", false)
            intent.putExtra("fromLang", fromLang)
            intent.putExtra("toLangs", toLangs)
            intent.putStringArrayListExtra("xmlStringsList", xmlStringsList)
            intent.putStringArrayListExtra("xmlNamesList", xmlNamesList)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else
                startService(intent)
        }
    }

    fun checkDailyLimitExists(xml_limit_path: File, xml_limit: File): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                if (!xml_limit_path.exists()) {
                    xml_limit_path.mkdir()
                }
                if (!xml_limit.parentFile.exists()) {
                    try {
                        xml_limit.parentFile.createNewFile()
                    } catch (e: Exception) {
                    }

                }
                return true
            } else {
                requestPermission() // Code for permission
                return false
            }
        } else {

            if (!xml_limit_path.exists())
                xml_limit_path.mkdir()

            if (!xml_limit.parentFile.exists()) {
                try {
                    xml_limit.parentFile.createNewFile()
                } catch (e: Exception) {
                }

            }

            return true
        }
    }

    fun showInterstitial() {
        Handler().postDelayed({
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            }
        }, 4000)
    }

    fun validateFileText(text: String?): Boolean {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(text)))
            return true
        } catch (e: Exception) {
            return false
        }

    }

    private fun animateTranslateButton() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(parentLayout)

        if (translateReady) {
            constraintSet!!.clear(R.id.ll_button_block, ConstraintSet.BOTTOM)
            constraintSet!!.connect(R.id.ll_button_block, ConstraintSet.TOP, R.id.root, ConstraintSet.BOTTOM, 0)
            constraintSet!!.constrainHeight(R.id.fl_paste_entry, dPToPx(100).toInt())
            rawEditText!!.gravity = Gravity.CENTER
            clearButton!!.visibility = View.GONE
        } else {
            constraintSet!!.clear(R.id.ll_button_block, ConstraintSet.TOP)
            constraintSet!!.connect(R.id.ll_button_block, ConstraintSet.BOTTOM, R.id.root, ConstraintSet.BOTTOM, 0)
            constraintSet!!.constrainHeight(R.id.fl_paste_entry, ConstraintSet.MATCH_CONSTRAINT)
        }

        translateReady = !translateReady
        constraintSet!!.applyTo(parentLayout!!)
    }

    private fun animateProgressBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(parentLayout)

        val constraintSet2 = ConstraintSet()
        constraintSet2.clone(parentLayout!!)

        if (translating) {
            submit.visibility = View.GONE
            translatingLabel!!.visibility = View.VISIBLE
            stopButton!!.visibility = View.VISIBLE
            progressBar!!.visibility = View.VISIBLE
            constraintSet2.clear(R.id.iv_google_drive, ConstraintSet.END)
            constraintSet2.connect(R.id.iv_google_drive, ConstraintSet.START, R.id.root, ConstraintSet.END, 0)
            constraintSet2.applyTo(parentLayout!!)
        } else {
            constraintSet!!.clear(R.id.iv_google_drive, ConstraintSet.START)
            constraintSet!!.connect(R.id.iv_google_drive, ConstraintSet.END, R.id.root, ConstraintSet.END, 0)
            submit.visibility = View.VISIBLE
            translatingLabel!!.visibility = View.GONE
            stopButton!!.visibility = View.GONE
            progressBar!!.visibility = View.GONE
            rawEditText!!.setText("")
        }
    }

    private fun dPToPx(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    private fun showChosenFile(chosenFile: String?) {
        this.chosenFile = chosenFile
        animateTranslateButton()
        pasteEntryLayout!!.visibility = View.GONE
        openFileButton!!.visibility = View.GONE
        chosenFileView!!.visibility = View.VISIBLE
        orTextView!!.visibility = View.GONE

        fileTextView!!.text = chosenFile
    }

    private fun showRawXml() {
        constraintSet!!.constrainHeight(R.id.et_paste_entry, ConstraintSet.MATCH_CONSTRAINT)
        animateTranslateButton()
        openFileButton!!.visibility = View.GONE
        orTextView!!.visibility = View.GONE
        clearButton!!.visibility = View.VISIBLE
    }

    fun storeNames(string: String?): ArrayList<*> {
        var i = 0
        val xmlNames = ArrayList<String>()
        while (true) {
            try {

                val found = string!!.indexOf("name=\"", i)
                if (found == -1) break
                val start = found + 6 // start of actual name
                val end = string.indexOf("\"", start)

                xmlNames.add(string.substring(start, end))

                i = end + 1  // advance i to start the next iteration
            } catch (e: Exception) {
            }

        }
        return xmlNames
    }

    fun storeValues(string: String?): ArrayList<*> {
        var i = 0
        val xmlStrings = ArrayList<String>()
        while (true) {
            try {

                val found = string!!.indexOf("\">", i)
                if (found == -1) break
                val start = found + 2 // start of actual name
                val end = string.indexOf("</", start)

                xmlStrings.add(string.substring(start, end))

                i = end + 1  // advance i to start the next iteration
            } catch (e: Exception) {
            }

        }
        return xmlStrings
    }

    fun performFileSearch() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    private fun setTranslationToLocale() {

        for (i in list!!.indices) {
            if (list!![i].substring(list!![i].lastIndexOf(' ') + 1) == locale) {
                spinner!!.setSelection(i)
                sharedPreferences!!.edit().putInt("index", i).apply()
                return
            }
        }

        spinner!!.setSelection(0)
        sharedPreferences!!.edit().putInt("index", 0).apply()
    }

    private fun setDefaultTranslation() {
        if (locale != "ar")
            sharedPreferences!!.edit().putBoolean("3", true).apply()
        if (locale != "zh")
            sharedPreferences!!.edit().putBoolean("13", true).apply()
        if (locale != "en")
            sharedPreferences!!.edit().putBoolean("18", true).apply()
        if (locale != "fr")
            sharedPreferences!!.edit().putBoolean("22", true).apply()
        if (locale != "de")
            sharedPreferences!!.edit().putBoolean("25", true).apply()
        if (locale != "es")
            sharedPreferences!!.edit().putBoolean("73", true).apply()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        locale = newConfig.locale.language
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_privacy_policy -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://jantz.000webhostapp.com/Privacy-Policy/")))
                return true
            }
        }
        return super.onMenuItemSelected(item.itemId, item)
    }

    override fun onItemSelectedListener() {
        spinner2!!.validateInputs()
    }

    companion object {

        private val READ_REQUEST_CODE = 42
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private val TAG = "<< DRIVE >>"
        protected val REQUEST_CODE_RESOLUTION = 1338
        private val PERMISSION_REQUEST_CODE = 1
        private val PERMISSION_REQUEST_CODE2 = 2
        private val ADMOB_APP_ID = "ca-app-pub-5985384760144093~6592515362"

        private val isExternalStorageAvailable: Boolean
            get() {
                val extStorageState = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == extStorageState
            }
    }
}

