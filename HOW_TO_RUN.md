# הוראות הרצה - מערכת ניהול סניפי בגדים

## דרישות מוקדמות
- Java JDK מותקן (גרסה 8 ומעלה)
- הפרויקט מקומפל (קבצי `.class` בתיקיית `out/production/FinalProject`)

## אופן הרצה

### ⚠️ חשוב: אם Java לא מזוהה ב-Command Prompt
אם מקבלים שגיאה `'java' is not recognized`, **השתמש בשיטה 2 (IntelliJ)** - זה הכי קל!
ראה `FIX_JAVA_PATH.md` לפתרונות נוספים.

### שיטה 1: הרצה דרך סקריפטים

#### Windows:
1. **הרצת השרת:**
   - לחץ כפול על `runServer.bat`
   - או פתח Command Prompt והרץ: `runServer.bat`
   - השרת יתחיל להאזין על פורט 5000

2. **הרצת הקליינט:**
   - פתח חלון Command Prompt נוסף
   - הרץ: `runClient.bat`
   - או לחץ כפול על `runClient.bat`

#### Linux/Mac:
1. **הרצת השרת:**
   ```bash
   chmod +x runServer.sh
   ./runServer.sh
   ```

2. **הרצת הקליינט:**
   ```bash
   chmod +x runClient.sh
   ./runClient.sh
   ```

### שיטה 2: הרצה דרך IntelliJ IDEA

#### השרת:
1. פתח את הפרויקט ב-IntelliJ IDEA
2. פתח את `src/server/ServerMain.java`
3. לחץ ימני על הקובץ → **Run 'ServerMain.main()'**
4. השרת יתחיל להאזין על פורט 5000

#### הקליינט:
1. פתח את `src/gui/LoginWindow.java`
2. לחץ ימני על הקובץ → **Run 'LoginWindow.main()'**
3. חלון ההתחברות יופיע

**חשוב:** ודא שהספריות מ-`libs/` מוגדרות ב-Project Structure → Libraries (לצורך ייצוא ל-Word)

### שיטה 3: הרצה ידנית מהטרמינל

#### השרת:
```bash
cd /path/to/FinalProject
java -cp "out/production/FinalProject;out/production/FinalProject/controller;out/production/FinalProject/model;out/production/FinalProject/server;out/production/FinalProject/storage" server.ServerMain
```

#### הקליינט (Windows):
```bash
cd /path/to/FinalProject
java -cp "out/production/FinalProject;out/production/FinalProject/controller;out/production/FinalProject/model;out/production/FinalProject/gui;out/production/FinalProject/storage;libs/poi-5.2.5.jar;libs/poi-ooxml-5.2.5.jar;libs/xmlbeans-5.2.0.jar;libs/commons-compress-1.24.0.jar;libs/commons-codec-1.16.0.jar" gui.LoginWindow
```

#### הקליינט (Linux/Mac):
```bash
cd /path/to/FinalProject
java -cp "out/production/FinalProject:out/production/FinalProject/controller:out/production/FinalProject/model:out/production/FinalProject/gui:out/production/FinalProject/storage:libs/poi-5.2.5.jar:libs/poi-ooxml-5.2.5.jar:libs/xmlbeans-5.2.0.jar:libs/commons-compress-1.24.0.jar:libs/commons-codec-1.16.0.jar" gui.LoginWindow
```

## סדר הפעולות

1. **תמיד התחל עם השרת** - השרת חייב לרוץ לפני הקליינט
2. **הרץ את הקליינט** - רק אחרי שהשרת רץ
3. **התחבר** - השתמש בשם משתמש וסיסמה קיימים

## פתרון בעיות

### השרת לא מתחיל:
- ודא שפורט 5000 פנוי
- בדוק שהקבצים מקומפלים ב-`out/production/FinalProject`
- ודא שיש Java מותקן: `java -version`

### הקליינט לא מתחבר:
- ודא שהשרת רץ
- בדוק שהפורט הוא 5000
- בדוק את חיבור הרשת

### שגיאת ClassNotFoundException:
- ודא שהספריות מ-`libs/` מוגדרות ב-classpath
- ראה `SETUP_WORD_EXPORT.md` להוראות מפורטות

## משתמשים לדוגמה

לאחר הרצה ראשונה, המערכת תיצור משתמש אדמין אוטומטית (אם לא קיים):
- **שם משתמש:** admin
- **סיסמה:** admin123

**חשוב:** שנה את הסיסמה לאחר התחברות ראשונה!
