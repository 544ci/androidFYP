package com.example.fyp_android;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.builder.GallerySettings;
import com.veinhorn.scrollgalleryview.loader.picasso.PicassoImageLoader;

import java.io.File;


public class Gallery extends AppCompatActivity {
    private ScrollGalleryView galleryView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        galleryView = ScrollGalleryView
                .from((ScrollGalleryView) findViewById(R.id.scroll_gallery_view))
                .settings(
                        GallerySettings
                                .from(getSupportFragmentManager())
                                .thumbnailSize(100)
                                .enableZoom(true)
                                .build()
                )
                .onImageClickListener(new ScrollGalleryView.OnImageClickListener() {
                    @Override
                    public void onClick(int position) {
                        Toast.makeText(Gallery.this, "image position = " + position, Toast.LENGTH_SHORT).show();
                    }
                })
                .onImageLongClickListener(new ScrollGalleryView.OnImageLongClickListener() {
                    @Override
                    public void onClick(int position) {
                        Toast.makeText(Gallery.this, "image position = " + position, Toast.LENGTH_SHORT).show();
                    }
                })
                .onPageChangeListener(new CustomOnPageListener())
//                .add(image("http://povodu.ru/wp-content/uploads/2016/04/pochemu-korabl-derzitsa-na-vode.jpg"))
//                .add(image(
//                        "https://i.pinimg.com/originals/1b/d3/f0/1bd3f0e146da86f9c504e89a0b7e1403.jpg",
//                        "Old Ship"
//                ))
                .build();

//        galleryView.addMedia(MediaInfo.mediaLoader(
//                new PicassoImageLoader("https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/USRC_Salmon_P_Chase_-_LoC_4a25817u.jpg/1200px-USRC_Salmon_P_Chase_-_LoC_4a25817u.jpg"),
//                "The word barque entered English via French, which in turn came from the Latin barca by way of Occitan, Catalan, Spanish or Italian."
//        ));

//        galleryView.addMedia(MediaInfo.mediaLoader(
//                new PicassoImageLoader("file://"+ new File(Environment.getExternalStorageDirectory() + "/intruders").listFiles()[0].getPath()),
//                "The word barque entered English via French, which in turn came from the Latin barca by way of Occitan, Catalan, Spanish or Italian."
//        ));
        File[] images = new File(Environment.getExternalStorageDirectory() + "/intruders").listFiles();
        for (File image : images) {
            galleryView.addMedia(MediaInfo.mediaLoader(new PicassoImageLoader("file://" + image.getPath())));
        }


    }

    private class CustomOnPageListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Log.i("ASASASASASASAS", "page selected #" + position);
        }


    }
}