package edu.temple.androidstorage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class PictureSave extends Activity {

    LinearLayout imageLayout; // Main layout defined in our xml file

    Uri imageUri; // Store the URI for an image returned by the camera intent

    private static int TAKE_PICTURE_REQUEST_CODE = 11111;

    private static String directoryName = "androidStorage";
    File storageDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_save);

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);

        TextView textView1 = new TextView(this);
        textView1.setText("Your List");
        imageLayout.addView(textView1); // Add a heading (of sorts) to our list

        storageDirectory = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + Environment.DIRECTORY_PICTURES
                + File.separator
                + directoryName);

        File[] files = storageDirectory.listFiles();
        for (File file : files) {
            addPicture(Uri.fromFile(file));
        }

    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(storageDirectory
                , String.valueOf(System.currentTimeMillis())); // Temporary file name
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE); // Launches an external activity/application to take a picture
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_pic) {
            takePicture();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE_REQUEST_CODE) {

            // Image is too large. Resize to a reasonable size
            resizeBitmap(imageUri, 600);
            addPicture(imageUri);
        }
    }

    private void addPicture(Uri uri){
        ImageView imageView = new ImageView(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(600, 600); // Set our image view to thumbnail size

        imageView.setLayoutParams(lp);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        ll.addView(imageView);

        TextView text = new TextView(this);
        text.setText(Calendar.getInstance().getTime().toString());

        ll.addView(text);
        try {
            Picasso.with(this).load(uri).into(imageView);

            imageLayout.addView(ll);
        } catch (Exception e) {
            Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT)
                    .show();
            Log.e("Camera", e.toString());
            e.printStackTrace();
        }
    }

    public void resizeBitmap(Uri imageUri, int maxSize) {
        Bitmap image = BitmapFactory.decodeFile(imageUri.getPath());
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        Bitmap bm = Bitmap.createScaledBitmap(image, width, height, true);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageUri.getPath());
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
