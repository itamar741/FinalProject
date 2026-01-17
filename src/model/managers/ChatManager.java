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
 * מנהל מערכת הצ'אט - ניהול צ'אטים פעילים, תורים, ומצבי משתמשים
 * משתמש ב-Queue Pattern (Producer-Consumer) לניהול תור בקשות
 */
public class ChatManager {
    
    // צ'אטים פעילים: chatId -> ChatSession
    private Map<String, ChatSession> activeChats;
    
    // תור בקשות (FIFO)
    private Queue<ChatRequest> chatQueue;
    
    // מצב משתמשים: username -> ChatUserStatus
    private Map<String, ChatUserStatus> userStatus;
    
    // בקשות ממתינות: requestId -> ChatRequest
    private Map<String, ChatRequest> pendingRequests;
    
    // הודעות צ'אט: chatId -> List<ChatMessage>
    private Map<String, List<ChatMessage>> chatMessages;
    
    // username -> chatId (למציאת צ'אט של משתמש)
    private Map<String, String> userToChat;
    
    // בקשות ממתינות לפי סניף: branchId -> Queue<ChatRequest>
    private Map<String, Queue<ChatRequest>> waitingForUser;
    
    private SessionManager sessionManager;
    private EmployeeManager employeeManager;
    private int chatIdCounter;
    
    public ChatManager(SessionManager sessionManager, EmployeeManager employeeManager) {
        this.activeChats = new ConcurrentHashMap<>();
        this.chatQueue = new LinkedBlockingQueue<>();
        this.userStatus = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.chatMessages = new ConcurrentHashMap<>();
        this.userToChat = new ConcurrentHashMap<>();
        this.waitingForUser = new ConcurrentHashMap<>();
        this.sessionManager = sessionManager;
        this.employeeManager = employeeManager;
        this.chatIdCounter = 1;
    }
    
    /**
     * משתמש מבקש צ'אט - נוסף לתור
     */
    public String requestChat(String requesterUsername, String requesterBranchId) {
        // בדיקה אם המשתמש כבר בשיחה או בתור
        ChatUserStatus currentStatus = userStatus.get(requesterUsername);
        if (currentStatus == ChatUserStatus.IN_CHAT) {
            return "ERROR;User is already in a chat";
        }
        if (currentStatus == ChatUserStatus.IN_QUEUE) {
            return "ERROR;User is already in queue";
        }
        
        // יצירת בקשה חדשה
        String requestId = "REQ_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatRequest request = new ChatRequest(requestId, requesterUsername, requesterBranchId);
        
        // הוספה לתור
        chatQueue.offer(request);
        pendingRequests.put(requestId, request);
        userStatus.put(requesterUsername, ChatUserStatus.IN_QUEUE);
        
        // ניסיון התאמה מיידית
        String matchResult = matchUsers();
        if (matchResult != null && !matchResult.startsWith("ERROR")) {
            return matchResult; // נמצאה התאמה
        }
        
        // אם לא נמצאה התאמה, נשמור את הבקשה בתור של הסניף
        waitingForUser.computeIfAbsent(requesterBranchId, k -> new LinkedBlockingQueue<>()).offer(request);
        
        return "OK;QUEUE;" + requestId; // נוסף לתור
    }
    
    /**
     * התאמה בין משתמשים בתור (FIFO, רק מסניפים שונים)
     * נקרא אוטומטית אחרי כל בקשה חדשה
     */
    public String matchUsers() {
        if (chatQueue.isEmpty()) {
            return null; // אין בקשות בתור
        }
        
        // נסה למצוא בקשה ראשונה בתור (FIFO)
        ChatRequest req1 = null;
        for (ChatRequest req : chatQueue) {
            if (req.getStatus() == ChatRequest.RequestStatus.PENDING) {
                req1 = req;
                break;
            }
        }
        
        if (req1 == null) {
            return null; // אין בקשות ממתינות
        }
        
        // מציאת משתמש פנוי מסניף אחר
        String matchedUsername = null;
        String matchedBranchId = null;
        
        Map<String, Session> allSessions = sessionManager.getAllActiveSessions();
        for (Session session : allSessions.values()) {
            String username = session.getUsername();
            String branchId = session.getBranchId();
            
            // רק מסניף אחר ופנוי
            if (!branchId.equals(req1.getRequesterBranchId()) && 
                getUserStatus(username) == ChatUserStatus.AVAILABLE) {
                matchedUsername = username;
                matchedBranchId = branchId;
                break;
            }
        }
        
        if (matchedUsername == null) {
            return null; // לא נמצא משתמש פנוי מסניף אחר
        }
        
        // יצירת צ'אט חדש
        String chatId = "CHAT_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatSession session = new ChatSession(chatId, req1.getRequesterUsername(), matchedUsername);
        activeChats.put(chatId, session);
        
        // עדכון מצב משתמשים
        userStatus.put(req1.getRequesterUsername(), ChatUserStatus.IN_CHAT);
        userStatus.put(matchedUsername, ChatUserStatus.IN_CHAT);
        userToChat.put(req1.getRequesterUsername(), chatId);
        userToChat.put(matchedUsername, chatId);
        
        // עדכון סטטוס בקשה
        req1.setStatus(ChatRequest.RequestStatus.MATCHED);
        
        // הסרה מתור
        chatQueue.remove(req1);
        pendingRequests.remove(req1.getRequestId());
        
        // הסרה מתור הסניף
        Queue<ChatRequest> branchQueue = waitingForUser.get(req1.getRequesterBranchId());
        if (branchQueue != null) {
            branchQueue.remove(req1);
            if (branchQueue.isEmpty()) {
                waitingForUser.remove(req1.getRequesterBranchId());
            }
        }
        
        // יצירת רשימת הודעות ריקה
        chatMessages.put(chatId, new ArrayList<>());
        
        // הודעת מערכת
        ChatMessage systemMsg = new ChatMessage(chatId, "SYSTEM", 
            "צ'אט התחיל בין " + req1.getRequesterUsername() + " ו-" + matchedUsername,
            ChatMessage.MessageType.SYSTEM);
        chatMessages.get(chatId).add(systemMsg);
        
        return "OK;MATCHED;" + chatId + ";" + req1.getRequesterUsername() + ";" + matchedUsername;
    }
    
