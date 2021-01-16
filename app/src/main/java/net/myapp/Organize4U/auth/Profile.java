package net.myapp.Organize4U.auth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TaskInfo;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.myapp.Organize4U.R;

import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

/*
 * A simple {@link Fragment} subclass.
*/
public class Profile extends Fragment {
    FirebaseFirestore fStore;
    ProgressBar progressBarSave;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
FirebaseDatabase firebaseDatabase;
StorageReference storageReference;
String storagePath = "Users_Profile_Cover_Imgs/";
DatabaseReference databaseReference;
    ImageView avatarTV,coverTV;
    TextView nameTV,emailTV;
FloatingActionButton fab;
ProgressDialog pd;

private static final int CAMERA_REQUEST_CODE = 100;
private static final int STORAGE_REQUEST_CODE = 200;
private static final int IMAGE_PICK_CAMERA_CODE = 400;
private static final int IMAGE_PICK_GALLERY_CODE = 300;

String cameraPermissions[];
String storagePermissions[];

Uri image_uri;

String profileOrCoverPhoto;
    public Profile(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        final String uid = user.getUid();
        databaseReference = firebaseDatabase.getReference("Users");
storageReference = getInstance().getReference();
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        avatarTV = view.findViewById(R.id.avatarTv);
        coverTV = view.findViewById(R.id.coverTV);
        nameTV = view.findViewById(R.id.nameTV);
        emailTV = view.findViewById(R.id.emailTV);
        fab = view.findViewById(R.id.fab);
pd = new ProgressDialog(getActivity());
        Query query = databaseReference.orderByChild("email").equalTo(user1.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String email = "" + ds.child("email").getValue();
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    nameTV.setText(name);
                    emailTV.setText(email);
                    try {
                        Picasso.get().load(image).into(avatarTV);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_add_a_photo_black_24dp).into(avatarTV);
                    }
                    try {
                        Picasso.get().load(cover).into(coverTV);
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        return view;
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    public void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
            == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    public void requestCameraPermission(){
      requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Edit Profile picture","Edit Cover Photo","Edit Name"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            if (which ==0) {
                pd.setMessage("Updating Profile Picture");
                profileOrCoverPhoto = "image";
                showImagePicDialog();
            }
            else if (which==1){
                pd.setMessage("Updating Cover Photo");
                profileOrCoverPhoto = "cover";
                showImagePicDialog();
                }
            else if (which==2){
                pd.setMessage("Updating Name");
                showNamePhoneUpdateDialog("name");
            }

            }

        });
            builder.create().show();
        }

    private void showNamePhoneUpdateDialog(final String key) {
AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
builder.setTitle("Update" + " " + key);
        LinearLayout linearLayout = new LinearLayout((getActivity()));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter" + " " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
String value = editText.getText().toString().trim();
if(!TextUtils.isEmpty(value)){
    pd.show();
    HashMap<String, Object> result = new HashMap<>();
    result.put(key,value);
    databaseReference.child(user.getUid()).updateChildren(result)
    .addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
pd.dismiss();
            Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();
        }
    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
pd.dismiss();
Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
}
else{
    Toast.makeText(getActivity(),"Please enter" + key,Toast.LENGTH_SHORT).show();
}
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
    private void showImagePicDialog() {
        String options[] = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which ==0) {
               if (!checkCameraPermission()){
                   requestCameraPermission();
               }
               else{
                   pickFromCamera();
               }
                }
                else if (which==1){
                  if(checkStoragePermission()){
                      requestStoragePermission();
                  }else{pickFromGallery();}
                }


            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE: {
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                    pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_LONG);
                    }}}
                    break;
                    case STORAGE_REQUEST_CODE: {
                        if(grantResults.length > 0){

                            boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                            if(writeStorageAccepted){
                                pickFromGallery();
                            }
                            else {
                                Toast.makeText(getActivity(), "Please enable Gallery", Toast.LENGTH_LONG);
                            }}}
                    break;
                    }

                }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_PICK_GALLERY_CODE){
            if (resultCode == Activity.RESULT_OK){
                assert data != null;
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
        } if (requestCode == IMAGE_PICK_CAMERA_CODE){
            if (resultCode == RESULT_OK){
                assert data != null;

                uploadProfileCoverPhoto(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void  uploadProfileCoverPhoto(final Uri uri) {
        //show progress
        pd.show();

        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+ " " + profileOrCoverPhoto +"_"+ user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        if (uriTask.isSuccessful()){

                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp DESCRIPTION");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_PICK_GALLERY_CODE);
    }
}












