package com.example.se2_gruppenphase_ss21.game.alternative_gui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.se2_gruppenphase_ss21.R;
import com.example.se2_gruppenphase_ss21.networking.client.GameClient;
import com.example.se2_gruppenphase_ss21.networking.client.listeners.PreRoundListener;
import com.example.se2_gruppenphase_ss21.logic.tetris.StructureLoader;

import java.util.Random;

/**
 * This class represents the DICE.
 * It receives a result from the server and displays it via an roll animation.
 * Then a random map will be picked and the corresponding TILEs
 * (according to dice) will be selected and passed to the PlayField.
 *
 * @author Manuel Simon #00326348
 */
public class Dice extends Fragment implements PreRoundListener {

    private GameClient client;

    private static final int ANTILOPE_ID = 1;
    private static final int LION_ID = 2;
    private static final int ELEPHANT_ID = 3;
    private static final int BUG_ID = 4;
    private static final int HAND_ID = 5;
    private static final int SNAKE_ID = 6;

    private int diceResultID = ANTILOPE_ID;
    private int animationTime = 5000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = GameClient.getActiveGameClient();
        client.registerListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup puzzleContainer,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dice, puzzleContainer, false);
        return view;
    }

    private void animateDiceRoll() {
        LinearLayout frame = getView().findViewById(R.id.frame_roll);
        float distance = frame.getWidth();
        String animationType = "translationX";

        ObjectAnimator rollAnimation = ObjectAnimator.ofFloat(frame, animationType, distance);
        rollAnimation.setInterpolator(new LinearInterpolator());
        rollAnimation.setDuration(animationTime/2);

        ObjectAnimator reset = ObjectAnimator.ofFloat(frame, animationType, 0f);
        reset.setInterpolator(new LinearInterpolator());
        reset.setDuration(0);

        float target = calculateTargetPos(frame);
        ObjectAnimator stop = ObjectAnimator.ofFloat(frame, animationType, target);
        stop.setInterpolator(new DecelerateInterpolator());
        stop.setDuration(animationTime/2);

        AnimatorSet animation = new AnimatorSet();
        animation.playSequentially(rollAnimation, reset, stop);

        animation.start();
    }

    private float calculateTargetPos(View frame) {
        View target = getDiceResultView();
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        return frame.getWidth() - target.getRight() + target.getWidth() / 2 - width / 2;
    }

    private View getDiceResultView() {
        switch(diceResultID) {
            case(ANTILOPE_ID):
                return getView().findViewById(R.id.antilope);
            case(LION_ID):
                return getView().findViewById(R.id.lion);
            case(ELEPHANT_ID):
                return getView().findViewById(R.id.elephant);
            case(BUG_ID):
                return getView().findViewById(R.id.bug);
            case(HAND_ID):
                return getView().findViewById(R.id.hand);
            default:
                return getView().findViewById(R.id.snake);
        }
    }

    private String getDiceResultName() {
        switch(diceResultID) {
            case(ANTILOPE_ID):
                return "antilope";
            case(LION_ID):
                return "lion";
            case(ELEPHANT_ID):
                return "elephant";
            case(BUG_ID):
                return "bug";
            case(HAND_ID):
                return "hand";
            default:
                return "snake";
        }
    }

    @Override
    public void playDiceAnimation(int result) {
        diceResultID = result;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> animateDiceRoll(), 500);
    }

    @Override
    public void transitionToPuzzle() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {

            int mapID = new Random().nextInt(35) + 2;
            int[] tiles = StructureLoader.getTiles(getContext().getAssets(),
                    getDiceResultName(), mapID);

            Bundle bundle = new Bundle();
            bundle.putIntArray("tiles", tiles);
            bundle.putInt("mapID", mapID);
            PlayField pf = new PlayField();
            pf.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.puzzle_container, pf)
                    .commit();
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
}
