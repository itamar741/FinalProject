package model.managers;

import model.ChatSession;
import model.ChatMessage;
import model.ChatRequest;
import model.ChatUserStatus;
import model.Session;
import model.Employee;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages the chat system - active chats, request queues, and user statuses.
 * Implements the Queue Pattern (Producer-Consumer) for managing chat request queues.
 * Uses FIFO (First In First Out) ordering to ensure fair matching between users from different branches.
 * Thread-safe implementation using ConcurrentHashMap and LinkedBlockingQueue.
 * 
 * @author FinalProject
 */
public class ChatManager {
    
    /** Active chat sessions: chatId -> ChatSession */
    private Map<String, ChatSession> activeChats;
    
    /** Request queue (FIFO) for matching users from different branches */
    private Queue<ChatRequest> chatQueue;
    
    /** User statuses: username -> ChatUserStatus */
    private Map<String, ChatUserStatus> userStatus;
    
    /** Pending requests: requestId -> ChatRequest */
    private Map<String, ChatRequest> pendingRequests;
    
    /** Chat messages: chatId -> List<ChatMessage> */
    private Map<String, List<ChatMessage>> chatMessages;
    
    /** User to chat mapping: username -> chatId (for finding user's active chat) */
    private Map<String, String> userToChat;
    
    /** Waiting requests by branch: branchId -> Queue<ChatRequest> */
    private Map<String, Queue<ChatRequest>> waitingForUser;
    
    private SessionManager sessionManager;
    private int chatIdCounter;
    
    /**
     * Constructs a new ChatManager.
     * 
     * @param sessionManager the session manager for accessing active sessions
     */
    public ChatManager(SessionManager sessionManager) {
        this.activeChats = new ConcurrentHashMap<>();
        this.chatQueue = new LinkedBlockingQueue<>();
        this.userStatus = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.chatMessages = new ConcurrentHashMap<>();
        this.userToChat = new ConcurrentHashMap<>();
        this.waitingForUser = new ConcurrentHashMap<>();
        this.sessionManager = sessionManager;
        this.chatIdCounter = 1;
    }
    
    /**
     * User requests a chat - added to queue.
     * Attempts immediate matching; if no match found, request is queued for the requester's branch.
     * 
     * @param requesterUsername the username of the user requesting the chat
     * @param requesterBranchId the branch ID of the requester
     * @return "OK;MATCHED;chatId;user1;user2" if matched, "OK;QUEUE;requestId" if queued, "ERROR;..." on failure
     */
    public String requestChat(String requesterUsername, String requesterBranchId) {
        // Check if user is already in a chat or in queue
        ChatUserStatus currentStatus = userStatus.get(requesterUsername);
        if (currentStatus == ChatUserStatus.IN_CHAT) {
            return "ERROR;User is already in a chat";
        }
        if (currentStatus == ChatUserStatus.IN_QUEUE) {
            return "ERROR;User is already in queue";
        }
        
        // Create new request
        String requestId = "REQ_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatRequest request = new ChatRequest(requestId, requesterUsername, requesterBranchId);
        
        // Add to queue
        chatQueue.offer(request);
        pendingRequests.put(requestId, request);
        userStatus.put(requesterUsername, ChatUserStatus.IN_QUEUE);
        
        // Attempt immediate matching
        String matchResult = matchUsers();
        if (matchResult != null && !matchResult.startsWith("ERROR")) {
            return matchResult; // Match found
        }
        
        // If no match found, save request to branch queue
        waitingForUser.computeIfAbsent(requesterBranchId, k -> new LinkedBlockingQueue<>()).offer(request);
        
        return "OK;QUEUE;" + requestId; // Added to queue
    }
    
