package com.alizawren.comiccatalog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.annotation.Nullable;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceBundle) {
        return inflater.inflate(R.layout.fragment_library, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceBundle) {
        Button btnAddBooks = getView().findViewById(R.id.button_add_book);
        btnAddBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_frame, new ScannerFragment())
                        .commit();
            }
        });
    }

}
