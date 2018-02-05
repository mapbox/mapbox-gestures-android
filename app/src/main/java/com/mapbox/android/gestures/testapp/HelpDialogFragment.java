package com.mapbox.android.gestures.testapp;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HelpDialogFragment extends DialogFragment {
  static final String TAG = "help_dialog_fragment_tag";

  public HelpDialogFragment() {
    // Required empty public constructor
  }

  public static HelpDialogFragment newInstance() {
    return new HelpDialogFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_help_dialog, container, false);
    Button gotItButton = (Button) view.findViewById(R.id.button_help_got_it);
    gotItButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
    return view;
  }
}
