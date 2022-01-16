package com.huce.mynotes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.huce.mynotes.R;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateNoteActivity extends AppCompatActivity {

    private static final String TAG = CreateNoteActivity.class.getSimpleName();
    private EditText mTitle, mContent;
    private ImageView imgBack, imgSave, imgNote;
    private TextView mTime, textWebURL;
    public Uri mImageUri;
    private String mImageUrl;
    private LinearLayout mLayoutWebURL;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStore;
    private StorageReference mStorageReference;
    private ProgressDialog mProgressDialog;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE = 3;

    private AlertDialog dialogAddURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        init();
        initView();
        processEvents();
    }

    private void init() {
        mProgressDialog = new ProgressDialog(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initView() {
        mTitle = findViewById(R.id.inputNoteTitle);
        mContent = findViewById(R.id.inputNote);
        imgBack = findViewById(R.id.imageBack);
        imgSave = findViewById(R.id.imageSave);
        imgNote = findViewById(R.id.imageNote);
        mFirebaseStore = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStore.getReference();
        textWebURL = findViewById(R.id.textWebURL);
        mLayoutWebURL = findViewById(R.id.layoutWebURL);
        mTime = findViewById(R.id.textDateTime);
    }


    private void processEvents() {
        try {
            //Lấy thời gian thực
            String time = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date());
            mTime.setText(time);

            //Hiện nút xoá Url khi có Url
            findViewById(R.id.imageRemoveWebURL).setOnClickListener(view -> {
                textWebURL.setText(null);
                mLayoutWebURL.setVisibility(View.GONE);
            });

            //Hiện nút xoá ảnh khi có ảnh
            findViewById(R.id.imageRemoveImage).setOnClickListener(view -> {
                imgNote.setImageBitmap(null);
                imgNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
            });

            initMiscellaneous();

            //Sự kiện nút save
            imgSave.setOnClickListener(view -> {
                String title = mTitle.getText().toString();
                String content = mContent.getText().toString();
                String link = textWebURL.getText().toString();
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Both field are require!", Toast.LENGTH_SHORT).show();
                } else {
                    //Đẩy dữ liệu lên Firebase
                    uploadImage(title, content, time);
                }
            });

            //Sự kiện nút back
            imgBack.setOnClickListener(view -> {
                startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class));
                finish();
            });
        } catch (Exception ex) {
            Log.e("Events: ", ex.getMessage());
        }
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });
        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imgNote.setImageURI(mImageUri);
            try {
                InputStream inputStream = getContentResolver().openInputStream(mImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgNote.setImageBitmap(bitmap);
                imgNote.setVisibility(View.VISIBLE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    //Upload ảnh lên firebase Storage
    private void uploadImage(String title, String content, String time) {
        if (mImageUri != null) {
            mProgressDialog.show();
            StorageReference ref = mStorageReference.child("images/" + System.currentTimeMillis());
            ref.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            mImageUrl = task.getResult().toString();
                            Toast.makeText(CreateNoteActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            addNote(title, content, time);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateNoteActivity.this, "Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        mProgressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        }
        else addNote(title, content, time);
    }

    private void addNote(String title, String content, String time) {
        DocumentReference documentReference = mFirebaseFirestore.collection("Notes").document(mFirebaseUser.getUid()).collection("MyNotes").document();
        Map<String, Object> note = new HashMap<>();
        note.put("Title", title);
        note.put("Content", content);
        note.put("Time", time);
        note.put("Image", mImageUrl);
        note.put("Url", textWebURL.getText().toString());
        documentReference.set(note).addOnSuccessListener(unused -> {
            Toast.makeText(getApplicationContext(), "Note Created Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed To Create Note!", Toast.LENGTH_SHORT).show());
    }

    //Xử lý hiển thị Url
    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.activity_add_url,
                    findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebURL.setText(inputURL.getText().toString());
                        mLayoutWebURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();

                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }
}