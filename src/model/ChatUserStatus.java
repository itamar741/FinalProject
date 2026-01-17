package model;

/**
 * מצב משתמש במערכת הצ'אט
 */
public enum ChatUserStatus {
    AVAILABLE,    // פנוי - יכול לקבל בקשות צ'אט
    IN_CHAT,      // בשיחה פעילה
    IN_QUEUE      // ממתין בתור
}