    /**
     * יצירת צ'אט חדש (ישירות, ללא תור)
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
     * הוספת הודעה לצ'אט
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
     * סיום צ'אט
     */
    public void endChat(String chatId) {
        ChatSession session = activeChats.get(chatId);
        if (session == null) {
            return;
        }
        
        session.end();
        
        // עדכון מצב משתמשים
        for (String username : session.getParticipants()) {
            userStatus.put(username, ChatUserStatus.AVAILABLE);
            userToChat.remove(username);
        }
        
        // ניסיון התאמה חדשה למשתמשים בתור
        matchUsers();
    }
    
    /**
     * מנהל מצטרף לצ'אט
     */
    public void joinChatAsManager(String chatId, String managerUsername) {
        ChatSession session = activeChats.get(chatId);
        if (session == null || !session.isActive()) {
            throw new IllegalArgumentException("Chat not found or not active: " + chatId);
        }
        
        // בדיקה שהמשתמש הוא מנהל
        try {
            Session userSession = sessionManager.getSessionByUsername(managerUsername);
            if (userSession == null) {
                throw new IllegalArgumentException("User not logged in: " + managerUsername);
            }
            
            Employee employee = employeeManager.getEmployee(userSession.getEmployeeNumber());
            if (!"manager".equals(employee.getRole())) {
                throw new IllegalArgumentException("User is not a manager: " + managerUsername);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot verify manager role: " + e.getMessage());
        }
        
        // הוספת מנהל לצ'אט
        session.addParticipant(managerUsername);
        userStatus.put(managerUsername, ChatUserStatus.IN_CHAT);
        userToChat.put(managerUsername, chatId);
        
        // הודעת מערכת
        ChatMessage systemMsg = new ChatMessage(chatId, "SYSTEM",
            "מנהל משמרת " + managerUsername + " הצטרף לצ'אט",
            ChatMessage.MessageType.SYSTEM);
        chatMessages.get(chatId).add(systemMsg);
    }
    
    /**
     * בדיקת מצב משתמש
     */
    public ChatUserStatus getUserStatus(String username) {
        return userStatus.getOrDefault(username, ChatUserStatus.AVAILABLE);
    }
    
    /**
     * רשימת משתמשים פנויים מסניף אחר
     */
    public List<String> getAvailableUsers(String excludeBranchId) {
        List<String> available = new ArrayList<>();
        
        // קבלת כל הסשנים הפעילים
        Map<String, Session> allSessions = sessionManager.getAllActiveSessions();
        
        for (Session session : allSessions.values()) {
            String username = session.getUsername();
            String branchId = session.getBranchId();
            
            // רק מסניף אחר ופנוי
            if (!branchId.equals(excludeBranchId) && 
                getUserStatus(username) == ChatUserStatus.AVAILABLE) {
                available.add(username);
            }
        }
        
        return available;
    }
    
    /**
     * היסטוריית צ'אט
     */
    public List<ChatMessage> getChatHistory(String chatId) {
        List<ChatMessage> messages = chatMessages.get(chatId);
        if (messages == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messages);
    }
    
    /**
     * קבלת צ'אט של משתמש
     */
    public ChatSession getUserChat(String username) {
        String chatId = userToChat.get(username);
        if (chatId == null) {
            return null;
        }
        return activeChats.get(chatId);
    }
    
    /**
     * ביטול בקשה בתור
     */
    public boolean cancelChatRequest(String username) {
        ChatUserStatus status = userStatus.get(username);
        if (status != ChatUserStatus.IN_QUEUE) {
            return false;
        }
        
        // מציאת הבקשה בתור
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
            
            // הסרה מתור הסניף
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
     * קבלת כל הצ'אטים הפעילים (לצורך ניהול/דיבוג)
     */
    public Map<String, ChatSession> getAllActiveChats() {
        return new HashMap<>(activeChats);
    }
    
    /**
     * בדיקה אם משתמש יכול לבקש צ'אט
     */
    public boolean canRequestChat(String username) {
        ChatUserStatus status = userStatus.getOrDefault(username, ChatUserStatus.AVAILABLE);
        return status == ChatUserStatus.AVAILABLE;
    }
    
    /**
     * קבלת רשימת בקשות ממתינות לסניף מסוים
     */
    public List<ChatRequest> getWaitingRequestsForBranch(String branchId) {
        Queue<ChatRequest> queue = waitingForUser.get(branchId);
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }
        // מחזירים רשימה של בקשות ממתינות (PENDING)
        List<ChatRequest> result = new ArrayList<>();
        for (ChatRequest req : queue) {
            if (req.getStatus() == ChatRequest.RequestStatus.PENDING) {
                result.add(req);
            }
        }
        return result;
    }
    
    /**
     * משתמש מאשר בקשה לצ'אט
     * הבקשה נמצאת בתור של הסניף של המבקש (לא של המאשר)
     */
    public String acceptChatRequest(String acceptingUsername, String requestId) {
        // בדיקה שהמשתמש פנוי
        if (getUserStatus(acceptingUsername) != ChatUserStatus.AVAILABLE) {
            return "ERROR;User is not available";
        }
        
        Session acceptingSession = sessionManager.getSessionByUsername(acceptingUsername);
        if (acceptingSession == null) {
            return "ERROR;User not logged in";
        }
        
        String acceptingBranchId = acceptingSession.getBranchId();
        
        // חיפוש הבקשה בכל התורים (הבקשה נמצאת בתור של הסניף של המבקש)
        ChatRequest request = null;
        String requesterBranchId = null;
        
        for (Map.Entry<String, Queue<ChatRequest>> entry : waitingForUser.entrySet()) {
            String branchId = entry.getKey();
            // רק תורים של סניפים אחרים
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
        
        // בדיקה שהמבקש עדיין בתור
        if (getUserStatus(request.getRequesterUsername()) != ChatUserStatus.IN_QUEUE) {
            // המבקש כבר לא בתור - נסיר את הבקשה
            Queue<ChatRequest> branchQueue = waitingForUser.get(requesterBranchId);
            if (branchQueue != null) {
                branchQueue.remove(request);
                if (branchQueue.isEmpty()) {
                    waitingForUser.remove(requesterBranchId);
                }
            }
            return "ERROR;Requester is no longer in queue";
        }
        
        // יצירת צ'אט
        String chatId = "CHAT_" + System.currentTimeMillis() + "_" + chatIdCounter++;
        ChatSession session = new ChatSession(chatId, request.getRequesterUsername(), acceptingUsername);
        activeChats.put(chatId, session);
        
        // עדכון מצב משתמשים
        userStatus.put(request.getRequesterUsername(), ChatUserStatus.IN_CHAT);
        userStatus.put(acceptingUsername, ChatUserStatus.IN_CHAT);
        userToChat.put(request.getRequesterUsername(), chatId);
        userToChat.put(acceptingUsername, chatId);
        
        // עדכון סטטוס בקשה
        request.setStatus(ChatRequest.RequestStatus.MATCHED);
        
        // הסרה מתור
        chatQueue.remove(request);
        pendingRequests.remove(request.getRequestId());
        
        // הסרה מתור הסניף
        Queue<ChatRequest> branchQueue = waitingForUser.get(requesterBranchId);
        if (branchQueue != null) {
            branchQueue.remove(request);
            if (branchQueue.isEmpty()) {
                waitingForUser.remove(requesterBranchId);
            }
        }
        
        // יצירת רשימת הודעות ריקה
        chatMessages.put(chatId, new ArrayList<>());
        
        // הודעת מערכת
        ChatMessage systemMsg = new ChatMessage(chatId, "SYSTEM",
            "צ'אט התחיל בין " + request.getRequesterUsername() + " ו-" + acceptingUsername,
            ChatMessage.MessageType.SYSTEM);
        chatMessages.get(chatId).add(systemMsg);
        
        return "OK;MATCHED;" + chatId + ";" + request.getRequesterUsername() + ";" + acceptingUsername;
    }
    
    /**
     * מציאת הסניף השני (יש רק שני סניפים)
     */
    private String getOtherBranchId(String currentBranchId) {
        Map<String, Session> allSessions = sessionManager.getAllActiveSessions();
        for (Session session : allSessions.values()) {
            String branchId = session.getBranchId();
            if (!branchId.equals(currentBranchId)) {
                return branchId;
            }
        }
        return null;
    }
}
