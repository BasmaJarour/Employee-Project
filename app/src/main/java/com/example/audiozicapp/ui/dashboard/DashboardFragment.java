package com.example.audiozicapp.ui.dashboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.audiozicapp.Model.Product;
import com.example.audiozicapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DashboardFragment extends Fragment {
    Button addbtn, getbtn;
    TextView productName, productPrice ,proDetails;
    ImageView productImag;

    Uri userPhotoUri;
    Product product;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    FirebaseFirestore fireStoreDB;
    StorageReference storageRef;
    private DashboardViewModel dashboardViewModel;
    private int requestCode;
    private String[] permissions;
    private int[] grantResults;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        product = new Product();

        productName = root.findViewById(R.id.name_item);
        productPrice = root.findViewById(R.id.item_price);
        proDetails = root.findViewById(R.id.text_details);
        addbtn = root.findViewById(R.id.but_add);
        productImag = root.findViewById(R.id.img_shop);


        addbtn.setOnClickListener(v -> {
            checkData();
        });




        productImag.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permission, PERMISSION_CODE);
                } else {
                    pickImageFromGallery();
                }
            } else {
                pickImageFromGallery();
            }
        });

        addbtn.setOnClickListener(v -> {
            checkData();
        });





        return root;
    }




    private void checkData() {
        String name = productName.getText().toString();
        String price = productPrice.getText().toString();
        String details = proDetails.getText().toString();

        boolean hasError = false;


        if (name.isEmpty()) {
            productName.setError("invalid_input");
            hasError = true;
        }
        if (price.isEmpty()) {
            productPrice.setError("invalid_input");
            hasError = true;
        }
        if (userPhotoUri == null) {
            hasError = true;
        }
        if (details.isEmpty()) {
            proDetails.setError("invalid_input");
            hasError = true;
        }
        if (hasError)
            return;

        product.name = name;
        product.price = (int)Double.parseDouble(price);
//        uploadPhoto(userPhotoUri);
        product.details = details;
        addEmployeeToFirebase(product.photo);



    }

    private void uploadPhoto(Uri photoUri) {

        StorageReference imgRef = storageRef.child("Images" + "/"
                + UUID.randomUUID().toString());

        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("aa", exception + "");

                Toast.makeText(getActivity(), "No", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(taskSnapshot -> {

            imgRef.getDownloadUrl().addOnCompleteListener(task -> {

                product.photo = task.getResult().toString();
                Log.e("s",product.photo);
//                System.out.println("Log uploaded url " + studentModel.getphoto());
//                addProductsToFirebase();
            });


        });
    }


    private void addEmployeeToFirebase(String PhotoURL) {

        String userId = fireStoreDB.collection("Employee").document().getId(); // this is auto genrat

        Map<String, Object> productModelMap = new HashMap<>();
        productModelMap.put("id", userId);
        productModelMap.put("name", product.name);
        productModelMap.put("JopNumber", product.price);
        productModelMap.put("photo", product.photo);
        productModelMap.put("details", product.details);

        fireStoreDB.collection("Employee").document(userId).set(productModelMap, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Employee Added", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(getActivity(), "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {

            userPhotoUri = data.getData();
            Picasso.get().load(userPhotoUri).placeholder(R.drawable.ic_profile).into(productImag);
            uploadPhoto(userPhotoUri);

//            productImag.setImageURI(userPhotoUri);
        }
    }
}