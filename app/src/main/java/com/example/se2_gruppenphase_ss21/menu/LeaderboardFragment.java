package com.example.se2_gruppenphase_ss21.menu;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.example.se2_gruppenphase_ss21.Player;
import com.example.se2_gruppenphase_ss21.PlayerArrayAdapter;
import com.example.se2_gruppenphase_ss21.R;
import com.example.se2_gruppenphase_ss21.game.Dice;
import com.example.se2_gruppenphase_ss21.game.alternative_gui.Puzzle;
import com.example.se2_gruppenphase_ss21.game.TimerListener;
import com.example.se2_gruppenphase_ss21.game.TimerView;
import com.example.se2_gruppenphase_ss21.networking.client.GameClient;
import com.example.se2_gruppenphase_ss21.networking.client.PlayerPlacement;

import com.example.se2_gruppenphase_ss21.networking.client.listeners.PostRoundListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */

public class LeaderboardFragment extends Fragment implements PostRoundListener, TimerListener {

    ArrayList<PlayerPlacement> placements;
    private View view;
    private ListView listView;
    private GameClient gameClient;
    private List<String[]> playerList;
    PlayerArrayAdapter playerArrayAdapter;


    private static final long CHALLENGE_TIMEOUT = 5000; // 5 seconds

    public LeaderboardFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);

        placements = getActivity().getIntent().getParcelableArrayListExtra("placements");

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        Button quitGameButton = view.findViewById(R.id.buttonQuitGame);
        quitGameButton.setOnClickListener((View v) ->{
            GameClient gameClient = GameClient.getActiveGameClient();
            gameClient.close();

            getParentFragmentManager().beginTransaction().replace(R.id.container, new MenuFragment()).addToBackStack("tag").commit();
        });

        gameClient = GameClient.getActiveGameClient();
        gameClient.registerListener(this);

        listView = (ListView) view.findViewById(R.id.listView);
        playerArrayAdapter = new PlayerArrayAdapter(view.getContext(), R.layout.listview_row_layout);
        listView.setAdapter(playerArrayAdapter);

        playerList = readData(placements);
        for(String[] playerData:playerList){
            String position = playerData[0];
            String playername = playerData[1];
            String points = playerData[2];
            Player player = new Player(position, playername, points);
            playerArrayAdapter.add(player);
        }

        // start timer
        TimerView timer = view.findViewById(R.id.challengeTimer);
        timer.setListener(this);

        long finishUntil = CHALLENGE_TIMEOUT + System.currentTimeMillis();

        timer.start(finishUntil);

        return view;
    }

    //List of players
    private List<String[]> readData(ArrayList<PlayerPlacement> placements) {
        List<String[]> resultList = new ArrayList<>();
        for (int i = 0; i < placements.size(); i++) {
            String[] player = new String[3];
            player[0] = Integer.toString(i+1);
            player[1] = placements.get(i).getNickname();
            Resources res = getResources();
            player[2] = res.getQuantityString(R.plurals.points, placements.get(i).getPoints(), placements.get(i).getPoints());
            resultList.add(player);
        }
        return resultList;
    }

    @Override
    public void transitionToDice() {
        SharedPreferences prefs = getActivity()
                .getSharedPreferences("prefs", Context.MODE_PRIVATE);
        boolean altGUI = prefs.getBoolean("altGUI", false);

        Class toLoad = altGUI ? Puzzle.class : Dice.class;
        Intent intent = new Intent(getActivity(), toLoad);
        startActivity(intent);
    }
    @Override
    public void endGame() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void accusationResult(String accuser, String accused, boolean wasCheating, int pointLoss) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {

            for (int i = 0; i < listView.getCount(); i++) {
                View row = listView.getChildAt(i);
                TextView name = (TextView) row.findViewById(R.id.playerName);
                TextView points = (TextView) row.findViewById(R.id.points);

                Resources res = getResources();

                if (wasCheating) {
                    if (name.getText().equals(accused)) {
                        int p = Integer.parseInt(String.valueOf(points.getText()).split(" ",2)[0]);
                        p -= pointLoss;
                        points.setText(res.getQuantityString(R.plurals.points, p, p));
                    }
                } else {
                    if (name.getText().equals(accuser)) {
                        int p = Integer.parseInt(String.valueOf(points.getText()).split(" ",2)[0]);
                        p -= 1;
                        points.setText(res.getQuantityString(R.plurals.points, p, p));
                    }
                }
            }

            playerArrayAdapter.notifyDataSetChanged();
        });
    }
    @Override
    public void userDisconnect(String nickname) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->
                Toast.makeText(getActivity(), "Player "+nickname+" disconnected!", Toast.LENGTH_LONG).show()
        );
    }
    @Override
    public void unknownMessage(String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->
                Toast.makeText(getActivity(), "Network error: "+message, Toast.LENGTH_LONG).show()
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void timeIsUp(TimerView timer) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {

            for (int i = 0; i < listView.getCount(); i++) {
                View row = listView.getChildAt(i);
                TextView name = (TextView) row.findViewById(R.id.playerName);
                CheckBox challenge = (CheckBox) row.findViewById(R.id.challenge);

                if (challenge.isChecked()) {
                    try {
                        gameClient.accuseOfCheating(String.valueOf(name.getText()));
                    } catch (IOException ex) {
                        Log.e("leaderboard", ex.toString());
                        Toast.makeText(this.getActivity(), "Connection to the server failed", Toast.LENGTH_LONG).show();
                    }
                }

                challenge.setVisibility(View.INVISIBLE);
            }
            view.findViewById(R.id.challengeTimer).setVisibility(View.INVISIBLE);
            updateList();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateList(){
        ArrayList<PlayerPlacement> newPlayerslist = new ArrayList<>();
        for (int i = 0; i < listView.getCount(); i++) {
            View row = listView.getChildAt(i);
            TextView name = (TextView) row.findViewById(R.id.playerName);
            TextView points = (TextView) row.findViewById(R.id.points);
            newPlayerslist.add(new PlayerPlacement(i, name.getText().toString(), Integer.parseInt(String.valueOf(points.getText()).split(" ",2)[0])));
        }

        Collections.sort(newPlayerslist, (o1, o2) -> o1.getPlacement() - o2.getPoints());

        PlayerArrayAdapter newplayarray = new PlayerArrayAdapter(view.getContext(), R.layout.listview_row_layout);
        playerList = readData(newPlayerslist);

        for (String[] playerData:playerList){
            String position = playerData[0];
            String playername = playerData[1];
            String points = playerData[2];
            Player player = new Player(position, playername, points);
            newplayarray.add(player);
        }
        listView.setAdapter(newplayarray);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            for (int i = 0; i < listView.getCount(); i++) {
                View row = listView.getChildAt(i);
                CheckBox challenge = (CheckBox) row.findViewById(R.id.challenge);
                challenge.setVisibility(View.INVISIBLE);
            }
        });
    }
}