    /**
     * Matches users in queue (FIFO, only from different branches).
     * Called automatically after each new request.
     * Implements FIFO matching algorithm: takes first pending request, finds available user from different branch.
     * 
     * @return "OK;MATCHED;chatId;user1;user2" if match found, null if no match available
     */
    public String matchUsers() {
        if (chatQueue.isEmpty()) {
            return null; // No requests in queue
        }
        
        // Find first pending request in queue (FIFO)
        ChatRequest req1 = null;
        for (ChatRequest req : chatQueue) {
            if (req.getStatus() == ChatRequest.RequestStatus.PENDING) {
                req1 = req;
                break;
            }
        }
        
        if (req1 == null) {
            return null; // No pending requests
        }
        
        // Find available user from different branch
        String matchedUsername = null;
        String matchedBranchId = null;
        
        Map<String, Session> allSessions = sessionManager.getAllActiveSessions();
        for (Session session : allSessions.values()) {
            String username = session.getUsername();
            String branchId = session.getBranchId();
            
            // Only from different branch and available
            if (!branchId.equals(req1.getRequesterBranchId()) && 
                getUserStatus(username) == ChatUserStatus.AVAILABLE) {
                matchedUsername = username;
                matchedBranchId = branchId;
                break;
            }
        }
        
        if (matchedUsername == null) {
            return null; // No available user from different branch found
        }
        
        // Create new chat
        String chatId = "CHAT_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatSession session = new ChatSession(chatId, req1.getRequesterUsername(), matchedUsername);
        activeChats.put(chatId, session);
        
        // Update user statuses
        userStatus.put(req1.getRequesterUsername(), ChatUserStatus.IN_CHAT);
        userStatus.put(matchedUsername, ChatUserStatus.IN_CHAT);
        userToChat.put(req1.getRequesterUsername(), chatId);
        userToChat.put(matchedUsername, chatId);
        
        // Update request status
        req1.setStatus(ChatRequest.RequestStatus.MATCHED);
        
        // Remove from queue
        chatQueue.remove(req1);
        pendingRequests.remove(req1.getRequestId());
        
        // Remove from branch queue
        Queue<ChatRequest> branchQueue = waitingForUser.get(req1.getRequesterBranchId());
        if (branchQueue != null) {
            branchQueue.remove(req1);
            if (branchQueue.isEmpty()) {
                waitingForUser.remove(req1.getRequesterBranchId());
            }
        }
        
        // Create empty message list
        chatMessages.put(chatId, new ArrayList<>());
        
        // System message
        ChatMessage systemMsg = new ChatMessage(chatId, "SYSTEM", 
            "Chat started between " + req1.getRequesterUsername() + " and " + matchedUsername,
            ChatMessage.MessageType.SYSTEM);
        chatMessages.get(chatId).add(systemMsg);
        
        return "OK;MATCHED;" + chatId + ";" + req1.getRequesterUsername() + ";" + matchedUsername;
    }
    
    /**
     * Creates a new chat session directly (without queue).
     * Used for direct chat creation, e.g., when a manager accepts a request.
     * 
     * @param user1 the first participant's username
     * @param user2 the second participant's username
     * @return the chat ID of the created session
     */
    public String createChatSession(String user1, String user2) {
        String chatId = "CHAT_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatSession session = new ChatSession(chatId, user1, user2);
        activeChats.put(chatId, session);
        
        userStatus.put(user1, ChatUserStatus.IN_CHAT);
        userStatus.put(user2, ChatUserStatus.IN_CHAT);
        userToChat.put(user1, chatId);
        userToChat.put(user2, chatId);
        
        chatMessages.put(chatId, new ArrayList<>());
        
        return chatId;
    }
    
    /**
     * Adds a message to a chat.
     * Validates that the chat is active and the sender is a participant.
     * 
     * @param chatId the chat ID
     * @param sender the sender's username
     * @param message the message content
     * @throws IllegalArgumentException if chat not found, not active, or sender is not a participant
     */
    public void addMessage(String chatId, String sender, String message) {
        ChatSession session = activeChats.get(chatId);
        if (session == null || !session.isActive()) {
            throw new IllegalArgumentException("Chat not found or not active: " + chatId);
        }
        
        if (!session.hasParticipant(sender)) {
            throw new IllegalArgumentException("User " + sender + " is not a participant in chat " + chatId);
        }
        
        ChatMessage chatMessage = new ChatMessage(chatId, sender, message, ChatMessage.MessageType.TEXT);
        chatMessages.get(chatId).add(chatMessage);
    }
    
    /**
     * Ends a chat session.
     * Updates user statuses to AVAILABLE and attempts to match waiting users.
     * 
     * @param chatId the chat ID to end
     */
    public void endChat(String chatId) {
        ChatSession session = activeChats.get(chatId);
        if (session == null) {
            return;
        }
        
        session.end();
        
        // Update user statuses
        for (String username : session.getParticipants()) {
            userStatus.put(username, ChatUserStatus.AVAILABLE);
            userToChat.remove(username);
        }
        
        // Attempt new matching for users in queue
        matchUsers();
    }
    
