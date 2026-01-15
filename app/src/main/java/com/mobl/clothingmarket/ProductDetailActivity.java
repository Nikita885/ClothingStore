package com.mobl.clothingmarket;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.Product;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1001;
    
    private TextView nameTextView;
    private TextView priceTextView;
    private TextView statusTextView;
    private TextView contentTextView;
    private ViewPager2 productImagesPager;
    private Button characteristicsButton;
    private Button descriptionButton;
    private Button addToCartButton;
    private ImageButton backButton;
    private ImageButton settingsButton;
    private android.widget.LinearLayout quantityControls;
    private com.google.android.material.button.MaterialButton decreaseButton;
    private com.google.android.material.button.MaterialButton increaseButton;
    private TextView quantityTextView;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;
    private Product product;
    private boolean showingCharacteristics = true;
    private com.mobl.clothingmarket.model.CartItem cartItem;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        database = AppDatabase.getInstance(this);
        prefsHelper = new SharedPreferencesHelper(this);

        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        product = database.productDao().getProductById(productId);
        if (product == null) {
            finish();
            return;
        }

        nameTextView = findViewById(R.id.detail_product_name);
        priceTextView = findViewById(R.id.detail_product_price);
        statusTextView = findViewById(R.id.product_status);
        contentTextView = findViewById(R.id.content_text);
        productImagesPager = findViewById(R.id.product_images_pager);
        characteristicsButton = findViewById(R.id.btn_characteristics);
        descriptionButton = findViewById(R.id.btn_description);
        addToCartButton = findViewById(R.id.btn_detail_add_to_cart);
        backButton = findViewById(R.id.btn_back);
        settingsButton = findViewById(R.id.btn_settings);
        quantityControls = findViewById(R.id.quantity_controls);
        decreaseButton = findViewById(R.id.btn_decrease_quantity);
        increaseButton = findViewById(R.id.btn_increase_quantity);
        quantityTextView = findViewById(R.id.tv_quantity);
        
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        saveProductImage(selectedImageUri);
                    }
                }
            }
        );

        backButton.setOnClickListener(v -> finish());

        if (prefsHelper.isAdmin()) {
            settingsButton.setVisibility(View.VISIBLE);
            settingsButton.setOnClickListener(v -> showAdminMenu());
        }

        displayProduct();
        setupButtons();
        setupCartControls();
        
        addToCartButton.setOnClickListener(v -> {
            if (prefsHelper.isLoggedIn()) {
                addToCart();
                updateCartControls();
            } else {
                Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show();
            }
        });

        if (!prefsHelper.isLoggedIn()) {
            addToCartButton.setVisibility(View.GONE);
            quantityControls.setVisibility(View.GONE);
        } else {
            updateCartControls();
        }
    }

    private void displayProduct() {
        nameTextView.setText(product.getName());
        
        priceTextView.setText(String.format("%.2f ₽", product.getPrice()));
        
        if (product.isInStock()) {
            statusTextView.setText(String.format(getString(R.string.in_stock_format), product.getStockQuantity()));
        } else {
            statusTextView.setText(getString(R.string.out_of_stock));
        }
        
        loadProductImage();
        
        showCharacteristics();
    }
    
    private void loadProductImage() {
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ProductImagePagerAdapter adapter = new ProductImagePagerAdapter(imageUrls);
            productImagesPager.setAdapter(adapter);
        } else {
            ProductImagePagerAdapter adapter = new ProductImagePagerAdapter(java.util.Collections.emptyList());
            productImagesPager.setAdapter(adapter);
        }
    }

    private void setupButtons() {
        characteristicsButton.setOnClickListener(v -> {
            if (!showingCharacteristics) {
                showCharacteristics();
            }
        });

        descriptionButton.setOnClickListener(v -> {
            if (showingCharacteristics) {
                showDescription();
            }
        });
    }

    private void showCharacteristics() {
        showingCharacteristics = true;
        
        StringBuilder characteristics = new StringBuilder();
        characteristics.append("Категория: ").append(product.getCategory()).append("\n\n");
        characteristics.append("Размер: ").append(product.getSize()).append("\n\n");
        characteristics.append("Цвет: ").append(product.getColor());
        
        contentTextView.setText(characteristics.toString());
        
        characteristicsButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent, getTheme()));
        characteristicsButton.setTextColor(getResources().getColor(R.color.white, getTheme()));
        
        descriptionButton.setBackgroundTintList(null);
        descriptionButton.setTextColor(getResources().getColor(R.color.accent, getTheme()));
    }

    private void showDescription() {
        showingCharacteristics = false;
        
        contentTextView.setText(product.getDescription());
        
        descriptionButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent, getTheme()));
        descriptionButton.setTextColor(getResources().getColor(R.color.white, getTheme()));
        
        characteristicsButton.setBackgroundTintList(null);
        characteristicsButton.setTextColor(getResources().getColor(R.color.accent, getTheme()));
    }

    private void setupCartControls() {
        decreaseButton.setOnClickListener(v -> {
            if (cartItem != null && cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                database.cartDao().updateCartItem(cartItem);
                updateCartControls();
            } else if (cartItem != null && cartItem.getQuantity() == 1) {
                database.cartDao().deleteCartItem(cartItem);
                cartItem = null;
                updateCartControls();
            }
        });

        increaseButton.setOnClickListener(v -> {
            if (cartItem != null) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                database.cartDao().updateCartItem(cartItem);
                updateCartControls();
            } else {
                addToCart();
                updateCartControls();
            }
        });
    }

    private void updateCartControls() {
        if (!prefsHelper.isLoggedIn()) {
            addToCartButton.setVisibility(View.GONE);
            quantityControls.setVisibility(View.GONE);
            return;
        }

        int userId = prefsHelper.getUserId();
        if (userId != -1) {
            cartItem = database.cartDao().getCartItem(userId, product.getId());
            
            if (cartItem != null && cartItem.getQuantity() > 0) {
                addToCartButton.setVisibility(View.GONE);
                quantityControls.setVisibility(View.VISIBLE);
                quantityTextView.setText(String.valueOf(cartItem.getQuantity()));
            } else {
                addToCartButton.setVisibility(View.VISIBLE);
                quantityControls.setVisibility(View.GONE);
                cartItem = null;
            }
        }
    }

    private void addToCart() {
        int userId = prefsHelper.getUserId();
        if (userId != -1) {
            com.mobl.clothingmarket.model.CartItem existingItem = 
                database.cartDao().getCartItem(userId, product.getId());
            
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                database.cartDao().updateCartItem(existingItem);
                cartItem = existingItem;
            } else {
                com.mobl.clothingmarket.model.CartItem newItem = 
                    new com.mobl.clothingmarket.model.CartItem(userId, product.getId(), 1);
                database.cartDao().insertCartItem(newItem);
                cartItem = newItem;
            }
        }
    }

    private void showAdminMenu() {
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, settingsButton);
        popupMenu.getMenu().add(0, 1, 0, "Редактировать товар");
        popupMenu.getMenu().add(0, 2, 0, "Загрузить фото");
        popupMenu.getMenu().add(0, 4, 0, "Удалить фото");
        popupMenu.getMenu().add(0, 3, 0, "Удалить товар");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openEditProductDialog();
                return true;
            } else if (item.getItemId() == 2) {
                pickProductImage();
                return true;
            } else if (item.getItemId() == 4) {
                deleteCurrentImage();
                return true;
            } else if (item.getItemId() == 3) {
                deleteProduct();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void openEditProductDialog() {
        android.content.Intent intent = new android.content.Intent(this, EditProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void pickProductImage() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_IMAGE_PICK);
                return;
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_PICK);
                return;
            }
        }
        
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_PICK) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Необходимо разрешение для доступа к изображениям", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void saveProductImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            
            int maxWidth = 1920;
            int maxHeight = 1080;
            int scale = 1;
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                int heightScale = Math.round((float) options.outHeight / maxHeight);
                int widthScale = Math.round((float) options.outWidth / maxWidth);
                scale = Math.max(heightScale, widthScale);
            }
            
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            
            if (bitmap == null) {
                Toast.makeText(this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                float scaleFactor = Math.min((float) maxWidth / bitmap.getWidth(), 
                                            (float) maxHeight / bitmap.getHeight());
                int newWidth = Math.round(bitmap.getWidth() * scaleFactor);
                int newHeight = Math.round(bitmap.getHeight() * scaleFactor);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            File imagesDir = new File(getFilesDir(), "product_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            long timestamp = System.currentTimeMillis();
            String imageFileName = "product_" + product.getId() + "_" + timestamp + ".jpg";
            File imageFile = new File(imagesDir, imageFileName);
            
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
                outputStream.close();
            
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            
            Bitmap displayBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            
            String relativePath = "product_images/" + imageFileName;
            List<String> imageUrls = product.getImageUrls();
            if (!imageUrls.contains(relativePath)) {
                imageUrls.add(relativePath);
                product.setImageUrls(imageUrls);
                database.productDao().updateProduct(product);
            }
            
            loadProductImage();
            
            Toast.makeText(this, "Изображение успешно загружено", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCurrentImage() {
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            Toast.makeText(this, "Нет изображений для удаления", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int currentPosition = productImagesPager.getCurrentItem();
        if (currentPosition < 0 || currentPosition >= imageUrls.size()) {
            Toast.makeText(this, "Ошибка: неверная позиция изображения", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удаление фото")
                .setMessage("Вы уверены, что хотите удалить это фото?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    String imagePath = imageUrls.get(currentPosition);
                    
                    File imageFile = new File(getFilesDir(), imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    
                    imageUrls.remove(currentPosition);
                    product.setImageUrls(imageUrls);
                    database.productDao().updateProduct(product);
                    
                    loadProductImage();
                    
                    Toast.makeText(this, "Фото удалено", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteProduct() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удаление товара")
                .setMessage("Вы уверены, что хотите удалить этот товар?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    database.productDao().deleteProduct(product);
                    database.cartDao().deleteCartItemsByProductId(product.getId());
                    Toast.makeText(this, "Товар удален", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (product != null) {
            Product updatedProduct = database.productDao().getProductById(product.getId());
            if (updatedProduct != null) {
                product = updatedProduct;
                displayProduct();
            }
        }
        if (prefsHelper.isLoggedIn()) {
            updateCartControls();
        }
    }

    private class ProductImagePagerAdapter extends RecyclerView.Adapter<ProductImagePagerAdapter.ImageViewHolder> {
        private List<String> imageUrls;

        ProductImagePagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
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
            }

            void bind(String imageUrl, int position) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    File imageFile = new File(getFilesDir(), imageUrl);
                    if (imageFile.exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                        
                        int maxWidth = 1920;
                        int maxHeight = 1080;
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
                
                imageView.setOnClickListener(v -> {
                    List<String> imageUrls = product.getImageUrls();
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        Intent intent = new Intent(ProductDetailActivity.this, ImageViewerActivity.class);
                        intent.putExtra("image_paths", imageUrls.toArray(new String[0]));
                        intent.putExtra("current_position", position);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
