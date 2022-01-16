package com.huce.mynotes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

public class EditNoteActivity extends AppCompatActivity {

    private static final String TAG = EditNoteActivity.class.getSimpleName();
    private Intent data;
    private EditText mEditTitle, mEditNote;
    private ImageView imgUpdate, imgBack, imgNote, imgRemoveImage, imgRemoveWebURL;
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
    private AlertDialog dialogDeleteNote;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        init();
        initView();
        initEvent();
        initMiscellaneous();
    }

    private void init() {
        mContext = this;
        data = getIntent();
        mProgressDialog = new ProgressDialog(mContext);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseStore = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStore.getReference();

        imgNote = findViewById(R.id.imageNote);
        textWebURL = findViewById(R.id.textWebURL);
        mLayoutWebURL = findViewById(R.id.layoutWebURL);
    }

    private void initView() {
        mEditTitle = findViewById(R.id.editTitle);
        mEditNote = findViewById(R.id.editNote);
        imgUpdate = findViewById(R.id.imageUpdate);
        imgBack = findViewById(R.id.imageBack);
        mTime = findViewById(R.id.textDateTime);
        imgRemoveImage = findViewById(R.id.imageRemoveImage);
        imgRemoveWebURL = findViewById(R.id.imageRemoveWebURL);


        String notetitle = data.getStringExtra("Title");
        String notecontent = data.getStringExtra("Content");
        String notetime = data.getStringExtra("Time");
        String noteurl = data.getStringExtra("Url");
        String noteImageUrl = data.getStringExtra("Image");
        mEditNote.setText(notecontent);
        mEditTitle.setText(notetitle);
        mTime.setText(notetime);
        textWebURL.setText(noteurl);
        mLayoutWebURL.setVisibility(View.VISIBLE);
        if (noteImageUrl != null) {
            Glide.with(mContext).load(noteImageUrl).into(imgNote);
            imgNote.setVisibility(View.VISIBLE);
            imgRemoveImage.setVisibility(View.VISIBLE);
        } else {
            imgNote.setVisibility(View.GONE);
            imgRemoveImage.setVisibility(View.GONE);
        }
        imgRemoveWebURL.setVisibility((noteurl == null || noteurl.equals("")) ? View.GONE : View.VISIBLE);

    }

    private void initEvent() {
        imgUpdate.setOnClickListener(view -> {
            String newTitle = mEditTitle.getText().toString();
            String newContent = mEditNote.getText().toString();

            String newTime = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date());
            mTime.setText(newTime);

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(mContext, "Something is empty", Toast.LENGTH_SHORT).show();
                return;
            } else {
                uploadImage(newTitle, newContent, newTime);
            }
        });

        imgBack.setOnClickListener(view -> {
            startActivity(new Intent(mContext, NotesActivity.class));
            finish();
        });

        imgRemoveWebURL.setOnClickListener(view -> {
            textWebURL.setText(null);
            mLayoutWebURL.setVisibility(View.GONE);
        });
        imgRemoveImage.setOnClickListener(view -> {
            imgNote.setImageBitmap(null);
            imgNote.setVisibility(View.GONE);
            imgRemoveImage.setVisibility(View.GONE);
        });
    }


    private void initMiscellaneous() {
        if (dialogAddURL != null) {
            dialogAddURL.dismiss();
        }
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(view -> chooseImage());
        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });
//        if (data != null) {
//            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
//            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                    showDeleteNoteDialog();
//                }
//            });
//        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
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
                imgRemoveImage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                imgNote.setVisibility(View.GONE);
                imgRemoveImage.setVisibility(View.GONE);
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
                            Toast.makeText(mContext, "Uploaded", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            doUpdate(title, content, time);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(mContext, "Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        mProgressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        } else  uploadImage(title, content, time);
    }

    private void doUpdate(String title, String content, String time) {
        DocumentReference documentReference = mFirebaseFirestore.collection("Notes").document(mFirebaseUser.getUid()).collection("MyNotes").document(data.getStringExtra("noteId"));
        Map<String, Object> note = new HashMap<>();
        note.put("Title", title);
        note.put("Content", content);
        note.put("Time", time);
        note.put("Image", mImageUrl);
        note.put("Url", textWebURL.getText().toString());
        documentReference.set(note).addOnSuccessListener(unused -> {
            Toast.makeText(mContext, "Note is updated!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mContext, NotesActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(mContext, "Failed to update!", Toast.LENGTH_SHORT).show());
    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.activity_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(view1 -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    Toast.makeText(mContext, "Enter valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    textWebURL.setText(inputURL.getText().toString());
                    mLayoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();

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

//    private void showDeleteNoteDialog() {
//        if (dialogDeleteNote == null) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(EditNoteActivity.this);
//            View view = LayoutInflater.from(this).inflate(
//                    R.layout.dialog_delete_note,
//                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
//            );
//            builder.setView(view);
//            dialogDeleteNote = builder.create();
//            if (dialogDeleteNote.getWindow() != null) {
//                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
//            }
//            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//                            //
//                            return null;
//                        }
//
//                        @Override
//                        protected void onPostExecute(Void aVoid) {
//                            super.onPostExecute(aVoid);
//                            Intent intent = new Intent();
//                            intent.putExtra("isNoteDeleted", true);
//                            setResult(RESULT_OK, intent);
//                            finish();
//                        }
//                    }
//                    new DeleteNoteTask().execute();
//                }
//            });
//
//            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialogDeleteNote.dismiss();
//                }
//            });
//        }
//        dialogDeleteNote.show();
//    }
}