package com.example.agoravideocalling


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas


class MainActivity : AppCompatActivity() {
    // Fill in the App ID obtained from the Agora Console
    private val appId = "6a7a297a62c7476c9ab0363eec7cc427"

    // Fill in the channel name
    private val channelName = "Sudhir"

    // Fill in the temporary token generated from Agora Console
//    private val token = "007eJxTYLhtJT/t/RptplKBRSc5vTkf1v9jObuiZ6fTHdXAr9VuBawKDIYWiYaGqRamiclp5iYppilJSUlmRqlJpuZmiYlmFubmJRefpzUEMjLsPBXCysgAgSA+G0NwaUpGZhEDAwB1NCCr"
    private val token = "007eJxTYJh65lT8zpjf+VzXv+45Gbbi9WKHy56s0RzRNl3/7V2bar4oMJglmicaWZonmhklm5uYmyVbJiYZGJsZp6YmmycnmxiZmwS8TGsIZGTYFH2ZiZEBAkF8Nobg0pSMzCIGBgAyIiIk"

    private var mRtcEngine: RtcEngine? =null

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Callback when successfully joining the channel
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Join channel success",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Callback when a remote user or host joins the current channel
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                // When a remote user joins the channel, display the remote video stream for the specified uid
                setupRemoteVideo(uid)
            }
        }

        // Callback when a remote user or host leaves the current channel
        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "User offline: $uid",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun initializeAndJoinChannel() {
        try {
            // Create an RtcEngineConfig instance and configure it
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            // Create and initialize an RtcEngine instance
            mRtcEngine = RtcEngine.create(config)

            mRtcEngine?.let {
                // Enable the video module
                it.enableVideo()

                // Enable local preview
                it.startPreview()

                // Create a SurfaceView object and make it a child object of FrameLayout
                val container = findViewById<FrameLayout>(R.id.local_video_view_container)
                val surfaceView = SurfaceView(baseContext)
                container.addView(surfaceView)
                // Pass the SurfaceView object to the SDK and set the local view
                it.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))

                // Create an instance of ChannelMediaOptions and configure it
                val options = ChannelMediaOptions()
                // Set the user role to BROADCASTER or AUDIENCE according to the scenario
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                // In the video calling scenario, set the channel profile to CHANNEL_PROFILE_COMMUNICATION
                options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION

                // Join the channel using a temporary token and channel name, setting uid to 0 means the engine will randomly generate a username
                // The onJoinChannelSuccess callback will be triggered upon success
                it.joinChannel(token, channelName, 0, options)
            } ?: run {
                Log.e("MainActivity", "Failed to create RtcEngine instance")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing and joining channel", e)
        }
    }
    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        val surfaceView = SurfaceView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        // Pass the SurfaceView object to the SDK and set the remote view
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private val requiredPermissions: Array<String>
        // Obtain recording, camera and other permissions required to implement real-time audio and video interaction
        get() =// Determine the permissions required when targetSDKVersion is 31 or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,  // Recording permission
                    Manifest.permission.CAMERA,  // Camera permission
                    Manifest.permission.READ_PHONE_STATE,  // Permission to read phone status
                    Manifest.permission.BLUETOOTH_CONNECT // Bluetooth connection permission
                )
            } else {
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            }

    private fun checkPermissions(): Boolean {
        for (permission in requiredPermissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Hide system UI using WindowInsetsController (Android 11+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val insetsController = window.insetsController
//            if (insetsController != null) {
//                insetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//                insetsController.systemBarsBehavior =
//                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//        } else {
//            // Fallback for older Android versions
//            hideSystemUIForOlderVersions()
//        }

        // Enable immersive full-screen mode
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        // Hide the title bar programmatically
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getSupportActionBar()?.hide();

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)


        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the bottom bar (navigation bar)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        // If authorized, initialize RtcEngine and join the channel
        if (checkPermissions()) {
            initializeAndJoinChannel()
        } else {
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions, PERMISSION_REQ_ID
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Use WindowInsetsController to hide system bars if API level is 30 or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insetsController = window.decorView.windowInsetsController
                if (insetsController != null) {
                    insetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Fallback for older Android versions
                hideSystemUIForOlderVersions()
            }
        }
    }

    private fun hideSystemUIForOlderVersions() {
        // For Android versions below 11
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }














//    private fun hideSystemUIForOlderVersions() {
//        // For Android versions below 11
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
//    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                val insetsController = window.insetsController
//                insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//            } else {
//                hideSystemUIForOlderVersions()
//            }
//        }
//    }


//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
//        }
//    }

    // System permission request callback
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermissions()) {
            initializeAndJoinChannel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop local video preview
        mRtcEngine!!.stopPreview()
        // Leave the channel
        mRtcEngine!!.leaveChannel()
    }

    companion object {
        private const val PERMISSION_REQ_ID = 22
    }
}













