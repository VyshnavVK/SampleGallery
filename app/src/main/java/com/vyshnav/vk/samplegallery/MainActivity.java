package com.vyshnav.vk.samplegallery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.appbar.AppBarLayout;
import com.vyshnav.vk.samplegallery.Adapter.Gallery;
import com.vyshnav.vk.samplegallery.Adapter.GalleryAdapter;
import com.vyshnav.vk.samplegallery.Fragment.ViewImagesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
List<Gallery> list;
GalleryAdapter galleryAdapter;
RecyclerView rv_main;
SwipeRefreshLayout swipe_layout;
String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
FrameLayout empty_frame;
Toolbar toolbar;
ImageView close,delete;
 AppBarLayout appbar;

 deleteFileForegroundService deleteFileForegroundService;
 boolean mBound = false;


private boolean selection_active = false ;


private ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        com.vyshnav.vk.samplegallery.deleteFileForegroundService.LocalBinder binder = (com.vyshnav.vk.samplegallery.deleteFileForegroundService.LocalBinder) iBinder;
        deleteFileForegroundService = binder.getService();
        if(deleteFileForegroundService.startForeground()){
            getContentResolver().notify();
            galleryAdapter.notifyDataSetChanged();
        }
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
   mBound = false;
    }
};



    private void initToolbar() {
        appbar = findViewById(R.id.appbar);
        close = findViewById(R.id.close);
        delete = findViewById(R.id.delete);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        close.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        appbar.setVisibility(View.GONE);
    }


    public void deleteAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete images!")
                .setMessage("Do you want to delete "+galleryAdapter.getSelectedItemCount()+" images?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                       deleteImages(GetSelectedImages());
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create()
                .show();
    }

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

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }


    public void deleteImages(List<String> listSelectedImagePath){
        if(listSelectedImagePath!=null){
            JSONArray jsonArray = new JSONArray();
            for(int i = 0;i< listSelectedImagePath.size();i++){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("path",listSelectedImagePath.get(i));
                    jsonArray.put(jsonObject);

                    if(i+1 == listSelectedImagePath.size()){
                        Intent intent = new Intent(this, deleteFileForegroundService.class);
                        intent.putExtra("paths",jsonArray.toString());
                        bindService(intent, connection, Context.BIND_AUTO_CREATE);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }
    }

    public void Notify(){
        if(galleryAdapter!=null)
        galleryAdapter.notifyDataSetChanged();
    }

    public void LoadFiles(){

       list.clear();
       final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
       final String orderBy = MediaStore.Images.Media.DATE_MODIFIED;

//Stores all the images from the gallery in Cursor
       Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
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

    public List<String> GetSelectedImages() {
        List<String> listSelectedItemPath = new ArrayList<>();
        if (list.size() > 0) {
            List<Integer> SelectedList;
            SelectedList = galleryAdapter.getSelectedItems();

            for (int i = 0; i < SelectedList.size(); i++) {
             listSelectedItemPath.add(list.get(SelectedList.get(i)).getImagePath());
             if(SelectedList.size()==i+1){
                 return listSelectedItemPath;
             }

            }

        } else {
           return null;
        }
return null;
}

    public void closeSelection(){
        galleryAdapter.clearSelections();
        appbar.setVisibility(View.GONE);
        selection_active = false;
    }

    private void toggleSelection(int position) {
        appbar.setVisibility(View.VISIBLE);
        galleryAdapter.toggleSelection(position);
        int count = galleryAdapter.getSelectedItemCount();
        if(count==0){
            closeSelection();
        }
        toolbar.setTitle(count + " item selected ");
        close.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);


    }

    private void enableActionMode(int position) {
        toggleSelection(position);
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

        initToolbar();

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
                if (!selection_active) {
                    Fragment fragment = new ViewImagesFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("imagePath", gallery.getImagePath());
                    fragment.setArguments(bundle);
                    FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                    tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    tx.replace(R.id.frame, fragment).addToBackStack("viewImage").commit();
                }else {
                        enableActionMode(pos);

                }
            }
            @Override
            public void onItemLongClick(View view, Gallery gallery, int pos) {
                enableActionMode(pos);
                selection_active = true;
            }
        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSelection();
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAlert();
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