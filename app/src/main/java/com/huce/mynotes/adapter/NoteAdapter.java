package com.huce.mynotes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.huce.mynotes.R;
import com.huce.mynotes.model.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private final Context mContext;
    private final List<Note> mNoteList;
    private final OnNoteItemCallback mCallback;

    public NoteAdapter(Context mContext, List<Note> mNoteList, OnNoteItemCallback mCallback) {
        this.mContext = mContext;
        this.mNoteList = mNoteList;
        this.mCallback = mCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.noteTitle.setText(mNoteList.get(position).getTitle());
        holder.noteContent.setText(mNoteList.get(position).getContent());
        holder.noteTime.setText(mNoteList.get(position).getTime());
        //holder.noteUrl.setText(firebasemodel.getUrl());
//        String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
        //Xem, sửa note
        holder.itemView.setOnClickListener(view -> mCallback.onOpenNoteItem(mNoteList.get(position)));
        //Xoá note
        holder.itemView.setOnLongClickListener(view -> {
            mCallback.onDeleteNoteItem(mNoteList.get(position));
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mNoteList == null ? 0 : mNoteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView noteTitle;
        private TextView noteContent;
        private TextView noteTime;
        LinearLayout mNote;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.textTitle);
            noteContent = itemView.findViewById(R.id.textNote);
            noteTime = itemView.findViewById(R.id.textDateTime);
            mNote = itemView.findViewById(R.id.layoutNote);

        }
    }

    public interface OnNoteItemCallback{
        void onOpenNoteItem(Note note);
        void onDeleteNoteItem(Note note);
    }
}
