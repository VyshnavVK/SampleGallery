package com.vyshnav.vk.samplegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;

import com.vyshnav.vk.samplegallery.Adapter.Gallery;
import com.vyshnav.vk.samplegallery.Adapter.GalleryAdapter;
import com.vyshnav.vk.samplegallery.Fragment.ViewImagesFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
List<Gallery> list;
GalleryAdapter galleryAdapter;
RecyclerView rv_main;
SwipeRefreshLayout swipe_layout;
String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};
FrameLayout empty_frame;
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



   public void LoadFiles(){

       list.clear();
       final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
       final String orderBy = MediaStore.Images.Media._ID;
//Stores all the images from the gallery in Cursor
       Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
//Total number of images


//Create an array to store path to all the images
       assert cursor != null;
       String[] arrPath = new String[cursor.getCount()];

       for (int i = 0; i < cursor.getCount(); i++) {
           cursor.moveToPosition(i);
           int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
           //Store the path of the image
           arrPath[i]= cursor.getString(dataColumnIndex);
           list.add(new Gallery(arrPath[i]));
           if(cursor.getCount()==i+1){
               if(list.size()>0){
                   empty_frame.setVisibility(View.INVISIBLE);
               }
              galleryAdapter.notifyDataSetChanged();
              swipe_layout.setRefreshing(false);
           }
       }
// The cursor should be freed up after use with close()
       cursor.close();



   }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(list,this);
        swipe_layout = findViewById(R.id.swipe_layout);
        empty_frame = findViewById(R.id.empty_frame);
        empty_frame.setVisibility(View.VISIBLE);
        rv_main = findViewById(R.id.rv_main);
        rv_main.setLayoutManager(new GridLayoutManager(this,3));
        rv_main.setAdapter(galleryAdapter);

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, 100);
        }else {
            LoadFiles();
        }

        galleryAdapter.setOnClickListener(new GalleryAdapter.OnClickListener() {
            @Override
            public void onGalleryItemClick(View view, Gallery gallery, int pos) {
                Fragment fragment = new ViewImagesFragment();

                Bundle bundle = new Bundle();
                bundle.putString("imagePath",gallery.getImagePath());
                fragment.setArguments(bundle);
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                tx.replace(R.id.frame, fragment).addToBackStack("viewImage").commit();
            }
        });


        swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_layout.setRefreshing(true);
                if(!hasPermissions(MainActivity.this, PERMISSIONS)){
                    swipe_layout.setRefreshing(false);
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 100);
                }else {
                    LoadFiles();
                }
            }
        });
    }
}