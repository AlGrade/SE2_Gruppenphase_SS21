package com.example.se2_gruppenphase_ss21.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.se2_gruppenphase_ss21.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private static String myPrefs = "prefs";
    private static String switchStatus = "switchOn";

    boolean switchOn;

    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch soundSwitch = view.findViewById(R.id.switch_sound);

        myPreferences = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        myEditor = myPreferences.edit();
        switchOn = myPreferences.getBoolean(switchStatus, true);
        soundSwitch.setChecked(switchOn);

        soundSwitch.setOnCheckedChangeListener((CompoundButton compoundButton, boolean isChecked) -> {
            AudioManager amanager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

            if (isChecked) {
                amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);

                myEditor.putBoolean(switchStatus, true);
                myEditor.apply();
                soundSwitch.setChecked(true);
            } else {
                amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);

                myEditor.putBoolean(switchStatus, false);
                myEditor.apply();
                soundSwitch.setChecked(false);
            }
        });

        Switch altPuzzleGUI = view.findViewById(R.id.switch_altGUI);
        altPuzzleGUI.setChecked(myPreferences.getBoolean("legacyGUI", false));

        altPuzzleGUI.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            myEditor.putBoolean("legacyGUI", isChecked);
            myEditor.apply();
        });

        view.findViewById(R.id.back).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new MenuFragment())
                    .commit();
        });

        return view;
    }

}