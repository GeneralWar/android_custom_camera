# android_custom_camera

A custom camera for Android with CameraManager and CameraDevice, since *android.hardware.Camera* was deprecated at API 21 (Android 5.0 Lollipop).  

I offer one Activity and one Class to do this.  

## CameraActivity(Activity)

```
public class CameraActivity extends Activity implements View.OnClickListener {
    static public void show(Acitivity parent);

    static public void show(Activity parent, int preferredWidth, int preferredHeight);

    static public Bitmap decodeBitmap(Intent data);

    static public void Clear();
}
```

Call ```CameraAcitivity.show``` to show a Camera Activity, or you can use any other acitivity instead.  

Call ```CameraActivity.decodeBitmap``` to get the picture captured by this activity. Because intent has a limit for data size, so we cached the pictures and return a key as result.  

If you do not need pictures captured by this activity any more, you should call ```CameraActivity.clear``` to release the cache.

## Camera(Class)

```
public class Camera extends CameraDevice.StateCallback {
    public Camera(@NonNull Activity owner, @NonNull SurfaceHolder target, @NonNull Callback callback);

    public boolean open(int preferredWidth, int preferredHeight);

    public boolean capture();
}
```

### Create new instance  
Create a new camera by calling ```new Camera(owner, target ,callback)```.
- @param owner: The activity who create this camera.
- @param target: The SurfaceHolder of SurfaceView which to display capture session.
-  @param callback: The callback to response captured bitmap. Since CameraManager and CameraDevice work as async tasks, so we can only get captured bitmap via callback.

### Other functions
Call ```Camera.open``` to open a camera device and start a repeating capture session.
- @param preferredWidth: It may be 0.
- @param preferredHeight: It may be 0.
- @return True as success, false as failure.
  
Call ```Camera.capture``` to stop current repeating capture session and request a capture (try to capture a bitmap).
- @return True as success, false as failure.

## Caution

I ***do not*** cut image to fit the border.