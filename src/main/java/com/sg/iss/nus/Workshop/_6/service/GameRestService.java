package com.sg.iss.nus.Workshop._6.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sg.iss.nus.Workshop._6.model.BoardGame;

@Service
public class GameRestService {

    @Autowired
    private RedisTemplate<String, Object> template; // Inject RedisTemplate

    public List<BoardGame> getAllBoardGame() {
        // Retrieve all keys matching the pattern for board games
        Set<String> keys = template.keys("*"); // Use a specific pattern if needed, e.g., "boardgame:*"

        // Initialize the list to hold all board games
        List<BoardGame> boardGamesList = new ArrayList<>();

        // Loop through each key and retrieve the board game
        if (keys != null) {
            for (String key : keys) {
                Object boardGameObj = template.opsForValue().get(key);

                if (boardGameObj instanceof BoardGame) {
                    boardGamesList.add((BoardGame) boardGameObj);
                }
            }
        }

        return boardGamesList;
    }

    public String insertBoardGame(BoardGame boardGame) {
        // Generate a unique Redis key based on the game ID
        String redisKey = "boardgame:" + boardGame.getGid();

        // Save the object directly into Redis
        template.opsForValue().set(redisKey, boardGame);

        return redisKey; // Return the Redis key
    }

    public BoardGame getBoardGameById(String boardGameId) {
        // Generate the Redis key for the board game
        String redisKey = boardGameId;

        // Retrieve the object from Redis
        Object boardGameObj = template.opsForValue().get(redisKey);

        // Return null if not found
        if (boardGameObj == null) {
            return null;
        }

        // Cast and return the object as BoardGame
        return (BoardGame) boardGameObj;
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

        // Update or insert the board game in Redis
        template.opsForValue().set(redisKey, boardGame);

        return true; // Indicate success
    }
}