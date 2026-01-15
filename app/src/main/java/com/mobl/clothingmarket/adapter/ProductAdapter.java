package com.mobl.clothingmarket.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.mobl.clothingmarket.R;
import com.mobl.clothingmarket.model.Product;

import java.io.File;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private OnItemClickListener listener;
    private boolean isLoggedIn;

    public interface OnItemClickListener {
        void onItemClick(Product product);
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnItemClickListener listener, boolean isLoggedIn) {
        this.products = products;
        this.listener = listener;
        this.isLoggedIn = isLoggedIn;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView priceTextView;
        private TextView descriptionTextView;
        private ViewPager2 productImagesPager;
        private Button addToCartButton;
        private View imageContainer;
        private View itemView;
        private Product currentProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            descriptionTextView = itemView.findViewById(R.id.product_description);
            productImagesPager = itemView.findViewById(R.id.product_images_pager);
            addToCartButton = itemView.findViewById(R.id.btn_add_to_cart);
            imageContainer = itemView.findViewById(R.id.image_container);

            productImagesPager.setUserInputEnabled(true);
            productImagesPager.setOffscreenPageLimit(1);
            
            productImagesPager.setNestedScrollingEnabled(false);
            
            final float[] startX = new float[1];
            final float[] startY = new float[1];
            final boolean[] isScrolling = new boolean[1];
            final long[] touchDownTime = new long[1];
            
            productImagesPager.setOnTouchListener((v, event) -> {
                ViewGroup parent = (ViewGroup) v.getParent();
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startX[0] = event.getX();
                        startY[0] = event.getY();
                        touchDownTime[0] = System.currentTimeMillis();
                        isScrolling[0] = false;
                        parent.requestDisallowInterceptTouchEvent(true);
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getX() - startX[0]);
                        float deltaY = Math.abs(event.getY() - startY[0]);
                        if (deltaX > 10 || deltaY > 10) {
                            isScrolling[0] = true;
                        }
                        if (deltaX > deltaY && deltaX > 10) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        } else if (deltaY > deltaX && deltaY > 10) {
                            parent.requestDisallowInterceptTouchEvent(false);
                            isScrolling[0] = true;
                        }
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                        long touchDuration = System.currentTimeMillis() - touchDownTime[0];
                        float finalDeltaX = Math.abs(event.getX() - startX[0]);
                        float finalDeltaY = Math.abs(event.getY() - startY[0]);
                        
                        if (!isScrolling[0] && finalDeltaX < 10 && finalDeltaY < 10 && touchDuration < 300) {
                            parent.requestDisallowInterceptTouchEvent(false);
                            imageContainer.performClick();
                            return true;
                        }
                        parent.requestDisallowInterceptTouchEvent(false);
                        break;
                    case android.view.MotionEvent.ACTION_CANCEL:
                        parent.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            });

            imageContainer.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Product product = products.get(getAdapterPosition());
                    if (product != null) {
                        listener.onItemClick(product);
                    }
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Product product = products.get(getAdapterPosition());
                    if (product != null) {
                        listener.onItemClick(product);
                    }
                }
            });

            addToCartButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Product product = products.get(getAdapterPosition());
                    if (product != null) {
                        listener.onAddToCartClick(product);
                    }
                }
            });
        }

        public void bind(Product product) {
            currentProduct = product;
            nameTextView.setText(product.getName());
            priceTextView.setText(String.format("%.2f ₽", product.getPrice()));
            descriptionTextView.setText(product.getDescription());
            addToCartButton.setEnabled(isLoggedIn);
            
            loadProductImages(product);
        }
        
        private void loadProductImages(Product product) {
            List<String> imageUrls = product.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                ProductImagePagerAdapter adapter = new ProductImagePagerAdapter(imageUrls, product);
                productImagesPager.setAdapter(adapter);
            } else {
                ProductImagePagerAdapter adapter = new ProductImagePagerAdapter(java.util.Collections.emptyList(), product);
                productImagesPager.setAdapter(adapter);
            }
        }

        private class ProductImagePagerAdapter extends RecyclerView.Adapter<ProductImagePagerAdapter.ImageViewHolder> {
            private List<String> imageUrls;
            private Product product;

            ProductImagePagerAdapter(List<String> imageUrls, Product product) {
                this.imageUrls = imageUrls;
                this.product = product;
            }

            @NonNull
            @Override
            public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_product_image, parent, false);
                return new ImageViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
                if (imageUrls != null && position < imageUrls.size()) {
                    holder.bind(imageUrls.get(position), position);
                } else {
                    holder.bind(null, position);
                }
            }

            @Override
            public int getItemCount() {
                return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.size() : 1;
            }

            class ImageViewHolder extends RecyclerView.ViewHolder {
                private ImageView imageView;

            ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                imageView.setClickable(false);
                imageView.setFocusable(false);
            }

            void bind(String imageUrl, int position) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        File imageFile = new File(itemView.getContext().getFilesDir(), imageUrl);
                        if (imageFile.exists()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                            
                            int maxWidth = 800;
                            int maxHeight = 600;
                            int scale = 1;
                            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                                int heightScale = Math.round((float) options.outHeight / maxHeight);
                                int widthScale = Math.round((float) options.outWidth / maxWidth);
                                scale = Math.max(heightScale, widthScale);
                            }
                            
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = scale;
                            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                            
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                                imageView.setBackgroundResource(0);
                            } else {
                                imageView.setImageResource(0);
                                imageView.setBackgroundResource(R.drawable.product_image_placeholder);
                            }
                        } else {
                            imageView.setImageResource(0);
                            imageView.setBackgroundResource(R.drawable.product_image_placeholder);
                        }
                    } else {
                        imageView.setImageResource(0);
                        imageView.setBackgroundResource(R.drawable.product_image_placeholder);
                    }
                }
            }
        }
    }
}

