package com.helloworld.demo.com.basic.rest;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class TriviaGame {
    private String[] difficulties = { "easy", "medium", "hard" };

    private JSONObject allQuestions;
    private JSONObject currentQuestion;
    private int questionCounter = 0;

    public TriviaGame() {
        try {
            // Read the JSON file content
            String content = new String(Files.readAllBytes(Paths.get("src/main/resources/trivia/questions.json")));

            // Convert the JSON content to a JSONObject
            this.allQuestions = new JSONObject(content);

            generateNewQuestion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getDifficulties() {
        return this.difficulties;
    }

    public void setDifficulties(String[] difficulties) {
        this.difficulties = difficulties;
    }

    public JSONObject getAllQuestions() {
        return this.allQuestions;
    }

    public void setAllQuestions(JSONObject allQuestions) {
        this.allQuestions = allQuestions;
    }

    public JSONObject getCurrentQuestion() {
        if (currentQuestion == null) {
            generateNewQuestion();
        }

        return this.currentQuestion;
    }

    public void setCurrentQuestion(JSONObject currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public int getQuestionCounter() {
        return this.questionCounter;
    }

    public void setQuestionCounter(int questionCounter) {
        this.questionCounter = questionCounter;
    }

    public void resetGame() {
        questionCounter = 0;
        generateNewQuestion();
    }

    public void generateNewQuestion() {
        try {
            // Randomly select difficulty (easy, medium, or hard)
            String difficulty = "\0";
            difficulty = difficulties[(int) (Math.random() * 3)];

            // Get the JSONArray for the corresponding difficulty
            JSONArray questions = allQuestions.getJSONArray(difficulty);

            // Get question at random index
            int index = (int) (Math.random() * questions.length());

            // Get the JSONObject for the question index
            currentQuestion = questions.getJSONObject(index);

            questionCounter++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