//package com.example.agoravideocalling
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.view.SurfaceView
//import android.widget.FrameLayout
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import io.agora.rtc2.ChannelMediaOptions
//import io.agora.rtc2.Constants
//import io.agora.rtc2.IRtcEngineEventHandler
//import io.agora.rtc2.RtcEngine
//import io.agora.rtc2.RtcEngineConfig
//import io.agora.rtc2.video.VideoCanvas
//
//
//
//
//class MainActivity : AppCompatActivity() {
//    // Fill in the App ID obtained from the Agora Console
//    private val appId = "18a11e85acf74d5dbbb62eb576aa6877"
//
//    // Fill in the channel name
//    private val channelName = "Sudhir"
//
//    // Fill in the temporary token generated from Agora Console
//    private val token = "007eJxTYLhtJT/t/RptplKBRSc5vTkf1v9jObuiZ6fTHdXAr9VuBawKDIYWiYaGqRamiclp5iYppilJSUlmRqlJpuZmiYlmFubmJRefpzUEMjLsPBXCysgAgSA+G0NwaUpGZhEDAwB1NCCr"
//
//    private var mRtcEngine: RtcEngine? = null
//
//    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
//        // Callback when successfully joining the channel
//        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
//            super.onJoinChannelSuccess(channel, uid, elapsed)
//            runOnUiThread {
//                Toast.makeText(
//                    this@MainActivity,
//                    "Join channel success",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//
//        // Callback when a remote user or host joins the current channel
//        override fun onUserJoined(uid: Int, elapsed: Int) {
//            runOnUiThread {
//                // When a remote user joins the channel, display the remote video stream for the specified uid
//                setupRemoteVideo(uid)
//            }
//        }
//
//        // Callback when a remote user or host leaves the current channel
//        override fun onUserOffline(uid: Int, reason: Int) {
//            super.onUserOffline(uid, reason)
//            runOnUiThread {
//                Toast.makeText(
//                    this@MainActivity,
//                    "User offline: $uid",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//    private fun initializeAndJoinChannel() {
//        try {
//            // Create an RtcEngineConfig instance and configure it
//            var config = RtcEngineConfig()
//            config.context = applicationContext // Use applicationContext
//            config.appId = appId
//            config.mEventHandler = mRtcEventHandler
//            // Create and initialize an RtcEngine instance
//            mRtcEngine = RtcEngine.create(config)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            throw RuntimeException("Check the error.")
//        }
//
//        // Enable the video module
//        mRtcEngine?.enableVideo()
//
//        // Enable local preview
//        mRtcEngine?.startPreview()
//
//        // Create a SurfaceView object and make it a child object of FrameLayout
//        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
//        val surfaceView = SurfaceView(this) // Use `this` context
//        container.addView(surfaceView)
//        // Pass the SurfaceView object to the SDK and set the local view
//        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
//
//        // Create an instance of ChannelMediaOptions and configure it
//        val options = ChannelMediaOptions().apply {
//            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
//            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
//        }
//
//        // Join the channel using a temporary token and channel name, setting uid to 0 means the engine will randomly generate a username
//        mRtcEngine?.joinChannel(token, channelName, null, options)
//    }
//
//
//
//    private fun setupRemoteVideo(uid: Int) {
//        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
//        val surfaceView = SurfaceView(baseContext)
//        surfaceView.setZOrderMediaOverlay(true)
//        container.addView(surfaceView)
//        // Pass the SurfaceView object to the SDK and set the remote view
//        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
//    }
//
//    private val requiredPermissions: Array<String>
//        // Obtain recording, camera and other permissions required to implement real-time audio and video interaction
//        get() =// Determine the permissions required when targetSDKVersion is 31 or above
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                arrayOf(
//                    Manifest.permission.RECORD_AUDIO,  // Recording permission
//                    Manifest.permission.CAMERA,  // Camera permission
//                    Manifest.permission.READ_PHONE_STATE,  // Permission to read phone status
//                    Manifest.permission.BLUETOOTH_CONNECT // Bluetooth connection permission
//                )
//            } else {
//                arrayOf(
//                    Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.CAMERA
//                )
//            }
//
//    private fun checkPermissions(): Boolean {
//        for (permission in requiredPermissions) {
//            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                return false
//            }
//        }
//        return true
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        // If authorized, initialize RtcEngine and join the channel
//        if (checkPermissions()) {
//            initializeAndJoinChannel()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                requiredPermissions, PERMISSION_REQ_ID
//            )
//        }
//    }
//
//    // System permission request callback
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (checkPermissions()) {
//            initializeAndJoinChannel()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Stop local video preview
//        mRtcEngine!!.stopPreview()
//        // Leave the channel
//        mRtcEngine!!.leaveChannel()
//    }
//
//    companion object {
//        private const val PERMISSION_REQ_ID = 22
//    }
//}