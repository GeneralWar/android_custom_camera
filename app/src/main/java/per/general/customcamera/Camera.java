package per.general.customcamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Camera extends CameraDevice.StateCallback {
    static public abstract class Callback {
        public abstract void response(@Nullable Bitmap bitmap);
    }

    private CameraManager mCameraManager = null;
    private CameraDevice mCameraDevice = null;

    private SurfaceHolder mTarget = null;
    private Activity mOwner = null;

    private Callback mCallback = null;

    private CameraCaptureSession mCaptureSession = null;
    private ImageReader mImageReader = null;

    private Bitmap mResult = null;
    public Bitmap getResult() { return mResult; }

    public Camera(@NonNull Activity owner, @NonNull SurfaceHolder target, @NonNull Callback callback){
        mOwner = owner;
        mTarget = target;
        mCallback = callback;
    }

    @SuppressLint("MissingPermission")
    public boolean open(){
        Log.d("Camera", "try to open camera");
        if (null != mCameraDevice){
            this.captureToRender();
            return true;
        }

        WindowManager.LayoutParams params = mOwner.getWindow().getAttributes();
        mTarget.setFixedSize(params.width, params.height);

        CameraManager manager = mCameraManager = mOwner.getSystemService(CameraManager.class);
        try {
            // 使用后置摄像头
            String targetId = "";
            String[] deviceIds = manager.getCameraIdList();
            for (int i = 0; i < deviceIds.length; ++i) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(deviceIds[i]);
                if (CameraCharacteristics.LENS_FACING_BACK == characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    targetId = deviceIds[i];
                    break;
                }
            }
            manager.openCamera("" == targetId ? deviceIds[0] : targetId, this, null);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            mCallback.response(null);
        }
        return false;
    }

    public boolean capture(){
        if (null == mCaptureSession){
            return false;
        }
        try {
            mCaptureSession.stopRepeating();

            CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(mImageReader.getSurface());
            mCaptureSession.capture(requestBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            mCallback.response(null);
            return false;
        }
        return true;
    }

    private CameraCaptureSession.StateCallback mStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                requestBuilder.addTarget(mTarget.getSurface());
                session.setRepeatingRequest(requestBuilder.build(), mCaptureCallback, null);
                mCaptureSession = session;
            } catch (CameraAccessException e) {
                e.printStackTrace();
                mCallback.response(null);
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            mCallback.response(null);
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            mCallback.response(null);
        }
    };

    private ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Bitmap bitmap = null;
            try {
                Image image = reader.acquireNextImage();

                Image.Plane[] planes = image.getPlanes();
                ByteBuffer byteBuffer = planes[0].getBuffer();
                byte[] buffer = new byte[byteBuffer.limit()];
                byteBuffer.get(buffer);
                bitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(buffer, 0, buffer.length));
                bitmap = Bitmap.createScaledBitmap(bitmap, -bitmap.getWidth(), bitmap.getHeight(), false);

                image.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            mResult = bitmap;
            mCallback.response(bitmap);
        }
    };

    private void captureToRender() {
        if (null == mCameraDevice){
            return;
        }

        mResult = null;

        try {
            Log.d("Camera", "try to create capture session");
            List<Surface> surfaces = new ArrayList<Surface>();
            Surface surface = mTarget.getSurface();
            surfaces.add(surface);

            Rect rect = mTarget.getSurfaceFrame();
            ImageReader imageReader = mImageReader = ImageReader.newInstance(rect.width(), rect.height(), ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(mImageAvailableListener, null);
            surfaces.add(imageReader.getSurface());

            mCameraDevice.createCaptureSession(surfaces, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            mCallback.response(null);
        }
    }

    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        Log.d("Camera", "camera open");
        mCameraDevice = camera;
        this.captureToRender();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {

    }

    @Override
    public void onError(@NonNull CameraDevice camera, int error) {

    }
}
