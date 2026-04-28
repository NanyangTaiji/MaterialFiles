/*
 * Merged from:
 *   - AppProvider.kt.txt           (ContentProvider initializer → Application subclass)
 *   - AppInitializers.kt.txt       (initialization steps)
 *   - SystemServices.kt.txt        (lazy system service accessors)
 *   - BackgroundActivityStarter.kt.txt
 *   - NotificationIds.kt.txt
 *
 * AppUpgrader / AppUpgraders are intentionally excluded per request.
 */

package me.zhanghai.android.files.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.PowerManager
import android.os.storage.StorageManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import jcifs.context.SingletonContext
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.R
import me.zhanghai.android.files.coil.initializeCoil
import me.zhanghai.android.files.compat.mainExecutorCompat
import me.zhanghai.android.files.filejob.fileJobNotificationTemplate
import me.zhanghai.android.files.ftpserver.ftpServerServiceNotificationTemplate
import me.zhanghai.android.files.hiddenapi.HiddenApi
import me.zhanghai.android.files.provider.FileSystemProviders
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.storage.FtpServerAuthenticator
import me.zhanghai.android.files.storage.SftpServerAuthenticator
import me.zhanghai.android.files.storage.SmbServerAuthenticator
import me.zhanghai.android.files.storage.StorageVolumeListLiveData
import me.zhanghai.android.files.storage.WebDavServerAuthenticator
import me.zhanghai.android.files.theme.custom.CustomThemeHelper
import me.zhanghai.android.files.theme.night.NightModeHelper
import me.zhanghai.android.files.util.NotificationChannelTemplate
import me.zhanghai.android.files.util.NotificationTemplate
import me.zhanghai.android.files.util.startActivitySafe
import okhttp3.OkHttpClient
import java.util.Properties
import java.util.concurrent.Executor
import me.zhanghai.android.files.provider.ftp.client.Client as FtpClient
import me.zhanghai.android.files.provider.sftp.client.Client as SftpClient
import me.zhanghai.android.files.provider.smb.client.Client as SmbClient
import me.zhanghai.android.files.provider.webdav.client.Client as WebDavClient

// ---------------------------------------------------------------------------
// Notification IDs  (NotificationIds.kt.txt)
// ---------------------------------------------------------------------------

object NotificationIds {
    const val FTP_SERVER = 1
}

// ---------------------------------------------------------------------------
// Global application reference  (AppProvider.kt.txt)
// Mirrors the original `lateinit var application: Application private set`.
// ---------------------------------------------------------------------------

lateinit var application: MyApp
    private set

// ---------------------------------------------------------------------------
// Lazy system-service accessors  (SystemServices.kt.txt)
// These remain top-level properties so call-sites need no change.
// ---------------------------------------------------------------------------

val appClassLoader: ClassLoader
    get() = MyApp::class.java.classLoader!!

val clipboardManager: ClipboardManager by lazy {
    application.getSystemService(ClipboardManager::class.java)
}

val contentResolver: ContentResolver by lazy { application.contentResolver }

val defaultSharedPreferences: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(application)
}

val okHttpClient: OkHttpClient by lazy { OkHttpClient() }

val inputMethodManager: InputMethodManager by lazy {
    application.getSystemService(InputMethodManager::class.java)
}

val mainExecutor: Executor by lazy { application.mainExecutorCompat }

val notificationManager: NotificationManagerCompat by lazy {
    NotificationManagerCompat.from(application)
}

val packageManager: PackageManager by lazy { application.packageManager }

val powerManager: PowerManager by lazy {
    application.getSystemService(PowerManager::class.java)
}

val storageManager: StorageManager by lazy {
    application.getSystemService(StorageManager::class.java)
}

val wifiManager: WifiManager by lazy {
    application.applicationContext.getSystemService(WifiManager::class.java)
}

// ---------------------------------------------------------------------------
// Background-activity notification template  (BackgroundActivityStarter.kt.txt)
// ---------------------------------------------------------------------------

