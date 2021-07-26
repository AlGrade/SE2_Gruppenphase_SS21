package com.example.se2_gruppenphase_ss21.networking.server.logic;

import com.example.se2_gruppenphase_ss21.networking.ServerMessage;
import com.example.se2_gruppenphase_ss21.networking.SocketWrapper;
import com.example.se2_gruppenphase_ss21.networking.Util;

import java.io.IOException;
import java.util.*;

public class GameRoom {
    private static final int DEFAULT_MAX_USERS = 4;
    private static final int ROUND_COUNT = 5;
    private static final int PUZZLE_DURATION = 60 * 1000;
    private static final int WAIT_FOR_ACTIVITY = 2;

    private ArrayList<GameClientHandler> handlers = new ArrayList<>();

    private int maxUsers;

    private GameRoomState state;

    public GameRoom() {
        this(DEFAULT_MAX_USERS);
    }
    public GameRoom(int maxUsers) {
        if(maxUsers < 2) {
            throw new IllegalArgumentException("A GameRoom must at least be for 2 users");
        }

        this.maxUsers = maxUsers;
        this.state = GameRoomState.WAITING;
    }

    public void addUser(SocketWrapper client, String nickname) throws GameLogicException {
        if (state != GameRoomState.WAITING) {
            throw new GameLogicException("This room is currently playing or restarting");
        } else if (handlers.size() >= maxUsers) {
            throw new GameLogicException("Room already full");
        } else if (hasNickname(nickname)) {
            throw new GameLogicException("Nickname taken");
        } else if (nickname.contains(",") || nickname.contains(":") || nickname.contains("&") || nickname.contains(";") || nickname.contains(" ")) {
            throw new GameLogicException("Nickname contains forbidden characters \",:&;[SPACE]\"");
        } else {
            GameClientHandler handler = new GameClientHandler(client, nickname);
            handler.startGameLoop(this);
            handlers.add(handler);
        }
    }

    public void broadcastMessage(ServerMessage type, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());
        sb.append(" ");
        for(Object arg : args) {
            sb.append(arg);
            sb.append(",");
        }

        if(sb.charAt(sb.length() - 1) == ',')
            sb.deleteCharAt(sb.length() - 1);

        for(GameClientHandler handler : handlers) {
            try {
                handler.sendMessage(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
                removeUser(handler);
            }
        }
    }

    public void removeUser(GameClientHandler handler) {
        handlers.remove(handler);
        broadcastMessage(ServerMessage.DISCONNECT, handler.getNickname());
    }

    public void broadcastUserList() {
        broadcastMessage(ServerMessage.USER_LIST, listNicknames());
    }

    public void broadcastReadyCount() {
        broadcastMessage(ServerMessage.READY, usersReady(), handlers.size());
    }

    public int usersReady() {
        int count = 0;
        for(GameClientHandler handler : handlers) {
            count += handler.isReady() ? 1 : 0;
        }
        return count;
    }

    public void broadcastIfGameStart() {
        if((handlers.size() == maxUsers || usersReady() == handlers.size()) && state == GameRoomState.WAITING) {
            state = GameRoomState.PLAYING;
            broadcastMessage(ServerMessage.GAME_START);
            startGameLoop();
        }
    }

    public String[] listNicknames() {
        String[] users = new String[handlers.size()];
        for(int i = 0; i < users.length; i++) {
            users[i] = handlers.get(i).getNickname();
        }

        return users;
    }

    public boolean hasNickname(String nickname) {
        for(String name : listNicknames()) {
            if(name.equals(nickname)) {
                return true;
            }
        }

        return false;
    }

    public GameClientHandler getUserByNickname(String nickname) {
        for(GameClientHandler handler : handlers) {
            if(handler.getNickname().equals(nickname))
                return handler;
        }

        return null;
    }

    public int userCount() {
        return handlers.size();
    }

    public int maxUsers() {
        return maxUsers;
    }

    public void startGameLoop() {
        new Thread(() -> {
            int round = 0;
            while (true) {
                if(round >= ROUND_COUNT) {
                    break;
                }else if(handlers.isEmpty()) {
                    System.err.println("Room was empty when starting round, closing room!");
                    break;
                }

                for(GameClientHandler handler : handlers)
                    handler.resetForNextRound();

                Util.sleep(WAIT_FOR_ACTIVITY,0);
                handleDiceRoll();

                Util.sleep(WAIT_FOR_ACTIVITY, 0);

                long puzzleUntil = startPuzzle();
                while (System.currentTimeMillis() < (puzzleUntil + 2000)) {
                    boolean stillWaiting = false;
                    for(GameClientHandler handler : handlers)
                        if(!handler.didFinnishPuzzle())
                            stillWaiting = true;

                    if(!stillWaiting)
                        break;
                    Util.sleep(0, 50);
                }

                sendPlacements(puzzleUntil - PUZZLE_DURATION);

                Util.sleep(10, 0);

                if(++round == ROUND_COUNT) {
                    broadcastMessage(ServerMessage.END_GAME);
                }else {
                    broadcastMessage(ServerMessage.END_SCOREBOARD);
                }

            }
            resetRoom();

            //Round end
        }).start();
    }

    private void sendPlacements(long puzzleStart) {
        assignPoints();
        Collections.sort(handlers, Collections.reverseOrder());
        String[] foo = new String[handlers.size()];

        for(int i = 0; i < handlers.size(); i++) {
            foo[i] = String.format("%s:%d:%d:%b:%d",
                    handlers.get(i).getNickname(),
                    (i + 1),
                    handlers.get(i).getPoints(),
                    handlers.get(i).didFinnishPuzzle(),
                    handlers.get(i).getPuzzleFinishedAt() - puzzleStart);
        }

        broadcastMessage(ServerMessage.PLACEMENTS, foo);
    }

    private long startPuzzle() {
        long finishUntil = System.currentTimeMillis() + PUZZLE_DURATION;
        broadcastMessage(ServerMessage.BEGIN_PUZZLE, finishUntil);
        return finishUntil;
    }

    private void handleDiceRoll() {
        broadcastMessage(ServerMessage.PLAY_DICE_ANIMATION, (new Random().nextInt(7 - 1) + 1));
        Util.sleep(8, 0);
        broadcastMessage(ServerMessage.END_DICE_ANIMATION);
    }

    private void resetRoom() {
        state = GameRoomState.RESTARTING;
        for(GameClientHandler handler : handlers)
            handler.close();
        handlers.clear();
        System.out.println("Room reset done!");
        state = GameRoomState.WAITING;
    }

    private void assignPoints() {
        ArrayList<GameClientHandler> toAssign = new ArrayList<>(handlers);

        for(GameClientHandler handler : handlers) {
            if(!handler.didFinnishPuzzle()) {
                toAssign.remove(handler);
                handler.addPoints(1);
            }
        }

        int c = 0;
        while (!toAssign.isEmpty()) {
            GameClientHandler handler = getHandlerFinishedFirst(toAssign);
            toAssign.remove(handler);
            handler.addPoints(handlers.size() - c);
            c++;
        }
    }

    private GameClientHandler getHandlerFinishedFirst(ArrayList<GameClientHandler> h) {
        GameClientHandler res = null;

        for(GameClientHandler handler : h)
            if(res == null || handler.getPuzzleFinishedAt() < res.getPuzzleFinishedAt())
                res = handler;

        return res;
    }

    public GameRoomState currentState() {
        return state;
    }
}
