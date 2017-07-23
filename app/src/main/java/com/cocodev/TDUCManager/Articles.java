package com.cocodev.TDUCManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cocodev.TDUCManager.Utility.Article;
import com.cocodev.TDUCManager.Utility.User;
import com.cocodev.TDUCManager.adapter.NothingSelectedSpinnerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.Iterator;

public class Articles extends AppCompatActivity {
    ImageView imgView;
    int PICK_IMAGE_REQUEST = 111;
    Uri filePath;
    DatabaseReference mArticleRef;
    ProgressDialog progressDialog;
    EditText mTagline, mTitle, mFullArticle, mAuthor, mDepartment, mImageUrl;
    Article article;
    Button mSubmit, mImagePicker;
    FirebaseUser user;
    String writerUID;

    Spinner departmentChoices,collegeChoices,categoryChoices;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public static User currentUser;
    private String Uid;
    private String Tagline;
    private String Author;
    private String Title;
    private String Content;

    private String Department;
    private String Image;
    private ArrayAdapter<String> collegeAdapter;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();

    private static String DEFAULT_SPINNER_TEXT = "[Select a College..]";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_logout:
                currentUser = null;
                mFirebaseUser = null;
                mFirebaseAuth.signOut();
                Intent intent = new Intent(this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Upload Articles");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009688")));
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        writerUID = mFirebaseUser.getUid();

        mArticleRef = FirebaseDatabase.getInstance().getReference().child("Articles");

        mTagline = (EditText) findViewById(R.id.editText_tagline);
        mTitle = (EditText) findViewById(R.id.editText_article_title);
        mFullArticle = (EditText) findViewById(R.id.editText_article);
        mAuthor = (EditText) findViewById(R.id.editText_author);
        mImageUrl = (EditText) findViewById(R.id.editText_image);
        imgView = (ImageView) findViewById(R.id.image_view_show_article);
        mImagePicker = (Button) findViewById(R.id.button_image_picker);
        collegeChoices = (Spinner) findViewById(R.id.spinner_college_articles);
        departmentChoices = (Spinner) findViewById(R.id.spinner_department_articles);

        categoryChoices = (Spinner) findViewById(R.id.spinner_category_articles);


        initCollegeSpinner();
        initCategorySpinner();


        mImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

        mSubmit = (Button) findViewById(R.id.button_submit);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

    }

    private void uploadImage() {
        Uid = mArticleRef.push().getKey();
        Tagline = mTagline.getText().toString();
        Author = mAuthor.getText().toString();
        Title = mTitle.getText().toString();
        Content = mFullArticle.getText().toString();
        if(!checkFields())
            return;
        if (filePath != null) {
            progressDialog.show();

            StorageReference childRef = storageReference.child("Articles").child(filePath.getLastPathSegment());
            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Uri imageurl = taskSnapshot.getDownloadUrl();
                    Image = imageurl.toString();
                    Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    bindData();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            bindData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                //Setting image to ImageView
                imgView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initCollegeSpinner() {

        final ArrayList<String> colleges =new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,colleges);
        collegeChoices.setAdapter(new NothingSelectedSpinnerAdapter(
                arrayAdapter,
                R.layout.contact_spinner_row_nothing_selected,
                this));
        DatabaseReference collegesDR = FirebaseDatabase.getInstance().getReference().child("CollegeList");

        arrayAdapter.add("University of Delhi");

        collegesDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot temp = iterator.next();
                    //get name of the department
                    String college = temp.getKey().toString();
                    colleges.add(college);
                    //to reflect changes in the ui
                    arrayAdapter.notifyDataSetChanged();
                    //collegeChoices.setSelection(arrayAdapter.getPosition(department));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //We will see this later

            }
        });
        collegeChoices.setOnItemSelectedListener(collegeSelectedListener);
    }

    AdapterView.OnItemSelectedListener collegeSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.e("tag","Position  = " + Integer.toString(position));
            if(position==0 || position==1){
                departmentChoices.setVisibility(View.GONE);
                departmentChoices.setSelection(0);
            }else {
                departmentChoices.setVisibility(View.VISIBLE);
                initDepartmentSpinner();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initCategorySpinner() {

        final ArrayList<String> category =new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,category);
        categoryChoices.setAdapter(new NothingSelectedSpinnerAdapter(
                arrayAdapter,
                R.layout.category_spinner_row_nothing_selected,
                this));
        DatabaseReference collegesDR = FirebaseDatabase.getInstance().getReference().child("CategoryList").child("Articles");
        collegesDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot temp = iterator.next();
                    //get name of the department
                    String college = temp.getKey().toString();
                    category.add(college);
                    //to reflect changes in the ui
                    arrayAdapter.notifyDataSetChanged();
                    //collegeChoices.setSelection(arrayAdapter.getPosition(department));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //We will see this later

            }
        });
        // TODO :categoryChoices.setOnItemSelectedListener(categorySelectedListener);
    }

    private void initDepartmentSpinner() {
        final ArrayList<String> departments =new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,departments);
        departmentChoices.setAdapter(new NothingSelectedSpinnerAdapter(
                arrayAdapter,
                R.layout.contact_spinner_row_nothing_selected_department,
                this));

        DatabaseReference departmensDR = FirebaseDatabase.getInstance().getReference().child("CollegeList")
                .child((String)collegeChoices.getSelectedItem());

        departmensDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot temp = iterator.next();
                    //get name of the department
                    String department = temp.getKey().toString();
                    departments.add(department);
                    //to reflect changes in the ui
                    arrayAdapter.notifyDataSetChanged();
                    //collegeChoices.setSelection(arrayAdapter.getPosition(department));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //We will see this later

            }
        });

    }

    private void bindData() {


        if(departmentChoices.getSelectedItemPosition()==0){
            Department="";
        }else {
            Department = (String) departmentChoices.getSelectedItem();
        }
        mImageUrl.setText(Image);

        if (checkFields()) {
            article = new Article(Uid,Author, Content, System.currentTimeMillis(), Tagline, Image, Title, writerUID, Department);

            if(collegeChoices.getSelectedItemPosition()>1){
                FirebaseDatabase.getInstance().getReference().child("College Content")
                        .child((String)collegeChoices.getSelectedItem())
                        .child("Articles")
                        .child(Uid)
                        .setValue(article);


                if(!Department.equals("")){
                    FirebaseDatabase.getInstance().getReference().child("College Content")
                            .child((String)collegeChoices.getSelectedItem())
                            .child("Department")
                            .child(Department)
                            .child(Uid)
                            .setValue(Uid);
                }
                if(categoryChoices.getSelectedItemPosition()!=0){
                    FirebaseDatabase.getInstance().getReference()
                            .child("College Content")
                            .child((String)collegeChoices.getSelectedItem())
                            .child("Categories")
                            .child("Articles")
                            .child((String)categoryChoices.getSelectedItem())
                            .child(Uid)
                            .setValue(Uid);
                }

                Toast.makeText(this,"Article Uploaded!",Toast.LENGTH_SHORT).show();
            }
            else {
                mArticleRef.child(Uid).setValue(article).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Articles.this, "Article Uploaded!", Toast.LENGTH_LONG).show();
                    }
                });
                if(categoryChoices.getSelectedItemPosition()!=0){
                    FirebaseDatabase.getInstance().getReference().child("Categories")
                            .child("Articles")
                            .child((String)categoryChoices.getSelectedItem())
                            .child(Uid)
                            .setValue(Uid);
                }
            }
        }

    }

    private boolean checkFields() {

        if (TextUtils.isEmpty(Author)) {
            mAuthor.setError("Field must not be empty");
            return false;
        }
        if (TextUtils.isEmpty(Title)) {
            mTitle.setError("Field must not be empty");
            return false;
        }
        if (TextUtils.isEmpty(Content)) {
            mFullArticle.setError("Field must not be empty");
            return false;
        }

        return true;
    }


    private static String getCurrentTime() {
        Long time = System.currentTimeMillis();
        String ts = time.toString();
        return ts;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
