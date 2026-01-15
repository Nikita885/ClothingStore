package com.mobl.clothingmarket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.mobl.clothingmarket.R;

import android.widget.ImageView;

import java.io.File;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ImageView closeButton;
    private List<String> imagePaths;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String[] pathsArray = getIntent().getStringArrayExtra("image_paths");
        currentPosition = getIntent().getIntExtra("current_position", 0);
        
        if (pathsArray != null) {
            imagePaths = java.util.Arrays.asList(pathsArray);
        }

        viewPager = findViewById(R.id.view_pager);
        closeButton = findViewById(R.id.btn_close);

        if (imagePaths != null && !imagePaths.isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(imagePaths);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(currentPosition, false);
        }

        closeButton.setOnClickListener(v -> finish());
    }

    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<String> paths;

        ImagePagerAdapter(List<String> paths) {
            this.paths = paths;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_viewer, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.bind(paths.get(position));
        }

        @Override
        public int getItemCount() {
            return paths != null ? paths.size() : 0;
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
            }

            void bind(String imagePath) {
                File imageFile = new File(getFilesDir(), imagePath);
                if (imageFile.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                    
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    
                    int scale = 1;
                    if (options.outHeight > screenHeight || options.outWidth > screenWidth) {
                        int heightScale = Math.round((float) options.outHeight / screenHeight);
                        int widthScale = Math.round((float) options.outWidth / screenWidth);
                        scale = Math.max(heightScale, widthScale);
                    }
                    
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = scale;
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                    
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    } else {
                        imageView.setImageResource(R.drawable.product_image_placeholder);
                    }
                } else {
                    imageView.setImageResource(R.drawable.product_image_placeholder);
                }
            }
        }
    }
}

