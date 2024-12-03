package com.sg.iss.nus.Workshop._6.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.iss.nus.Workshop._6.model.BoardGame;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class GameRestService {

    @Autowired
    private RedisTemplate<String, String> template; // Inject RedisTemplate

    RestTemplate restTemplate = new RestTemplate();

    String filePath = "game.json";

    public List<BoardGame> getAllBoardGame() throws IOException {

        String boardgameData = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonReader jReader = Json.createReader(new StringReader(boardgameData));
        JsonArray jArray = jReader.readArray();

        List<BoardGame> boardGamesList = new ArrayList<>();

        for (int i = 0; i < jArray.size(); i++) {
            JsonObject jsonObject = jArray.getJsonObject(i);

            BoardGame boardGame = new BoardGame();
            boardGame.setGameId(jsonObject.getInt("gid"));
            boardGame.setGameName(jsonObject.getString("name"));
            boardGame.setGameUrl(jsonObject.getString("url"));

            boardGamesList.add(boardGame);
        }
        return boardGamesList;
    }

    public String insertBoardGame(BoardGame boardGame) {

        // Generate a unique Redis key based on the game ID
        String redisKey = boardGame.getGameId().toString();

        try {
            // Serialize the BoardGame object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String boardGameJson = mapper.writeValueAsString(boardGame);

            // Save the serialized object into Redis
            template.opsForValue().set(redisKey, boardGameJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing board game object", e);
        }

        // Return the Redis key
        return redisKey;
    }

    public BoardGame getBoardGameById(Integer boardGameId) {
        // Generate the Redis key for the board game
        String redisKey = boardGameId.toString();

        // Retrieve the JSON string from Redis
        String boardGameJson = template.opsForValue().get(redisKey);

        // Return null if the board game is not found
        if (boardGameJson == null) {
            return null;
        }

        // Deserialize the JSON string into a BoardGame object
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(boardGameJson, BoardGame.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing board game JSON", e);
        }
    }

    public boolean updateBoardGame(Integer boardGameId, BoardGame boardGame, boolean upsert) {
        // Generate the Redis key for the board game
        String redisKey = boardGameId.toString();

        // Check if the board game exists in Redis
        boolean exists = template.hasKey(redisKey);

        // If it doesn't exist and upsert is false, return false
        if (!exists && !upsert) {
            return false;
        }

        try {
            // Serialize the BoardGame object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String boardGameJson = mapper.writeValueAsString(boardGame);

            // Update or insert the document in Redis
            template.opsForValue().set(redisKey, boardGameJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing board game object", e);
        }

        return true;
    }
}
