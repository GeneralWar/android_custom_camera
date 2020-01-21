package per.general.customcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class CameraActivity extends Activity implements View.OnClickListener {
    static final int PERMISSION_TAKE = 1;

    private Button mTakeButton = null;
    private TextView mCheckButton = null;
    private TextView mCancelButton = null;
    private SurfaceView mSurfaceView = null;

    private Camera mCamera = null;
    private CameraActivity mSelf = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelf = this;
        if (this.checkCameraPermission()) {
            this.takePhoto();
        }
    }

    private void initializeContentAndCamera() {
        Log.d("CameraActivity", "initializeContentAndCamera");
        Point screenSize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(screenSize);

        WindowManager.LayoutParams windowParams = this.getWindow().getAttributes();
        windowParams.height = screenSize.y;
        windowParams.width = (int)(windowParams.height * 1280.0 / 720);
        windowParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE; // SYSTEM_UI_FLAG_IMMERSIVE为沉浸式体验，点击也不显示导航栏

        this.setContentView(R.layout.activity_camera);

        mTakeButton = (Button)this.findViewById(R.id.camera_button_take);
        mTakeButton.setOnClickListener(this);
        mTakeButton.setVisibility(View.INVISIBLE);
        mCheckButton = (TextView)this.findViewById(R.id.camera_button_check);
        mCheckButton.setVisibility(View.INVISIBLE);
        mCheckButton.setOnClickListener(this);
        mCancelButton = (TextView)this.findViewById(R.id.camera_button_cancel);
        mCancelButton.setVisibility(View.INVISIBLE);
        mCancelButton.setOnClickListener(this);

        Log.d("CameraActivity", "initialize surface view");
        mSurfaceView = (SurfaceView)this.findViewById(R.id.camera_surface);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(mSurfaceHolderCallback);
    }

    private Camera.Callback mCameraCallback = new Camera.Callback() {
        @Override
        public void response(Bitmap bitmap) {
            if (null == bitmap) {
                // 理论上的拍照失败
                mTakeButton.setVisibility(View.VISIBLE);
            }
            else {
                mCheckButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.VISIBLE);
            }
        }
    };

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d("CameraActivity", "surface view created");
            mTakeButton.setVisibility(View.VISIBLE);

            mCamera = new Camera(mSelf, holder, mCameraCallback);
            mCamera.open();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    @Override
    public void onClick(final View v) {
        if (v == mTakeButton && null != mCamera) {
            if (mCamera.capture()) {
                mTakeButton.setVisibility(View.INVISIBLE);
            }
        }
        if (v == mCancelButton && null != mCamera) {
            mCamera.open();
            mTakeButton.setVisibility(View.VISIBLE);
            mCheckButton.setVisibility(View.INVISIBLE);
            mCancelButton.setVisibility(View.INVISIBLE);
        }
        if (v == mCheckButton && null != mCamera) {
            try {
//                Bitmap bitmap = mCamera.getResult();
//                Intent intent = new Intent();
//                intent.putExtra("data", bitmap);
//                this.setResult(0, intent);
                this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCameraPermission() {
        int oldStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (PackageManager.PERMISSION_GRANTED == oldStatus) {
            return true;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_TAKE);
        return false;
    }

    private void takePhoto() {
        this.initializeContentAndCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; ++i) {
            switch (requestCode) {
                case PERMISSION_TAKE:
                    this.takePhoto();
                    break;
            }
        }
    }
}