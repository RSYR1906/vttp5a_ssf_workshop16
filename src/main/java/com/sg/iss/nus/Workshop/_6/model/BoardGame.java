package com.sg.iss.nus.Workshop._6.model;

public class BoardGame {

    private Integer gameId;

    private String gameName;

    private String gameUrl;

    public BoardGame() {
    }

    public BoardGame(Integer gameId, String gameName, String gameUrl) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameUrl = gameUrl;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    @Override
    public String toString() {
        return gameId + "," + gameName + "," + gameUrl;
    }

}
