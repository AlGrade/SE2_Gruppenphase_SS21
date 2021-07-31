package com.example.se2_gruppenphase_ss21.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.se2_gruppenphase_ss21.R;
import com.example.se2_gruppenphase_ss21.game.Dice;
import com.example.se2_gruppenphase_ss21.game.alternative_gui.Puzzle;
import com.example.se2_gruppenphase_ss21.networking.client.GameClient;
import com.example.se2_gruppenphase_ss21.networking.client.listeners.GeneralGameListener;
import com.example.se2_gruppenphase_ss21.networking.client.listeners.PreGameListener;
import com.example.se2_gruppenphase_ss21.networking.server.logic.GameLogicException;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnlineRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnlineRoomFragment extends Fragment {

    GameClient client = null;
    int port = 6789;
    static boolean isReady = false;

    // Handler for creating post delay threads for updating ui
    Handler handler = new Handler();


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public OnlineRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OnlineRoomFragment.
     */
    public static OnlineRoomFragment newInstance(String param1, String param2) {
        OnlineRoomFragment fragment = new OnlineRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // force using ui thread for client connection
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_online_room, container, false);

        String userName = getArguments().getString(ARG_PARAM1);
        String roomName = getArguments().getString(ARG_PARAM2);

        TextView roomNameTextView = view.findViewById(R.id.textView_roomName_online);
        roomNameTextView.setText(roomName + "\n\n "+getResources().getString(R.string.current_players));

        try {

            client = new GameClient("swablu.cloud", port, roomName, userName);
            GameClient.setActiveGameClient(client);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            client.connect();

            GeneralGameListener preGameListener = new PreGameListener() {
                @Override
                public void readyCount(int current, int max) {
                    if (isReady) {
                        updateReady(current, max, view);
                    }
                }

                @Override
                public void onGameStart() {
                    SharedPreferences prefs = getActivity()
                            .getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    boolean legacyGUI = prefs.getBoolean("legacyGUI", false);

                    Class toLoad = legacyGUI ? Dice.class : Puzzle.class;
                    Intent intent = new Intent(getActivity(), toLoad);
                    startActivity(intent);
                }

                @Override
                public void userDisconnect(String nickname) {
                    // react on disconnection of user
                }

                @Override
                public void receiveUserList(String[] nicknames) {
                    updatePlayers(nicknames, view);
                }

                @Override
                public void unknownMessage(String message) {
                    // react on unknown message from server
                }
            };

            client.registerListener(preGameListener);
            client.startReceiveLoop();

        } catch (GameLogicException | IOException e) {
            e.printStackTrace();
        }


        Button startGameButton = view.findViewById(R.id.button_ready_online);
        GameClient finalClient = client;
        startGameButton.setOnClickListener((View v) -> {
            try {
                finalClient.sendReady(true);
                isReady = true;
                startGameButton.setVisibility(View.INVISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        /**
         * overrides behaviour of 'back-button'; client should be removed from room when clicking back button
         */
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                client.close();
                getParentFragmentManager().beginTransaction().replace(R.id.container, OnlineGameFragment.newInstance(userName)).addToBackStack("tag").commit();
            }
        });


        return view;
    }

    /**
     * updates UI: shows a list of players that currently joined the room
     *
     * @param nicknames all players that are in a given room
     * @param view      of the fragment
     */
    public void updatePlayers(String[] nicknames, View view) {

        final Runnable runUpdatePlayers = new Runnable() {
            public void run() {

                LinearLayout layout = view.findViewById(R.id.layoutRoom_online);
                layout.removeAllViews();

                for (String n : nicknames) {

                    Button user;

                    user = new Button(getContext());
                    user.setText(n);
                    user.setClickable(false);
                    layout.addView(user);

                }
            }
        };

        handler.postDelayed(runUpdatePlayers, 1000);
    }

    /**
     * updates UI: shows how many users are ready
     *
     * @param current number of users that are currently ready
     * @param max     number of users that are in the room
     * @param view    of the fragment
     */
    public void updateReady(int current, int max, View view) {

        final Runnable runUpdateReady = new Runnable() {
            public void run() {

                LinearLayout layout = view.findViewById(R.id.layoutRoom_online);
                layout.removeAllViews();

                layout.setPadding(450, 200, 450, 200);

                Button readyData;
                readyData = new Button(getContext());
                readyData.setText(current + "/" + max);
                readyData.setClickable(false);
                layout.addView(readyData);

            }
        };

        handler.postDelayed(runUpdateReady, 1000);
    }
}