package per.general.customcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.findViewById(R.id.button_camera).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (this.findViewById(R.id.button_camera) == v){
//            CameraActivity.show(this);
            Size size = Utils.getScreenSize(this);
            CameraActivity.show(this, size.getWidth(), size.getHeight(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView imageView = (ImageView)this.findViewById(R.id.image_view);
        Bitmap bitmap = CameraActivity.decodeBitmap(data);
        if (null != bitmap) {
            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, -bitmap.getWidth(), bitmap.getHeight(), false));
        }
    }
}
