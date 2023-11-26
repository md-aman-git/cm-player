package com.aman.videoplayer.fragments;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.ApkModal;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ShareFiles;
import com.aman.videoplayer.adapters.ApkFileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectAppsFragment extends Fragment {

    View view;
    RecyclerView apkRV;
    ArrayList<ApkModal> appsInfo = new ArrayList<>();
    public SelectAppsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_select_apps, container, false);
        apkRV = view.findViewById(R.id.apkFileRV);
        List<ApplicationInfo> application = getContext()
                .getPackageManager().getInstalledApplications(0);
        for(ApplicationInfo app : application) {
            if(!((app.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                    | ApplicationInfo.FLAG_SYSTEM)) > 0)) {
                // user app...
                ApkModal apkModal = new ApkModal(app.loadLabel(getActivity()
                        .getPackageManager()).toString(), app.publicSourceDir,
                        String.valueOf(new File(app.publicSourceDir).length()),
                        app.packageName,
                        app.loadIcon(getActivity().getPackageManager()), 'a');
                appsInfo.add(apkModal);
            }
        }
        ApkFileAdapter apkFileAdapter = new ApkFileAdapter(getContext(), appsInfo);
        apkRV.setAdapter(apkFileAdapter);
        apkRV.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false));
        return view;
    }
}