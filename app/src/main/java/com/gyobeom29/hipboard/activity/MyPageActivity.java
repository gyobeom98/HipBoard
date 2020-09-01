package com.gyobeom29.hipboard.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gyobeom29.hipboard.R;

public class MyPageActivity extends BasicActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;

    @Override
    public void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        setActionBarTitle("MyPage");
        init();

        if(user == null){
            finish();
        }else{
            firestore.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getData().get("name").toString();
                            String address = documentSnapshot.getData().get("address").toString();
                            
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }

    }




    private void init(){
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
    }


}
