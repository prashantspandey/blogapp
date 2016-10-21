package com.homeapplications.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mClickLike = false;
    private DatabaseReference mDatabaseLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                }


            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDatabase.keepSynced(true);

        recyclerView = (RecyclerView) findViewById(R.id.blog_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkIfUserExists();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId()==R.id.action_updateProfile){
            Intent updateProfile = new Intent(MainActivity.this,SetupActivity.class);
            startActivity(updateProfile);
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        private View mview;
        ImageButton likeButton;
       DatabaseReference mDatabaseLike;
       FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
            likeButton = (ImageButton) mview.findViewById(R.id.blog_list_likeEmpty);

           mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
           mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }
        public void setLikeButton(final String postkey){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                        if (dataSnapshot.child(postkey).hasChild(mAuth.getCurrentUser().getUid())){
                            likeButton.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                        }else{
                            likeButton.setImageResource(R.mipmap.ic_thumb_up_white_24dp);
                        }
                    }



                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title) {
            TextView postTitle = (TextView) mview.findViewById(R.id.blog_list_post_title);

            postTitle.setText(title);

        }

        public void setDescription(String description) {
            TextView postDescription = (TextView) mview.findViewById(R.id.blog_list_post_description);
            postDescription.setText(description);
        }

        public void setImage(Context con, String image) {
            ImageView postImage = (ImageView) mview.findViewById(R.id.blog_list_post_image);
            Picasso.with(con).load(image).into(postImage);
        }
        public void setUserName(String userName){
            TextView postUserName = (TextView) mview.findViewById(R.id.blog_list_userName);
            postUserName.setText(userName);
        }
        public void setDisplayPicture(Context con, String image){
            ImageView displayImage = (ImageView) mview.findViewById(R.id.blog_list_displayImage);
            Picasso.with(con).load(image).into(displayImage);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        mAuth.addAuthStateListener(mAuthStateListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class, R.layout.blog_row, BlogViewHolder.class, mDatabase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String postKey = getRef(position).getKey();


                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(), model.getImageUrl());
                viewHolder.setUserName(model.getUserName());
                viewHolder.setDisplayPicture(getApplicationContext(),model.getDisplayPicture());
                viewHolder.setLikeButton(postKey);

                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singlePost =  new Intent(MainActivity.this,BlogPost.class);
                        singlePost.putExtra("BlogId",postKey);
                        startActivity(singlePost);
                    }
                });

                viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClickLike =true;
                        final FirebaseUser user = mAuth.getCurrentUser();

                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mClickLike) {
                                        if (dataSnapshot.child(postKey).hasChild(user.getUid())) {
                                            mDatabaseLike.child(postKey).child(user.getUid()).removeValue();
                                            mClickLike = false;
                                        } else {
                                            mDatabaseLike.child(postKey).child(user.getUid()).setValue("Random");
                                            mClickLike = false;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    }
                });
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void checkIfUserExists() {
        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {
                        Intent setUpIntent = new Intent(MainActivity.this, SetupActivity.class);


                        startActivity(setUpIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener!=null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}