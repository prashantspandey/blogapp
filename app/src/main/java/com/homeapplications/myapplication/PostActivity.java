package com.homeapplications.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_REQUEST = 0;
    private ImageButton imageButton;
    private EditText titleText;
    private EditText descriptionText;
    private Button submitButton;

    private Uri imageUri =  null;
    private StorageReference mStorage;
    private ProgressDialog progressDialog;
    private DatabaseReference mdatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance().getReference();
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

        imageButton = (ImageButton) findViewById(R.id.activity_post_image);
        imageButton.setOnClickListener(this);
        titleText = (EditText) findViewById(R.id.activity_post_titletext);
        descriptionText = (EditText) findViewById(R.id.activity_post_descriptiontext);
        submitButton = (Button) findViewById(R.id.activity_post_submitbutton);
        submitButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id==R.id.activity_post_image){
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        }
         if (id==R.id.activity_post_submitbutton){


            submitPost();
        }
    }

    private void submitPost() {
        progressDialog.setMessage("Submitting Post..");
        progressDialog.show();
        final String title = titleText.getText().toString().trim();
        final String description = descriptionText.getText().toString().trim();

        if (!TextUtils.isEmpty(title)&& !TextUtils.isEmpty(description)&& imageUri!=null){
            StorageReference storageReference = mStorage.child("Blog_Image").child(imageUri.getLastPathSegment());
            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUri = taskSnapshot.getDownloadUrl();
                    final DatabaseReference newPost = mdatabase.push();


                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("Title").setValue(title);
                            newPost.child("Description").setValue(description);
                            newPost.child("ImageUrl").setValue(downloadUri.toString());
                            newPost.child("uid").setValue(currentUser.getUid());
                            newPost.child("displayPicture").setValue(dataSnapshot.child("DisplayImage").getValue());
                            newPost.child("UserName").setValue(dataSnapshot.child("DisplayName").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                                    }else {
                                        Toast.makeText(PostActivity.this,"Error posting",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this,"Posted successfully !",Toast.LENGTH_SHORT).show();


                }
            });
        }
        else {
            Toast.makeText(PostActivity.this,"Please fill in details",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_REQUEST && resultCode ==RESULT_OK){
            imageUri = data.getData();
            imageButton.setImageURI(imageUri);
        }
    }
}
