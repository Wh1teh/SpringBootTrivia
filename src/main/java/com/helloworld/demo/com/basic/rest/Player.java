package com.helloworld.demo.com.basic.rest;

public class Player {
    private String name;
    private int points = 0;
    private int wrongAnswers = 0;

    private Difficulty[] exDifficulties;

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, Difficulty[] exDifficulties) {
        this.name = name;
        this.exDifficulties = new Difficulty[exDifficulties.length];
        this.exDifficulties = exDifficulties;
    }

    public static enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    };

    public void incrementPoints(Difficulty difficulty) {
        this.points += difficulty.ordinal() + 1;
    }

    public void incrementWrongAnswers() {
        ++this.wrongAnswers;
    }

    public void resetPlayer() {
        this.points = 0;
        this.wrongAnswers = 0;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return this.points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getWrongAnswers() {
        return this.wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

}
