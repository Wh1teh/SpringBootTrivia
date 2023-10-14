package com.helloworld.demo.com.basic.rest;

import java.io.ObjectInputFilter.Status;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helloworld.demo.com.basic.rest.Player.Difficulty;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.annotation.PostConstruct;

@RestController
public class PlayerController {
    private enum StatusMessage {
        UNDEF_STATUS,
        WRONG_ASNWER,
        CORRECT_ANSWER,
        GAME_WON,
        GAME_LOST,
        WELCOME
    };

    private TriviaGame trivia;

    @PostConstruct
    public void runThisToInit() {
        trivia = new TriviaGame();
    }

    @GetMapping
    public String getInfo() {
        return "Welcome!<br>"
                + "To play this epic trivia game:<br>"
                + "go to `/play?name=&lt;name&gt;` , to start playing, <br>"
                + "(where &lt;name&gt; is your decided nickname).<br><br>"
                + "Answer questions with /play?name=&lt;name&gt;&answer=&lt;x&gt;,<br>"
                + "(where &lt;name&gt; is the nickname you gave before, and &lt;x&gt; is the number of your answer).<br><br>"
                + "When you reach " + MAX_SCORE
                + " points, you win! Get three wrong answers and you'll have to start over...";
    }

    private List<Player> players = new ArrayList<>();
    private List<String> names = new ArrayList<>(); // cba to implement comparable
    private final int MAX_SCORE = 10;

    @GetMapping("play")
    public ResponseEntity<String> getPlayer(
            @RequestParam String name,
            @RequestParam(value = "answer", required = false) String answer) {

        if (answer != null) {
            return answerQuestion(name, answer);
        }

        Player player = new Player(name);
        if (!names.contains(player.getName())) {
            players.add(player);
            names.add(name);
        }

        return new ResponseEntity<>(respondToAnswer(player, StatusMessage.WELCOME), HttpStatus.OK);
    }

    public ResponseEntity<String> answerQuestion(String name, String answer) {
        if (!names.contains(name)) {
            return getPlayer(name, null);
        }

        Player player = findPlayer(name);
        if (player == null) {
            throw new NullPointerException("Didn't find player somehow");
        }

        int playerIndex = 0;
        for (String string : names) {
            if (name.equals(string))
                break;

            playerIndex++;
        }

        StatusMessage result = StatusMessage.WRONG_ASNWER;
        if (answerMatches(answer)) {
            result = StatusMessage.CORRECT_ANSWER;

            player.incrementPoints(Difficulty.EASY);

            if (player.getPoints() >= 10) {
                result = StatusMessage.GAME_WON;
            }
        } else {
            player.incrementWrongAnswers();
            if (player.getWrongAnswers() >= 3) {
                result = StatusMessage.GAME_LOST;
            }
        }

        return new ResponseEntity<>(respondToAnswer(players.get(playerIndex), result), HttpStatus.OK);
    }

    private Player findPlayer(String name) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) {
                return players.get(i);
            }
        }

        return null;
    }

    private boolean answerMatches(String answer) {
        String correctAnswer = trivia.getCurrentQuestion().getString("answer");

        // Get the JSONObject for the question index
        JSONObject question = trivia.getCurrentQuestion();

        // Get the question, options and answer from the JSONObject
        JSONArray options = question.getJSONArray("options");

        System.out
                .println("player: " + options.get(Integer.parseInt(answer) - 1) + ", correct: " + correctAnswer + " ?= "
                        + options.get(Integer.parseInt(answer) - 1).equals(correctAnswer));

        if (options.get(Integer.parseInt(answer) - 1).equals(correctAnswer)) {
            trivia.generateNewQuestion();
            return true;
        }

        trivia.generateNewQuestion();
        return false;
    }

    private String respondToAnswer(Player player) {
        return respondToAnswer(player, StatusMessage.UNDEF_STATUS);
    }

    private String respondToAnswer(Player player, StatusMessage status) {
        StringBuilder response = new StringBuilder();

        switch (status) {
            case WELCOME:
                response.append("Greetings, " + player.getName() + "!");
                break;

            case CORRECT_ANSWER:
                response.append("Correct answer, " + player.getName() + "!");
                break;

            case WRONG_ASNWER:
                response.append("Wrong answer, " + player.getName() + "!");
                break;

            case GAME_WON:
                response.append("Correct answer, " + player.getName() + "! You won the game!");
                resetTrivia();
                break;

            case GAME_LOST:
                response.append("Wrong answer, " + player.getName() + "! 3 strikes, you'll have to start over!");
                // resetTrivia();
                player.resetPlayer();
                break;

            default:
                break;
        }
        response.append(" <br>");

        response.append("Current score: " + player.getPoints() + "/" + MAX_SCORE + "<br><br>");

        // Get the JSONObject for the question index
        JSONObject question = trivia.getCurrentQuestion();

        // Get the question, options and answer from the JSONObject
        String questionText = question.getString("question");
        JSONArray options = question.getJSONArray("options");

        // Print the question, options, and answer
        response.append("Question #" + trivia.getQuestionCounter() + ": " + questionText + "<br>");

        int optIndex = 0;
        for (Object option : options) {
            response.append((++optIndex) + ". " + option.toString() + "<br>");
        }

        return response.toString();
    }

    private void resetTrivia() {
        trivia.resetGame();

        for (Player player : players) {
            player.resetPlayer();
        }
    }
}
