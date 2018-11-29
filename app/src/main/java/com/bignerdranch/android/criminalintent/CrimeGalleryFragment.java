package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bignerdranch.android.criminalintent.utils.PictureUtils;

public class CrimeGalleryFragment extends DialogFragment {

    public static final String ARG_PHOTO = "photo";
    private ImageView photoGallery;

    public static CrimeGalleryFragment newGallery(String path){
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PHOTO, path);

        CrimeGalleryFragment fragment = new CrimeGalleryFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

       View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_gallery,null);
       photoGallery = v.findViewById(R.id.photoGallery);

       String path = (String) getArguments().getString(ARG_PHOTO);

       Bitmap bitmap = PictureUtils.getScaledBitmap(path,getActivity());
       photoGallery.setImageBitmap(bitmap);

       Dialog builder = new Dialog(getActivity());
       builder.setContentView(v);
       return builder;
    }
}
