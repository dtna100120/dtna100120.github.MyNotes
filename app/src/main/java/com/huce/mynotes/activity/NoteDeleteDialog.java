package com.huce.mynotes.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huce.mynotes.R;
import com.huce.mynotes.model.Note;

public class NoteDeleteDialog extends Dialog {
    private final OnNoteDeleteCallback mCallback;
    private Note mNote;

    public NoteDeleteDialog(@NonNull Context context, OnNoteDeleteCallback mCallback) {
        super(context);
        this.mCallback = mCallback;
    }

    public void setNote(Note note){
        this.mNote = note;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_note);
        getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.bg_transparent));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        TextView txtDeleteNote, txtCancel;
        txtDeleteNote = findViewById(R.id.textDeleteNote);
        txtCancel = findViewById(R.id.textCancel);

        txtDeleteNote.setOnClickListener(v -> {
            if(mNote != null){
                mCallback.onConfirmDelete(mNote);
            }
            dismiss();
        });
        txtCancel.setOnClickListener(v -> {
            mCallback.onCancelDelete();
            dismiss();
        });
    }


    public interface OnNoteDeleteCallback{
        void onConfirmDelete(Note note);
        void onCancelDelete();
    }
}
