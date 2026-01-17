# פתרון בעיית Java לא מזוהה

## הבעיה
כשמנסים להריץ את `runServer.bat` או `runClient.bat`, מקבלים:
```
'java' is not recognized as an internal or external command
```

## פתרונות

### פתרון 1: הרצה דרך IntelliJ IDEA (הכי קל - מומלץ!)

1. **פתח את הפרויקט ב-IntelliJ IDEA**

2. **הרצת השרת:**
   - פתח את `src/server/ServerMain.java`
   - לחץ ימני על הקובץ
   - בחר **Run 'ServerMain.main()'**
   - השרת יתחיל להאזין על פורט 5000

3. **הרצת הקליינט:**
   - פתח חלון חדש של IntelliJ (או השתמש באותו חלון)
   - פתח את `src/gui/LoginWindow.java`
   - לחץ ימני על הקובץ
   - בחר **Run 'LoginWindow.main()'**
   - חלון ההתחברות יופיע

**יתרון:** IntelliJ משתמש ב-JDK שלו, לא צריך להגדיר PATH!

---

### פתרון 2: מציאת Java והוספה ל-PATH

#### שלב 1: מצא את Java
1. פתח את IntelliJ IDEA
2. File → Project Structure → Project
3. בדוק את "SDK" - שם כתוב הנתיב ל-JDK
4. לדוגמה: `C:\Program Files\JetBrains\jbr-17` או `C:\Program Files\Java\jdk-17`

#### שלב 2: הוסף ל-PATH
1. לחץ ימני על "This PC" → Properties
2. Advanced system settings
3. Environment Variables
4. ב-"System variables", מצא את `Path` ולחץ Edit
5. לחץ New והוסף את הנתיב ל-Java:
   - אם הנתיב הוא `C:\Program Files\Java\jdk-17`, הוסף: `C:\Program Files\Java\jdk-17\bin`
6. לחץ OK בכל החלונות
7. **סגור ופתח מחדש את Command Prompt**

#### שלב 3: בדיקה
פתח Command Prompt חדש והרץ:
```cmd
java -version
```
אם זה עובד, נסה שוב את `runServer.bat`

---

### פתרון 3: עדכון הסקריפטים עם נתיב מלא

אם מצאת את Java (למשל ב-`C:\Program Files\Java\jdk-17\bin\java.exe`):

1. פתח את `runServer.bat` בעורך טקסט
2. החלף את `java` בנתיב המלא:
   ```bat
   "C:\Program Files\Java\jdk-17\bin\java.exe" -cp ...
   ```
3. עשה אותו דבר ב-`runClient.bat`

---

### פתרון 4: התקנת Java (אם לא מותקן)

1. הורד Java JDK מ: https://adoptium.net/ (או Oracle)
2. התקן את Java
3. הוסף את `C:\Program Files\Java\jdk-XX\bin` ל-PATH (ראה פתרון 2)

---

## המלצה

**השתמש בפתרון 1 (IntelliJ)** - זה הכי פשוט ולא דורש הגדרות!
