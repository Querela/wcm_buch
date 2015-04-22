wcm_buch
========

Wissens- und Content-Management - Buch-Gruppe

IDE-Setup für Java-Code
-----------------------

Das Java-Server-Projekt befindet sich im [Java Ordner](https://github.com/Querela/wcm_buch/tree/master/Java/WCMBookServer).
Da es sich hier um ein Java-Maven-Projekt handelt, kann es in den meisten Fällen ganz einfach über die *pom.xml* importiert werden. Falls dies nicht klappen sollte, fehlt eventuell die Maven-Erweiterung der IDE.

Nach dem Importieren sollte das Projekt über den Maven-Lifecycle **package** gebaut werden können. Dabei entstehen die folgenden Resourcen:

-   target/**WCMBookServer.jar**
  -   Dies ist die finale Jar, mit der der Server ganz einfach gestartet werden kann. Dies kann ganz einfach wie folgt geschehen: _"java -jar **WCMBookServer.jar**"_. Über *Crtl+C* oder *ENTER* wird der Server wieder beendet.
-   target/**WCMBookServer-javadoc.jar**
  -   JavaDoc-Dokumentation des Quellcodes.
-   target/**WCMBookServer-sources.jar**
  -   Der Quellcode.
-   target/**dependency-jars/*.jar**
  -   Dies sind die Projekt-Abhängigkeiten. Sie müssen sich als ganzes im selben Verzeichnis relativ zur **WCMBookServer.jar** befinden.

DNB-Daten (Parser, ElasticSearch)
---------------------------------

Um zu Büchern den Original-Titel zu finden, werden zusätzlich DNB-Daten genutzt. Diese können von der **[DNB-Website][2]** unter diesem **[Link][3]** heruntergeladen werden, mit dem Python-Script im [Parser-Ordner](https://github.com/Querela/wcm_buch/tree/master/Parser) zu JSON konvertiert und nach **[ElasticSearch][4]** importiert werden.

Editor-Setup für HTML/CSS/JavaScript
------------------------------------

Zum Website-Prototyping wird der freie Editor **[brackets.io][1]** empfohlen.

Wir haben dabei die folgenden Plug-Ins benutzt, um die Produktivität zu erhöhen:

-   Brackets Git
-   Brackets Outline List
-   Code Folding
-   Beautify
-   JavaScript & CSS CDN Suggestions
-   Autoprefixer
-   Emmet
-   CSSLint
-   HTMLHint
-   JSHint

zusätzlich noch empfohlen:

-   Minimap
-   Static Preview
-   Theseus for Brackets (_sollte am Anfang bei Nicht-Benutzung evtl. deaktiviert werden_)
-   Brackets Diagnostic Report
-   Show Git Branch for project
-   Window Title Tweak
-   Recent projects
-   Brackets New Project Creator
-   Brackets Extension - UTF8 converter
-   HTML Special Chars
-   Todo
-   Notes

Zusätzlich werden die folgenden Einstellungen empfohlen (anzupassen unter _"Debug" -> "Einstellungsdatei öffnen"_, mit _Copy & Paste_ am Anfang einfügen):

    "livedev.multibrowser": true,
    "closeBrackets": true,
    "highlightMatches": {
        "showToken": true
    },
    "debug.showErrorsInStatusBar": true,
    "theseus.enabled": false,
    "me.drewh.jsbeautify.on_save": false,
    "dragDropText": true,

Der Entwurf der Website ist unter [Github Design Ordner](https://github.com/Querela/wcm_buch/tree/website-design/Website/Design) zu finden.

[1]: http://brackets.io/    "brackets.io"
[2]: http://www.dnb.de/     "DNB"
[3]: http://datendienst.dnb.de/cgi-bin/mabit.pl?cmd=fetch&userID=opendata&pass=opendata&mabheft=DNBTitel.ttl.gz  "DNB-Daten-Export"
[4]: https://www.elastic.co/    "ElasticSearch"