val backgroundActivityStartNotificationTemplate = NotificationTemplate(
    NotificationChannelTemplate(
        "background_activity_start",
        R.string.notification_channel_background_activity_start_name,
        NotificationManagerCompat.IMPORTANCE_HIGH,
        descriptionRes = R.string.notification_channel_background_activity_start_description,
        showBadge = false
    ),
    colorRes = R.color.color_primary,
    smallIcon = R.drawable.notification_icon,
    ongoing = true,
    autoCancel = true,
    category = NotificationCompat.CATEGORY_ERROR,
    priority = NotificationCompat.PRIORITY_HIGH
)

// ---------------------------------------------------------------------------
// Application class  (AppProvider.kt.txt + AppInitializers.kt.txt)
// ---------------------------------------------------------------------------

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
        runInitializers()
    }

    /**
     * Ordered initialisation steps — mirrors [appInitializers] from AppInitializers.kt.txt.
     * AppUpgrader is intentionally omitted per request.
     */
    private fun runInitializers() {
        initializeCrashlytics()
        disableHiddenApiChecks()
        initializeWebViewDebugging()
        initializeCoil()
        initializeFileSystemProviders()
        // upgradeApp() — excluded per request
        initializeLiveDataObjects()
        initializeCustomTheme()
        initializeNightMode()
        createNotificationChannels()
    }

    private fun initializeCrashlytics() {
        //#ifdef NONFREE
        me.zhanghai.android.files.nonfree.CrashlyticsInitializer.initialize()
        //#endif
    }

    private fun disableHiddenApiChecks() {
        HiddenApi.disableHiddenApiChecks()
    }

    private fun initializeWebViewDebugging() {
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    @Suppress("DEPRECATION") // AsyncTask.THREAD_POOL_EXECUTOR: fine for fire-and-forget
    private fun initializeFileSystemProviders() {
        FileSystemProviders.install()
        FileSystemProviders.overflowWatchEvents = true
        // SingletonContext.init() connects to the network, so push it off the main thread.
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            SingletonContext.init(
                Properties().apply {
                    setProperty("jcifs.netbios.cachePolicy", "0")
                    setProperty("jcifs.smb.client.maxVersion", "SMB1")
                }
            )
        }
        FtpClient.authenticator = FtpServerAuthenticator
        SftpClient.authenticator = SftpServerAuthenticator
        SmbClient.authenticator = SmbServerAuthenticator
        WebDavClient.authenticator = WebDavServerAuthenticator
    }

    private fun initializeLiveDataObjects() {
        // Force init on the main thread so it never happens on a background thread.
        StorageVolumeListLiveData.value
        Settings.FILE_LIST_DEFAULT_DIRECTORY.value
    }

    private fun initializeCustomTheme() {
        CustomThemeHelper.initialize(application)
    }

    private fun initializeNightMode() {
        NightModeHelper.initialize(application)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannels(
                listOf(
                    backgroundActivityStartNotificationTemplate.channelTemplate,
                    fileJobNotificationTemplate.channelTemplate,
                    ftpServerServiceNotificationTemplate.channelTemplate
                ).map { it.create(application) }
            )
        }
    }

    // -----------------------------------------------------------------------
    // BackgroundActivityStarter  (BackgroundActivityStarter.kt.txt)
    // Folded into MyApp since it is a pure app-level utility with no state
    // of its own beyond what ProcessLifecycleOwner already tracks.
    // -----------------------------------------------------------------------

    /**
     * Start [intent] immediately when the app is in the foreground, or post a
     * high-priority notification so the user can tap to launch it from the background.
     */
    fun startBackgroundActivity(intent: Intent, title: CharSequence, text: CharSequence?) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (isInForeground) {
            startActivitySafe(intent)
        } else {
            notifyStartActivity(intent, title, text)
        }
    }

    private val isInForeground: Boolean
        get() = ProcessLifecycleOwner.get()
            .lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    // The original Kotlin accepts a potential SecurityException from the file-job layer,
    // so MissingPermission is suppressed here deliberately.
    @SuppressLint("MissingPermission")
    private fun notifyStartActivity(intent: Intent, title: CharSequence, text: CharSequence?) {
        var flags = PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        val pendingIntent = PendingIntent.getActivity(this, intent.hashCode(), intent, flags)
        val notification = backgroundActivityStartNotificationTemplate.createBuilder(this)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(intent.hashCode(), notification)
    }
}
