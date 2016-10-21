package com.homeapplications.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton addPhoto;
    private EditText addDisplayName;
    private Button updateProfile;
    private Uri mImageUri = null;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorageImage;
    private ProgressDialog progressDialog;

    private  static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mStorageImage = FirebaseStorage.getInstance().getReference().child("DisplayImages");
        addPhoto = (ImageButton) findViewById(R.id.activity_setup_addPhoto);
        addDisplayName = (EditText) findViewById(R.id.activity_setup_displayName);
        updateProfile = (Button) findViewById(R.id.activity_setup_updateButton);
        progressDialog = new ProgressDialog(this);

        addPhoto.setOnClickListener(this);
        updateProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id==R.id.activity_setup_addPhoto){
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,GALLERY_REQUEST_CODE);
        }
        if (id==R.id.activity_setup_updateButton){
            setUpAccount();
        }

    }

    private void setUpAccount() {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();
        final String dName = addDisplayName.getText().toString().trim();
        final String userId = mAuth.getCurrentUser().getUid();
        if (!TextUtils.isEmpty(dName) && mImageUri!=null){
            StorageReference filePath = mStorageImage.child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUri =  taskSnapshot.getDownloadUrl().toString();
                    mDatabaseUsers.child(userId).child("DisplayName").setValue(dName);
                    mDatabaseUsers.child(userId).child("DisplayImage").setValue(downloadUri);

                    progressDialog.dismiss();
                    Intent loginIntent = new Intent(SetupActivity.this, MainActivity.class);

                    startActivity(loginIntent);
                }

            });




        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            Uri mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(5,5)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                addPhoto.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
