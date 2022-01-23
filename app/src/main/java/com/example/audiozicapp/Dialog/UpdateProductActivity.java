package com.example.audiozicapp.Dialog;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.audiozicapp.DataCallBack;
import com.example.audiozicapp.Model.Product;
import com.example.audiozicapp.databinding.ActivityUpdateProductBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class UpdateProductActivity extends Dialog {

    Activity activity;
    Uri updatProfileImgeUri;
    FirebaseFirestore fireStoreDB;
    StorageReference storageRef;
    Product product;
    ActivityUpdateProductBinding binding;
    DataCallBack dataCallBack;

    public UpdateProductActivity(Activity context, Product productmodil, final DataCallBack dataCallBack) {
        super(context);
        activity = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        binding = ActivityUpdateProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        product = productmodil;
        this.dataCallBack = dataCallBack;
        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        binding.tvUserName.setText(product.name);
        binding.edPrice.setText(String.valueOf(product.price));

        if (product.photo != null) {
            Glide.with(activity)
                    .asBitmap()
                    .load(product.photo)
                    .into(binding.updatProfileImg);
        }
//            setPhotoUri(Uri.parse(studentModel.photo));

        binding.okBtn.setOnClickListener(view -> {

            String userNameStr = binding.tvUserName.getText().toString().trim();
            String pricePro = binding.edPrice.getText().toString().trim();

            // here check all fields that is not null on empty

            boolean hasError = false;
            if (userNameStr.isEmpty()) {
                binding.tvUserName.setError("Invalid Input");
                hasError = true;
            }
            if (pricePro.isEmpty()) {
                binding.edPrice.setError("Invalid Input");
                hasError = true;
            }
//            if (updatProfileImgeUri == null) {
//                Toast.makeText(activity, activity.getString(R.string.please_select_photo), Toast.LENGTH_SHORT).show();
//                hasError = true;
//            }
            if (hasError)
                return;

            product.name = userNameStr;
            product.price = (int)Double.parseDouble(pricePro);
            if (updatProfileImgeUri != null) {
                uploadPhoto(updatProfileImgeUri);
            } else {
                updateProductData();
            }
//            dataAccess.updateStudent(studentModel.name, studentModel.average, studentModel.photo);


        });

        binding.cancelBtn.setOnClickListener(view -> {

            dismiss();
        });

        binding.updatProfileImg.setOnClickListener(view -> {

            if (dataCallBack != null) {
                dataCallBack.Result(null, "pick_image", null);
            }
        });

        try {
            if (activity != null && !activity.isFinishing())
                show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void setPhotoUri(Uri photoUri) {

        updatProfileImgeUri = photoUri;

        Glide.with(activity)
                .asBitmap()
                .load(updatProfileImgeUri)
                .into(binding.updatProfileImg);
    }
    private void uploadPhoto(Uri photoUri) {

        StorageReference imgRef = storageRef.child("Images" + "/"
                + UUID.randomUUID().toString());


        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("aa", exception + "");
//                GlobalHelper.hideProgressDialog();
                // Handle unsuccessful uploads
                Toast.makeText(activity, "ddddd", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(taskSnapshot -> {

            imgRef.getDownloadUrl().addOnCompleteListener(task -> {

                product.photo = task.getResult().toString();
                Log.i("s", "Log photo " + product.photo);
                updateProductData();
//                System.out.println("Log uploaded url " + studentModel.getphoto());
            });


        });
    }



    private void updateProductData() {

        Map<String, Object> studentMap = new HashMap<>();
        studentMap.put("id", product.id);
        studentMap.put("name", product.name);
        studentMap.put("photo", product.photo);
        studentMap.put("price", product.price);


//        progressDialog.show();
        fireStoreDB.collection("Employee").document(product.id)
                .update(studentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    //                    progressDialog.d
                    @Override

                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Log DocumentSnapshot successfully deleted!");
                        dismiss();

                        if (dataCallBack != null) {
                            dataCallBack.Result(product, "", null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Log Error deleting document", e);

                        Toast.makeText(activity, "Fail edit student", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}