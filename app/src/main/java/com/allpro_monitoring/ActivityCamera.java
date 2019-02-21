package com.allpro_monitoring;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class ActivityCamera extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private TextureView mTextureView;
    private int mTotalRotation;
    //======================================TextureVIew listens to SurfaceListener=============================
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener()
    {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
        {
            setupCamera(width, height);
            connectCamera();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
        {
            configureTransform( width,  height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    //=========================================================================================================
    private CameraDevice mCameraDevice;
    //==================================================CameraDevice Listener=================================
    //Cameralistener




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback()
    {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(CameraDevice camera)
        {
            mCameraDevice = camera;
            if(mIsRecording)
            {
                try
                {
                    createVideoFileName();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();

            }
            else
            {
                startPreview();
                Toast.makeText(getApplicationContext(), "Camera connection made!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera)
        {
            camera.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(CameraDevice camera, int error)
        {
            camera.close();
            mCameraDevice = null;
        }
    };
    //============================================================================================================


    private CaptureRequest.Builder  mCaptureRequestBuilder;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;

    private ImageButton mRecordImageButton;
    private boolean mIsRecording = false;

    private File mVideoFolder;
    private String mVideoFileName;



    //static array to check  orientation of the phone
    private static SparseIntArray ORIENTATION = new SparseIntArray();
    static
    {
        ORIENTATION.append(Surface.ROTATION_0,0);
        ORIENTATION.append(Surface.ROTATION_90,90);
        ORIENTATION.append(Surface.ROTATION_180, 180);
        ORIENTATION.append(Surface.ROTATION_270,270);

    }
    //this class compares resolutions to get the best one
    private static class CompareSizeByArea implements Comparator<Size>
    {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs)
        {


            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    // MAIN ########################################################################################################
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        System.out.println("Within the onCreate for the CameraActivity class....");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_camera);
        createVideoFolder();

        mMediaRecorder = new MediaRecorder();

        mTextureView = findViewById(R.id.textureView);
        mRecordImageButton = findViewById(R.id.videoBtn);
        mRecordImageButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                if(mIsRecording)
                {
                    mIsRecording = false;
                    // mRecordImageButton.setImageResource(R.mipmap.custom_buttons1);
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    startPreview();
                }
                else
                {
                    checkWriteStoragePermission();

                }
            }
        });






    }
    //################################################################################################################
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    //===================================================checking if texture view is available=============================
    //this methods resumes camera
    protected void onResume()
    {
        super.onResume();

        startBackgroundThread();
        if(mTextureView.isAvailable())
        {
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        }
        else
        {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }
    //======================================================================================================================
    // this methods checks for camera permission is granted or not
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(),"Application won't runt wihtout camera services", Toast.LENGTH_SHORT ).show();
            }
        }
        if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mIsRecording = true;
                //mRecordImageButton.setImageResource(R.mipmap.custom_buttons);
                try
                {
                    createVideoFileName();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(this, "AllPro app needs to save video to run",Toast.LENGTH_SHORT).show();

            }
        }


    }

    //===========================================================================================
    //method to close camera on pausing video
    @Override
    protected void onPause()
    {
        closeCamera();

        stopBackgroundThread();
        super.onPause();

    }

    //==========================================================================================
    //this method makes the texture view full screen
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus)
        {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
        }
    }
    //==============================================================================================================
    //this method setups the camera including which camera to be used (front or back)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera(int width, int height)
    {
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);


                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation)
                {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mCameraId = cameraId;
                return;
            }
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }
    //==============================================================================================================
    //this method creates connection to camera hardware and it checks for the permission
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    //=================================================================================================================
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecord()
    {
        try
        {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface  = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);


            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback()
                    {
                        @Override
                        public void onConfigured(CameraCaptureSession session)
                        {
                            try
                            {
                                session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session)
                        {

                        }
                    }, null);


        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }
    //=================================================================================================================
    //this method starts camera preview to TextureView or Screen
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPreview()
    {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try
        {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session)
                {
                    try
                    {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(), null,mBackgroundHandler);

                    } catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session)
                {
                    Toast.makeText(getApplicationContext(), "Unable to setup Camera Preview", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }
    //=====================================================resets the camera if it is not working====================
    //reseting camera device method
    private void closeCamera()
    {
        if (mCameraDevice != null)
        {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }
    //===================================================================================================================
    //this method starts the background thread

    private void startBackgroundThread()
    {
        mBackgroundHandlerThread = new HandlerThread("MainActivity");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());


    }

    //=====================================================================================================================
    //this method stop the background thread
    private void stopBackgroundThread()
    {
        mBackgroundHandlerThread.quitSafely();
        try
        {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread= null;
            mBackgroundHandler=null;


        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    //======================================================================================================================
    //this method gets sensor orientation
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation )
    {
        int sensorOrientation  = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATION.get(deviceOrientation);

        return (sensorOrientation + deviceOrientation + 360) % 360;

    }
    //=====================================================================================================================
    // choosing optimal size for the screen to display camera preview
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseOptimalSize(Size[] choices, int width, int height)
    {
        Size bigEnough = null;
        int minAreaDiff = Integer.MAX_VALUE;
        for (Size option : choices) {
            int diff = (width*height)-(option.getWidth()*option.getHeight()) ;
            if (diff >=0 && diff < minAreaDiff &&
                    option.getWidth() <= width &&
                    option.getHeight() <= height) {
                minAreaDiff = diff;
                bigEnough = option;
            }
        }
        if (bigEnough != null) {
            return bigEnough;
        } else {
            Arrays.sort(choices,new CompareSizeByArea());
            return choices[0];
        }

    }
    //================================================================================================================
    //this method is called when the phone orientation is changed
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureTransform(int viewWidth, int viewHeight) {

        if (null == mTextureView || null == mPreviewSize ) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }
    //==================================================================================================
    // this method creates video folder for videos
    private void createVideoFolder()
    {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "AllProVideos");
        if(!mVideoFolder.exists())
        {
            mVideoFolder.mkdirs();
        }

    }
    //==================================================================================================
    // create video file name with date
    private File createVideoFileName () throws IOException
    {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend  = "VIDEO_" + timestamp + "_";
        File videoFile  = File.createTempFile(prepend, ".mp4", mVideoFolder);


        mVideoFileName = videoFile.getAbsolutePath();

        return videoFile;
    }
    //====================================================================================================
    //check for permission to write on the local storage
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkWriteStoragePermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
            {
                mIsRecording = true;
                //mRecordImageButton.setImageResource(R.mipmap.custom_buttons);
                try
                {
                    createVideoFileName();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();

            }
            else
            {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    Toast.makeText(this, "AllPro app needs to be able to save videos", Toast.LENGTH_SHORT).show();

                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT );
            }
        }
        else
        {
            mIsRecording = true;
            //mRecordImageButton.setImageResource(R.mipmap.custom_buttons);
            try
            {
                createVideoFileName();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
        }
    }
    //================================================================================================================================
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupMediaRecorder() throws IOException
    {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();

    }
}
