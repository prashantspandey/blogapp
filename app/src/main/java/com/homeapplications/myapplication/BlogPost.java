package com.homeapplications.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogPost extends AppCompatActivity {

    private ImageView mpostImage,mpostAuthorImage;
    private TextView mpostTitle,mpostDescription,mpostAuthor;

    private String mPostKey = null;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Button deletebutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_post);
        deletebutton = (Button) findViewById(R.id.blog_menu_delete);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mpostAuthor = (TextView) findViewById(R.id.activity_blog_postAuthor);
        mpostTitle = (TextView) findViewById(R.id.activity_blog_postTitle);
        mpostDescription = (TextView) findViewById(R.id.activity_blog_postDescription);
        mpostImage = (ImageView) findViewById(R.id.activity_blog_postImage);
        mpostAuthorImage = (ImageView) findViewById(R.id.activity_blog_postDisplayPicture);


        mPostKey = getIntent().getStringExtra("BlogId");
        //Toast.makeText(this,mPostKey,Toast.LENGTH_SHORT).show();
        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postTitle = dataSnapshot.child("Title").getValue().toString();
                String postDescription = dataSnapshot.child("Description").getValue().toString();
                String postAuthor = dataSnapshot.child("UserName").getValue().toString();
                String postImage = dataSnapshot.child("ImageUrl").getValue().toString();
                String postAuthorPhoto = dataSnapshot.child("displayPicture").getValue().toString();
                String postUid = dataSnapshot.child("uid").getValue().toString();

                mpostTitle.setText(postTitle);
                mpostAuthor.setText(postAuthor);
                mpostDescription.setText(postDescription);
                Picasso.with(BlogPost.this).load(postImage).into(mpostImage);
                Picasso.with(BlogPost.this).load(postAuthorPhoto).into(mpostAuthorImage);

                if(mAuth.getCurrentUser().getUid().equals(mPostKey)){

                    deletebutton.setVisibility(View.GONE);
                }else{
                    deletebutton.setVisibility(View.VISIBLE);

                }





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blog_post_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.blog_menu_logout){
            logout();
        }
        if (item.getItemId()==R.id.blog_menu_delete){
            deletebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabase.child(mPostKey).removeValue();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        mAuth.signOut();
    }

}
