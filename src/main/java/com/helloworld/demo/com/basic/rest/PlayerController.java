package com.helloworld.demo.com.basic.rest;

import java.io.ObjectInputFilter.Status;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        trivia = new TriviaGame(); // init trivia
    }

    @GetMapping
    public String getInfo() {
        return "Welcome!\n"
                + "To play this epic trivia game:\n"
                + "send GET request to `/play?name=<name>` , to start playing, \n"
                + "curl http://localhost:8080/play?name=<name>\n"
                + "(where <name> is your decided nickname).\n\n"
                + "Answer questions with POST request to /play with parameters name & answer,\n"
                + "curl -X POST -d \"name=<name>&answer=<x>\" http://localhost:8080/play\n"
                + "(where <name> is the nickname you gave before, and <x> is the number of your answer).\n\n"
                + "When you reach " + MAX_SCORE
                + " points, you win! Get three wrong answers and you'll have to start over...\n";
    }

    private List<Player> players = new ArrayList<>();
    private List<String> names = new ArrayList<>(); // cba to implement comparable
    private final int MAX_SCORE = 15;

    @GetMapping("play")
    public ResponseEntity<String> getPlayer(
            @RequestParam(value = "name", required = false) String name) {

        // bad parameters
        if (name == null) {
            return new ResponseEntity<>(getInfo(), HttpStatus.OK);
        }

        Player player = new Player(name);

        // if player doesn't exist, add to players
        if (!names.contains(player.getName())) {
            players.add(player);
            names.add(name);
        }

        return new ResponseEntity<>(respondToPlayer(player, StatusMessage.WELCOME), HttpStatus.OK);
    }

    @PostMapping("play")
    public ResponseEntity<String> postAnswer(
            @RequestParam String name,
            @RequestParam String answer) {
        // player doesn't yet exist
        if (!names.contains(name)) {
            return getPlayer(name);
        }

        // find player from the list
        Player player = findPlayer(name);
        if (player == null) {
            throw new NullPointerException("Didn't find player somehow");
        }

        StatusMessage result = StatusMessage.WRONG_ASNWER; // overwrite with CORRECT_ANSWER if so
        if (answerMatches(answer)) {
            result = StatusMessage.CORRECT_ANSWER;

            player.incrementPoints(trivia.getPreviousQuestionDifficulty());

            // player has won
            if (player.getPoints() >= MAX_SCORE) {
                result = StatusMessage.GAME_WON;
            }
        } else {
            player.incrementWrongAnswers();

            // player has lost
            if (player.getWrongAnswers() >= 3) {
                result = StatusMessage.GAME_LOST;
            }
        }

        return new ResponseEntity<>(respondToPlayer(player, result), HttpStatus.OK);
    }

    // helper method to find player from the list
    private Player findPlayer(String name) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) {
                return players.get(i);
            }
        }

        return null;
    }

    // see if answer is correct
    private boolean answerMatches(String answer) {
        String correctAnswer = trivia.getCurrentQuestion().getString("answer");

        // Get the JSONObject for the question index
        JSONObject question = trivia.getCurrentQuestion();

        // Get the question, options and answer from the JSONObject
        JSONArray options = question.getJSONArray("options");

        trivia.generateNewQuestion();

        // answer is correct
        if (options.get(Integer.parseInt(answer) - 1).equals(correctAnswer)) {

            return true;
        }

        return false;
    }

    // statusless version
    private String respondToPlayer(Player player) {
        return respondToPlayer(player, StatusMessage.UNDEF_STATUS);
    }

    // respond to player according to status and pose a question
    private String respondToPlayer(Player player, StatusMessage status) {
        StringBuilder response = new StringBuilder();

        // add status message
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
        response.append(" \n");

        response.append("Current score: " + player.getPoints() + "/" + MAX_SCORE + "\n\n");

        // Get the JSONObject for the question index
        JSONObject question = trivia.getCurrentQuestion();

        // Get the question, options and answer from the JSONObject
        String questionText = question.getString("question");
        JSONArray options = question.getJSONArray("options");

        // add question to response
        response.append("Question #" + trivia.getQuestionCounter() + ": " + questionText
                + " [" + trivia.getCurrentQuestionDifficulty() + "]\n");

        // add options to response
        int optIndex = 0;
        for (Object option : options) {
            response.append((++optIndex) + ". " + option.toString() + "\n");
        }

        return response.toString();
    }

    // resets the trivia game and players
    private void resetTrivia() {
        trivia.resetGame();

        for (Player player : players) {
            player.resetPlayer();
        }
    }
}
