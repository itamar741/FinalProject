ספריות נדרשות לייצוא ל-Word:

1. הורד את הספריות הבאות מ-Apache POI:
   - poi-5.2.5.jar
   - poi-ooxml-5.2.5.jar
   - poi-ooxml-lite-5.2.5.jar
   - xmlbeans-5.2.0.jar
   - commons-compress-1.24.0.jar
   - commons-codec-1.16.0.jar
   - log4j-api-2.20.0.jar
   - log4j-core-2.20.0.jar

2. הורד מ: https://poi.apache.org/download.html

3. העתק את כל ה-JAR files לתיקייה זו (libs)

4. ב-IntelliJ IDEA:
   - File -> Project Structure -> Libraries
   - לחץ על + -> Java
   - בחר את כל הקבצים בתיקיית libs
   - לחץ OK

או דרך ה-.iml file - הוסף:
<orderEntry type="library" name="Apache POI" level="project">
  <CLASSES>
    <root url="jar://$MODULE_DIR$/libs/poi-5.2.5.jar!/" />
    <root url="jar://$MODULE_DIR$/libs/poi-ooxml-5.2.5.jar!/" />
    <!-- וכו' -->
  </CLASSES>
</orderEntry>

הערה: אם הספריות לא קיימות, המערכת תשתמש ב-HTML כחלופה (ניתן לפתוח ב-Word).
