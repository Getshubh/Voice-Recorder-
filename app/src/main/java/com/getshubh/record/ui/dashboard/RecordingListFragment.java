package com.getshubh.record.ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getshubh.record.MainActivity;
import com.getshubh.record.R;
import com.getshubh.record.adapter.RecordingAdapter;
import com.getshubh.record.model.Recording;
import com.getshubh.record.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class RecordingListFragment extends Fragment {

    private RecyclerView recyclerViewRecordings;
    private ArrayList<Recording> recordingArraylist;
    private RecordingAdapter recordingAdapter;
    private TextView textViewNoRecordings;
    private EditText etSearchView;
    private ImageView ivCancel;
    private RelativeLayout rlSearch;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_recording_list, container, false);

        recordingArraylist = new ArrayList<Recording>();

        recyclerViewRecordings = root.findViewById(R.id.recyclerViewRecordings);
        etSearchView = root.findViewById(R.id.et_searchview);
        ivCancel = root.findViewById(R.id.iv_cancel);
        rlSearch = root.findViewById(R.id.rl_search);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        recyclerViewRecordings.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recordingAdapter != null)
                    recordingAdapter.stopPlayingAudio(true);
            }
        });

        textViewNoRecordings = root.findViewById(R.id.textViewNoRecordings);
        fetchRecordings();

        etSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivCancel.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearchView.setText("");
                etSearchView.setCursorVisible(true);
                etSearchView.setFocusable(true);
            }
        });

        return root;


    }

    void filter(String text) {
        ArrayList<Recording> temp = new ArrayList();
        for (Recording d : recordingArraylist) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getFileName().contains(text)) {
                temp.add(d);
            }
        }
        //update recyclerview
        recordingAdapter.updateList(temp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbar(getContext().getString(R.string.title_dashboard));
    }

    public void fetchRecordings() {

        recordingArraylist = new ArrayList<>();
        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/Audio";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null)
            Log.d("Files", "Size: " + files.length);
        if (files != null) {

            for (int i = 0; i < files.length; i++) {

                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/Audio/" + fileName;
                String audioFileLength = Utils.getInstance().getAudioFileLength(recordingUri, true, getContext());
                Recording recording = new Recording(recordingUri, fileName, false, audioFileLength);
                recordingArraylist.add(recording);
            }


            if (files.length > 0) {
                visibleNoRecording(false);
                setAdaptertoRecyclerView();
            } else {
                visibleNoRecording(true);
            }

        } else {
            visibleNoRecording(true);
        }

    }

    private void visibleNoRecording(boolean visibleNoRecording) {
        textViewNoRecordings.setVisibility(visibleNoRecording ? View.VISIBLE : View.GONE);
        recyclerViewRecordings.setVisibility(visibleNoRecording ? View.GONE : View.VISIBLE);
        if (visibleNoRecording)
            rlSearch.setVisibility(View.GONE);
        else {
            if (recordingArraylist != null && recordingArraylist.size() > 1)
                rlSearch.setVisibility(View.VISIBLE);
            else
                rlSearch.setVisibility(View.GONE);
        }
    }


    private void setAdaptertoRecyclerView() {
        recordingAdapter = new RecordingAdapter(getActivity(), recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recordingAdapter != null)
            recordingAdapter.stopPlayingAudio(false);
    }
}