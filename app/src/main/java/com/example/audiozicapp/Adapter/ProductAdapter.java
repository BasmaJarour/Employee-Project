package com.example.audiozicapp.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.audiozicapp.DataCallBack;
import com.example.audiozicapp.Model.Product;
import com.example.audiozicapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    Activity context;
    public ArrayList<Product> dataList;
    DataCallBack dataCallBack;
    FirebaseFirestore fireStoreDB = FirebaseFirestore.getInstance();

    public ProductAdapter(Activity context, ArrayList<Product> dataList,DataCallBack dataCallBack) {
        this.context = context;
        this.dataList = dataList;
        this.dataCallBack = dataCallBack;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.list_product, parent, false);
        return new ViewHolder(root) ;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = dataList.get(position);

        holder.txName.setText(String.valueOf(product.name));
        holder.txPrice.setText(String.valueOf(product.price));

        Glide.with(context).asBitmap().load(product.photo).placeholder(R.drawable.ic_product_image).into(holder.img);

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView idTv;
        TextView txName;
        TextView txPrice;
        ImageView img;
        Button okBtn;
        Button cancelBtn;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            idTv = itemView.findViewById(R.id.idTv);
            txName = itemView.findViewById(R.id.textNameView);
            txPrice = itemView.findViewById(R.id.textpriceView);
            img = itemView.findViewById(R.id.image_viewShop);
            cardView = itemView.findViewById(R.id.cardViewId);
            okBtn = itemView.findViewById(R.id.okBtn);
            cancelBtn = itemView.findViewById(R.id.cancelBtn);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Product product = dataList.get(getAdapterPosition());
                    dataCallBack.Result(product, "", getAdapterPosition());
                    dataCallBack.Result(product,"",getAdapterPosition());


                }
            });

            cardView.setOnLongClickListener(v -> {

                Product product = dataList.get(getAdapterPosition());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                StudentDB dataAccess = new StudentDB(context);
                builder.setTitle("Delete Employee");
                builder.setMessage("Are you sure to delete Employee?");

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    int position = getAdapterPosition();
//                   dataAccess.deleteStudent(studentModel.getId());

                    fireStoreDB.collection("Employee").document(product.id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error deleting document", e);
                                }
                            });

                    dataList.remove(position);
                    notifyItemRemoved(position);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                });
                builder.create().show();
                return false;
            });
        }


    }
}
