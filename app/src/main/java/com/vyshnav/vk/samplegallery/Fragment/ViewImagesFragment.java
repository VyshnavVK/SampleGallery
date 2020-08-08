package com.vyshnav.vk.samplegallery.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.vyshnav.vk.samplegallery.R;

import java.io.File;
import java.util.Objects;

public class ViewImagesFragment extends Fragment {

    String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};


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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.view_image_fragment,container,false);
        ImageView image = view.findViewById(R.id.image);
        TextView name  = view.findViewById(R.id.name);
        Bundle bundle = getArguments();
       if(bundle!=null){
           if(!hasPermissions(requireContext(), PERMISSIONS)){
               ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, 100);
           }else {
               File file = new File(Objects.requireNonNull(bundle.getString("imagePath")));
               if(file.exists()){
                   Glide.with(requireActivity()).load(file).into(image);
                   name.setText(file.getName());
               }else {
                   requireActivity().getSupportFragmentManager().popBackStack();
               }
           }
       }

        return view;
    }
}