    /**
     * User joins an existing chat.
     * Verifies that the user has permission to join (admin or manager role).
     * 
     * @param chatId the chat ID to join
     * @param username the username of the user joining
     * @throws IllegalArgumentException if chat not found, not active, or user does not have permission
     */
    public void joinChatAsManager(String chatId, String username) {
        ChatSession session = activeChats.get(chatId);
        if (session == null || !session.isActive()) {
            throw new IllegalArgumentException("Chat not found or not active: " + chatId);
        }
        
        // Verify user has permission to join (admin or manager)
        Session userSession = sessionManager.getSessionByUsername(username);
        if (userSession == null) {
            throw new IllegalArgumentException("User not logged in: " + username);
        }
        
        String role = userSession.getRole();
        if (!"admin".equals(role) && !"manager".equals(role)) {
            throw new IllegalArgumentException("User does not have permission to join existing chats: " + username);
        }
        
        // Add user to chat
        session.addParticipant(username);
        userStatus.put(username, ChatUserStatus.IN_CHAT);
        userToChat.put(username, chatId);
        
        // System message
        String roleLabel = "admin".equals(role) ? "Admin" : "Shift manager";
        ChatMessage systemMsg = new ChatMessage(chatId, "SYSTEM",
            roleLabel + " " + username + " joined the chat",
            ChatMessage.MessageType.SYSTEM);
        chatMessages.get(chatId).add(systemMsg);
    }
    
    /**
     * Gets the current status of a user in the chat system.
     * 
     * @param username the username to check
     * @return the user's chat status, or AVAILABLE if not found
     */
    public ChatUserStatus getUserStatus(String username) {
        return userStatus.getOrDefault(username, ChatUserStatus.AVAILABLE);
    }
    
    /**
     * Gets the chat history (all messages) for a specific chat.
     * Returns a defensive copy to prevent external modification.
     * 
     * @param chatId the chat ID
     * @return a list of chat messages, or empty list if chat not found
     */
    public List<ChatMessage> getChatHistory(String chatId) {
        List<ChatMessage> messages = chatMessages.get(chatId);
        if (messages == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messages);
    }
    
    /**
     * Gets the active chat session for a user.
     * 
     * @param username the username to check
     * @return the ChatSession if user is in a chat, null otherwise
     */
    public ChatSession getUserChat(String username) {
        String chatId = userToChat.get(username);
        if (chatId == null) {
            return null;
        }
        return activeChats.get(chatId);
    }
    
