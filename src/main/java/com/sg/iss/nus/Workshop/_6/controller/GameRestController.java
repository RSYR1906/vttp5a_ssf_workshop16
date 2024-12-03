package com.sg.iss.nus.Workshop._6.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sg.iss.nus.Workshop._6.model.BoardGame;
import com.sg.iss.nus.Workshop._6.service.GameRestService;

@RestController
@RequestMapping("/")
public class GameRestController {

    @Autowired
    GameRestService gameRestService;

    @GetMapping("/api/allBoardGames")
    public ResponseEntity<List<BoardGame>> getAllBoardGame() throws IOException {
        List<BoardGame> boardgameList = gameRestService.getAllBoardGame();

        return ResponseEntity.ok().body(boardgameList);
    }

    @GetMapping("/api/boardgame/{id}")
    public ResponseEntity<?> getBoardGame(@PathVariable("id") String id) {
        // Fetch the board game from the service
        BoardGame boardGame = gameRestService.getBoardGameById(id);

        // If the board game is not found, return 404 status with an error message
        if (boardGame == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 404);
            errorResponse.put("error", "Board game not found");
            errorResponse.put("message", "No board game found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // If found, return the board game with a 200 OK status
        return ResponseEntity.ok(boardGame);
    }

    @PostMapping("/api/boardgames")
    public ResponseEntity<Map<String, Object>> insertBoardGames(@RequestBody List<BoardGame> boardGames) {
        Map<String, Object> response = new HashMap<>();
        List<String> insertedKeys = new ArrayList<>();

        try {
            // Validate input
            if (boardGames == null || boardGames.isEmpty()) {
                response.put("error", "Invalid input");
                response.put("message", "No board games provided for insertion");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Insert each board game
            for (BoardGame boardGame : boardGames) {
                if (boardGame.getGid() != null && boardGame.getName() != null) {
                    String redisKey = gameRestService.insertBoardGame(boardGame);
                    insertedKeys.add(redisKey);
                } else {
                    response.put("skipped", "Some board games were skipped due to missing Game ID or Game Name");
                }
            }

            // Prepare response
            response.put("insert_count", insertedKeys.size());
            response.put("inserted_ids", insertedKeys);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to insert board games");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/api/boardgame/{id}")
    public ResponseEntity<Map<String, Object>> updateBoardGame(
            @PathVariable("id") Integer id,
            @RequestBody BoardGame boardGame,
            @RequestParam(name = "upsert", defaultValue = "false") boolean upsert) throws JsonProcessingException {

        // Try updating the board game
        boolean isUpdated = gameRestService.updateBoardGame(id, boardGame, upsert);

        // If the board game does not exist and upsert is false
        if (!isUpdated && !upsert) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 400);
            errorResponse.put("error", "Board game not found");
            errorResponse.put("message", "No board game found with ID: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Prepare the response payload
        Map<String, Object> response = new HashMap<>();
        response.put("update_count", 1);
        response.put("id", id);
        response.put("message", "Update successful");

        // Return 200 OK status
        return ResponseEntity.ok(response);
    }
}
