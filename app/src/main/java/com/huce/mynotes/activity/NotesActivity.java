package com.huce.mynotes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.huce.mynotes.R;
import com.huce.mynotes.adapter.NoteAdapter;
import com.huce.mynotes.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity implements NoteAdapter.OnNoteItemCallback, NoteDeleteDialog.OnNoteDeleteCallback {

    private static final String TAG = NotesActivity.class.getSimpleName();
    private FloatingActionButton mCreateNote;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStore;
    private NoteDeleteDialog mDeleteNoteDialog;
    private EditText edtSearch;

    private List<Note> mNoteList;
    private List<Note> mNoteSearchList;
    private NoteAdapter mNoteAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        init();
        initView();
        initEvent();
        initData();
        processEvents();
    }

    private void initEvent() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onSearchNote();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void init() {
        mContext = this;
        mCreateNote = findViewById(R.id.createNote);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStore = FirebaseStorage.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initView() {
        edtSearch = findViewById(R.id.edtSearch);
        mRecyclerView = findViewById(R.id.recyclerview);

        mDeleteNoteDialog = new NoteDeleteDialog(mContext, this);
    }

    private void initData() {
        mNoteList = new ArrayList<>();
        mNoteSearchList = new ArrayList<>();
        mNoteAdapter = new NoteAdapter(mContext, mNoteSearchList, this);
        mRecyclerView.setAdapter(mNoteAdapter);
    }

    private void processEvents() {
        try {
            getSupportActionBar().setTitle("All Notes");
            mCreateNote.setOnClickListener(view -> startActivity(new Intent(NotesActivity.this, CreateNoteActivity.class)));

            mFirebaseFirestore
                    .collection("Notes")
                    .document(mFirebaseUser.getUid())
                    .collection("MyNotes")
                    .orderBy("Title", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            Log.e(TAG, value.getDocuments().toString());
                            mNoteList.clear();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                Note note = document.toObject(Note.class);
                                note.setId(document.getId());
                                Log.e(TAG, note.toString());
                                mNoteList.add(note);
                            }
                            onSearchNote();
                        }
                    });
        } catch (Exception ex) {
            Log.e("Events: ", ex.getMessage());
        }
    }

    private void onSearchNote() {
        mNoteSearchList.clear();
        if (edtSearch.getText().toString().equals("")) {
            mNoteSearchList.addAll(mNoteList);
        } else {
            for (Note note : mNoteList) {
                if (note.getTitle().contains(edtSearch.getText().toString())) {
                    mNoteSearchList.add(note);
                }
            }
        }
        mNoteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOpenNoteItem(Note note) {
        Intent intent = new Intent(mContext, EditNoteActivity.class);
        intent.putExtra("Title", note.getTitle());
        intent.putExtra("Content", note.getContent());
        intent.putExtra("Time", note.getTime());
        intent.putExtra("noteId", note.getId());
        intent.putExtra("Url", note.getUrl());
        intent.putExtra("Image", note.getImage());
        startActivity(intent);
    }

    @Override
    public void onDeleteNoteItem(Note note) {
        mDeleteNoteDialog.setNote(note);
        mDeleteNoteDialog.show();
    }



    @Override
    public void onConfirmDelete(Note note) {
        DocumentReference documentReference = mFirebaseFirestore.collection("Notes").document(mFirebaseUser.getUid()).collection("MyNotes").document(note.getId());
        documentReference.delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "This note is deleted!", Toast.LENGTH_SHORT).show();
                    if (note.getImage() != null) {
                        mFirebaseStore.getReferenceFromUrl(note.getImage())
                                .delete()
                                .addOnSuccessListener(unused1 -> {
                                    Toast.makeText(getApplicationContext(), "Image note is deleted!", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Failed Image to delete!", Toast.LENGTH_SHORT).show();

                                });
                    }


                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to delete!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onCancelDelete() {
        Toast.makeText(getApplicationContext(), "Cancel to delete!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mFirebaseAuth.signOut();
                finish();
                startActivity(new Intent(NotesActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}