    /**
     * Cancels a chat request in the queue.
     * Removes the request from both the main queue and the branch queue.
     * 
     * @param username the username whose request to cancel
     * @return true if request was found and cancelled, false otherwise
     */
    public boolean cancelChatRequest(String username) {
        ChatUserStatus status = userStatus.get(username);
        if (status != ChatUserStatus.IN_QUEUE) {
            return false;
        }
        
        // Find the request in queue
        ChatRequest toRemove = null;
        for (ChatRequest req : chatQueue) {
            if (req.getRequesterUsername().equals(username) && 
                req.getStatus() == ChatRequest.RequestStatus.PENDING) {
                toRemove = req;
                break;
            }
        }
        
        if (toRemove != null) {
            chatQueue.remove(toRemove);
            pendingRequests.remove(toRemove.getRequestId());
            toRemove.setStatus(ChatRequest.RequestStatus.CANCELLED);
            
            // Remove from branch queue
            Queue<ChatRequest> branchQueue = waitingForUser.get(toRemove.getRequesterBranchId());
            if (branchQueue != null) {
                branchQueue.remove(toRemove);
                if (branchQueue.isEmpty()) {
                    waitingForUser.remove(toRemove.getRequesterBranchId());
                }
            }
            
            userStatus.put(username, ChatUserStatus.AVAILABLE);
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets all active chat sessions (for management/debugging).
     * Returns a defensive copy to prevent external modification.
     * 
     * @return a Map of chatId to ChatSession
     */
    public Map<String, ChatSession> getAllActiveChats() {
        return new HashMap<>(activeChats);
    }
    
    /**
     * Checks if a user can request a chat (is available).
     * 
     * @param username the username to check
     * @return true if user is available, false otherwise
     */
    public boolean canRequestChat(String username) {
        ChatUserStatus status = userStatus.getOrDefault(username, ChatUserStatus.AVAILABLE);
        return status == ChatUserStatus.AVAILABLE;
    }
    
    /**
     * Gets the list of waiting requests for a specific branch.
     * Used to notify available employees in a branch about pending requests from other branches.
     * 
     * @param branchId the branch ID to get requests for
     * @return a list of pending ChatRequests, or empty list if none
     */
    public List<ChatRequest> getWaitingRequestsForBranch(String branchId) {
        Queue<ChatRequest> queue = waitingForUser.get(branchId);
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }
        // Return list of pending requests (PENDING status)
        List<ChatRequest> result = new ArrayList<>();
        for (ChatRequest req : queue) {
            if (req.getStatus() == ChatRequest.RequestStatus.PENDING) {
                result.add(req);
            }
        }
        return result;
    }
    
    /**
     * User accepts a chat request.
     * The request is located in the queue of the requester's branch (not the acceptor's branch).
     * Searches all other branch queues to find the request.
     * 
     * @param acceptingUsername the username of the user accepting the request
     * @param requestId the request ID to accept
     * @return "OK;MATCHED;chatId;requester;acceptor" if successful, "ERROR;..." on failure
     */
    public String acceptChatRequest(String acceptingUsername, String requestId) {
        // Check that user is available
        if (getUserStatus(acceptingUsername) != ChatUserStatus.AVAILABLE) {
            return "ERROR;User is not available";
        }
        
        Session acceptingSession = sessionManager.getSessionByUsername(acceptingUsername);
        if (acceptingSession == null) {
            return "ERROR;User not logged in";
        }
        
        String acceptingBranchId = acceptingSession.getBranchId();
        
        // Search for request in all queues (request is in requester's branch queue)
        ChatRequest request = null;
        String requesterBranchId = null;
        
        for (Map.Entry<String, Queue<ChatRequest>> entry : waitingForUser.entrySet()) {
            String branchId = entry.getKey();
            // Only queues from other branches
            if (!branchId.equals(acceptingBranchId)) {
                Queue<ChatRequest> branchQueue = entry.getValue();
                for (ChatRequest req : branchQueue) {
                    if (req.getRequestId().equals(requestId) && 
                        req.getStatus() == ChatRequest.RequestStatus.PENDING) {
                        request = req;
                        requesterBranchId = branchId;
                        break;
                    }
                }
                if (request != null) break;
            }
        }
        
        if (request == null) {
            return "ERROR;Request not found or already processed";
        }
        
        // Check that requester is still in queue
        if (getUserStatus(request.getRequesterUsername()) != ChatUserStatus.IN_QUEUE) {
            // Requester is no longer in queue - remove the request
            Queue<ChatRequest> branchQueue = waitingForUser.get(requesterBranchId);
            if (branchQueue != null) {
                branchQueue.remove(request);
                if (branchQueue.isEmpty()) {
                    waitingForUser.remove(requesterBranchId);
                }
            }
            return "ERROR;Requester is no longer in queue";
        }
        
        // Create chat
        String chatId = "CHAT_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatSession session = new ChatSession(chatId, request.getRequesterUsername(), acceptingUsername);
        activeChats.put(chatId, session);
        
        // Update user statuses
        userStatus.put(request.getRequesterUsername(), ChatUserStatus.IN_CHAT);
        userStatus.put(acceptingUsername, ChatUserStatus.IN_CHAT);
        userToChat.put(request.getRequesterUsername(), chatId);
        userToChat.put(acceptingUsername, chatId);
        
        // Update request status
        request.setStatus(ChatRequest.RequestStatus.MATCHED);
        
        // Remove from queue
        chatQueue.remove(request);
        pendingRequests.remove(request.getRequestId());
        
        // Remove from branch queue
        Queue<ChatRequest> branchQueue = waitingForUser.get(requesterBranchId);
        if (branchQueue != null) {
            branchQueue.remove(request);
            if (branchQueue.isEmpty()) {
                waitingForUser.remove(requesterBranchId);
            }
        }
        
        // Create empty message list
        chatMessages.put(chatId, new ArrayList<>());
        
        // System message
        ChatMessage systemMsg = new ChatMessage(chatId, "SYSTEM",
            "Chat started between " + request.getRequesterUsername() + " and " + acceptingUsername,
            ChatMessage.MessageType.SYSTEM);
        chatMessages.get(chatId).add(systemMsg);
        
        return "OK;MATCHED;" + chatId + ";" + request.getRequesterUsername() + ";" + acceptingUsername;
    }
}
