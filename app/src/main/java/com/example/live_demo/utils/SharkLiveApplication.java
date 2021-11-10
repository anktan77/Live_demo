package com.example.live_demo.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.capture.video.camera.CameraManager;
import android.text.TextUtils;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.formatter.message.json.DefaultJsonFormatter;
import com.elvishew.xlog.formatter.message.throwable.DefaultThrowableFormatter;
import com.elvishew.xlog.formatter.message.xml.DefaultXmlFormatter;
import com.elvishew.xlog.formatter.stacktrace.DefaultStackTraceFormatter;
import com.elvishew.xlog.formatter.thread.DefaultThreadFormatter;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.example.faceunity.FURenderer;
import com.example.live_demo.BuildConfig;
import com.example.live_demo.R;
import com.example.live_demo.framework.PreprocessorFaceUnity;
import com.example.live_demo.protocol.model.ClientProxy;
import com.example.live_demo.vlive.Config;
import com.example.live_demo.vlive.shark.SharkEngine;
import com.example.live_demo.vlive.shark.rtc.RtcEventHandler;
import com.tencent.bugly.crashreport.CrashReport;

import io.agora.rtc.RtcEngine;
import io.agora.rtm.RtmClient;

public class SharkLiveApplication extends Application {
    //khi khởi động nó sẽ chạy song song với các activity khác giúp truyền, sử dụng phương thức

    private static final String TAG = SharkLiveApplication.class.getSimpleName();
    private SharedPreferences mPref;
    private Config mConfig;
    private SharkEngine mAgoraEngine;
    private CameraManager mCameraVideoManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = getSharedPreferences(Global.Constants.SF_NAME, Context.MODE_PRIVATE);
        mConfig = new Config(this);
        initXLog();
        initVideoGlobally();
        initCrashReport();
        XLog.i("onApplicationCreate");
    }

    public Config config() {
        return mConfig;
    }

    public SharedPreferences preferences() {
        return mPref;
    }

    public void initEngine(String appId) {
        mAgoraEngine = new SharkEngine(this, appId);
    }

    public RtcEngine rtcEngine() {
        return mAgoraEngine != null ? mAgoraEngine.rtcEngine() : null;
    }

    public RtmClient rtmClient() {
        return mAgoraEngine != null ? mAgoraEngine.rtmClient() : null;
    }

    public ClientProxy proxy() {
        return ClientProxy.instance();
    }

    public void registerRtcHandler(RtcEventHandler handler) {
        mAgoraEngine.registerRtcHandler(handler);
    }

    public void removeRtcHandler(RtcEventHandler handler) {
        mAgoraEngine.removeRtcHandler(handler);
    }

    public CameraManager cameraVideoManager() {
        return mCameraVideoManager;
    }

    // setup camera
    private void initVideoGlobally() {
        new Thread(() -> {
            FURenderer.initFURenderer(getApplicationContext());
            PreprocessorFaceUnity preprocessor =
                    new PreprocessorFaceUnity(this);
            // quan trọng là cái này contrustor
            mCameraVideoManager = new CameraManager(
                    this, preprocessor);
            mCameraVideoManager.setCameraStateListener(preprocessor);
        }).start();
    }

    private void initXLog() {
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(BuildConfig.DEBUG ?
                        LogLevel.DEBUG : LogLevel.INFO)                         // Specify log level, logs below this level won't be printed, default: LogLevel.ALL
                .tag("SharkLive")                                               // Specify TAG, default: "X-LOG"
                //.t()                                                            // Enable thread info, disabled by default
                .st(Global.Constants.LOG_CLASS_DEPTH)                           // Enable stack trace info with depth 2, disabled by default
                // .b()                                                            // Enable border, disabled by default
                .jsonFormatter(new DefaultJsonFormatter())                      // Default: DefaultJsonFormatter
                .xmlFormatter(new DefaultXmlFormatter())                        // Default: DefaultXmlFormatter
                .throwableFormatter(new DefaultThrowableFormatter())            // Default: DefaultThrowableFormatter
                .threadFormatter(new DefaultThreadFormatter())                  // Default: DefaultThreadFormatter
                .stackTraceFormatter(new DefaultStackTraceFormatter())          // Default: DefaultStackTraceFormatter
                .build();

        Printer androidPrinter = new AndroidPrinter();                          // Printer that print the log using android.util.Log

        String flatPattern = "{d yy/MM/dd HH:mm:ss} {l}|{t}: {m}";
        Printer filePrinter = new FilePrinter                                   // Printer that print the log to the file system
                .Builder(UserUtil.appLogFolderPath(this))         // Specify the path to save log file
                .fileNameGenerator(new DateFileNameGenerator())                 // Default: ChangelessFileNameGenerator("log")
                .backupStrategy(new FileSizeBackupStrategy(
                        Global.Constants.APP_LOG_SIZE))                         // Default: FileSizeBackupStrategy(1024 * 1024)
                .cleanStrategy(new FileLastModifiedCleanStrategy(
                        Global.Constants.LOG_DURATION))
                .flattener(new PatternFlattener(flatPattern))                   // Default: DefaultFlattener
                .build();

        XLog.init(                                                              // Initialize XLog
                config,                                                         // Specify the log configuration, if not specified, will use new LogConfiguration.Builder().build()
                androidPrinter,
                filePrinter);
    }

    private void initCrashReport() {
        String buglyAppId = getResources().getString(R.string.bugly_app_id);
        if (TextUtils.isEmpty(buglyAppId)) {
            XLog.i("Bugly app id not found, crash report initialize skipped");
        } else {
            CrashReport.initCrashReport(getApplicationContext(),
                    buglyAppId, BuildConfig.DEBUG);
        }
    }

    @Override
    public void onTerminate() {
        XLog.i("onApplicationTerminate");
        super.onTerminate();
        mAgoraEngine.release();
    }
}
