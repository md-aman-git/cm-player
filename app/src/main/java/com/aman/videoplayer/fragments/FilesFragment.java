package com.aman.videoplayer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFiles;
import com.aman.videoplayer.adapters.VideoAdapter;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.aman.videoplayer.activities.MainActivity.videoFiles;


public class FilesFragment extends Fragment implements SearchView.OnQueryTextListener {

    RecyclerView recyclerView;
    VideoAdapter videoAdapter;
    View view;
    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_files, container, false);
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.videoList);
        recyclerView.setHasFixedSize(true);
        if ( videoFiles != null && !(videoFiles.size() < 1))
        {
            videoAdapter = new VideoAdapter(getContext(), videoFiles);
            recyclerView.setAdapter(videoAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                    RecyclerView.VERTICAL, false));
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<VideoFiles> myFiles = new ArrayList<>();
        for (VideoFiles song : videoFiles) {
            if (song.getTitle().toLowerCase().contains(userInput)) {
                myFiles.add(song);
            }
        }
        if (videoAdapter != null)
        videoAdapter.updateList(myFiles);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String MY_PREFS_NAME = "SortOrder";
        String MY_PREFS_THEME = "ThemesChanger";
        final SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_PREFS_NAME,
                MODE_PRIVATE).edit();
        final SharedPreferences.Editor editorThemes = getContext().getSharedPreferences(MY_PREFS_THEME,
                MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.sortByName:
                editor.putString("sorting", "sortByName");
                editor.apply();
                if (getActivity() != null)
                    getActivity().recreate();
                //getContext().recreate();
                Toast.makeText(getContext(),
                        "Name Sorted Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sortByDate:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                if (getActivity() != null)
                    getActivity().recreate();
                Toast.makeText(getContext(),
                        "Date Sorted Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sortBySize:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                if (getActivity() != null)
                    getActivity().recreate();
                Toast.makeText(getContext(),
                        "Size Sorted Clicked!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lightTheme:
                editorThemes.putString("themes", "light");
                editorThemes.apply();
                if (getActivity() != null)
                    getActivity().recreate();
                break;
            case R.id.darkTheme:
                editorThemes.putString("themes", "dark");
                editorThemes.apply();
                if (getActivity() != null)
                    getActivity().recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}