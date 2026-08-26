package com.connectabroad.controller;

import com.connectabroad.dto.response.*;
import com.connectabroad.service.ConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<ConnectionResponse> sendRequest(
            Authentication authentication,
            @PathVariable("userId") Long targetUserId) {
        String userEmail = authentication.getName();
        ConnectionResponse response = connectionService.sendRequest(userEmail, targetUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{connectionId}/accept")
    public ResponseEntity<ConnectionResponse> acceptRequest(
            Authentication authentication,
            @PathVariable("connectionId") Long connectionId) {
        String userEmail = authentication.getName();
        ConnectionResponse response = connectionService.acceptRequest(userEmail, connectionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{connectionId}/reject")
    public ResponseEntity<ConnectionResponse> rejectRequest(
            Authentication authentication,
            @PathVariable("connectionId") Long connectionId) {
        String userEmail = authentication.getName();
        ConnectionResponse response = connectionService.rejectRequest(userEmail, connectionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{connectionId}/cancel")
    public ResponseEntity<ConnectionResponse> cancelRequest(
            Authentication authentication,
            @PathVariable("connectionId") Long connectionId) {
        String userEmail = authentication.getName();
        ConnectionResponse response = connectionService.cancelRequest(userEmail, connectionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{connectionId}")
    public ResponseEntity<ConnectionResponse> removeConnection(
            Authentication authentication,
            @PathVariable("connectionId") Long connectionId) {
        String userEmail = authentication.getName();
        ConnectionResponse response = connectionService.removeConnection(userEmail, connectionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<ConnectionRequestResponse>> getReceivedRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ConnectionRequestResponse> response = connectionService.getReceivedRequests(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<ConnectionRequestResponse>> getSentRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ConnectionRequestResponse> response = connectionService.getSentRequests(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConnectedUserResponse>> getMyConnections(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ConnectedUserResponse> response = connectionService.getMyConnections(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<ConnectionStatusResponse> getConnectionStatus(
            Authentication authentication,
            @PathVariable("userId") Long targetUserId) {
        String userEmail = authentication.getName();
        ConnectionStatusResponse response = connectionService.getConnectionStatus(userEmail, targetUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/count")
    public ResponseEntity<Map<String, Long>> getPendingReceivedCount(Authentication authentication) {
        String userEmail = authentication.getName();
        long count = connectionService.getPendingReceivedCount(userEmail);
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
}
