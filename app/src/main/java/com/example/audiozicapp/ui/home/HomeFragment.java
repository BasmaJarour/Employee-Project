package com.example.audiozicapp.ui.home;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiozicapp.Adapter.ProductAdapter;
import com.example.audiozicapp.DataCallBack;
import com.example.audiozicapp.Dialog.UpdateProductActivity;
import com.example.audiozicapp.Model.Product;
import com.example.audiozicapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kcode.permissionslib.main.OnRequestPermissionsCallBack;
import com.kcode.permissionslib.main.PermissionCompat;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView rv;
   public ArrayList<Product> ProductList;
    ProductAdapter productAdapter;
    Product product;
    DataCallBack dataCallBack;
    UpdateProductActivity updateProductActivity;
    FirebaseFirestore fireStoreDB;
    ActivityResultLauncher<Intent> pickImageLauncher;


    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        fireStoreDB = FirebaseFirestore.getInstance();
        rv = root.findViewById(R.id.recyclerView);
        ProductList = new ArrayList<>();
        rv.setLayoutManager(new GridLayoutManager(getActivity(), 2));

//        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        fireStoreDB = FirebaseFirestore.getInstance();
        productAdapter=new ProductAdapter(getActivity(),ProductList,dataCallBack);



//        Log.e("nes", data.get(0).getName().toString());
        productAdapter = new ProductAdapter(getActivity(),ProductList, new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {
                product = (Product) obj;
                int position = (int) otherData;

                if (updateProductActivity == null) {
                    updateProductActivity = new UpdateProductActivity(getActivity(), product, new DataCallBack() {
                        @Override
                        public void Result(Object obj, String type, Object otherData) {

                            if (type.equals("pick_image")) {
                                checkPermission();
                            } else {
                                product = (Product) obj;
                                productAdapter.dataList.set(position, product);
                                productAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    updateProductActivity.setOnDismissListener(dialog -> updateProductActivity = null);
                }
            }





        });
        rv.setAdapter(productAdapter);
         getProductData();


        return root;
    }


    public void getProductData() {
        fireStoreDB.collection("Employee")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {


                if (task.isSuccessful()) {

                    ProductList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        Product product = document.toObject(Product.class);
                        ProductList.add(product);
                    }
                    productAdapter.dataList = ProductList;
                    productAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getActivity(), ("Fail to get data"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermission() {

        try {

            PermissionCompat.Builder builder = new PermissionCompat.Builder(getActivity());
            builder.addPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            builder.addPermissionRationale("Should allow permission");
            builder.addRequestPermissionsCallBack(new OnRequestPermissionsCallBack() {
                @Override
                public void onGrant() {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    pickImageLauncher.launch(Intent.createChooser(intent, ""));
                }

                @Override
                public void onDenied(String permission) {
                    Toast.makeText(getActivity(),("Some permission denied"), Toast.LENGTH_SHORT).show();
                }
            });
            builder.build().request();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}