package per.general.customcamera;

import android.app.Activity;
import android.graphics.Point;
import android.util.Size;
import android.view.WindowManager;

public class Utils {
    static public Size getScreenSize(Activity acitivity) {
        Point point = new Point();
        WindowManager manager = (WindowManager)acitivity.getApplication().getSystemService(WindowManager.class);
        manager.getDefaultDisplay().getSize(point);
        return new Size(point.x, point.y);
    }
}
