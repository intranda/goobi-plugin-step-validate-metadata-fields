---
title: Validierung von Metadatenfeldern
identifier: intranda_step_validateMetadataFields
description: Step Plugin für die Validierung von Metadatenfeldern in METS-Dateien
published: true
keywords:
    - Goobi workflow
    - Plugin
    - Step Plugin
    - Validierung
    - Metadaten
---

## Einführung
Dieses Plugin ermöglicht die automatische Validierung von Metadatenfeldern innerhalb von METS-Dateien in Goobi workflow. Es prüft konfigurierbare Regeln wie Pflichtfelder, reguläre Ausdrücke, erlaubte Inhalte und Mindestwortzahlen. Bei Validierungsfehlern wird der Arbeitsschritt auf den Status Fehler gesetzt und eine detaillierte Fehlermeldung ins Vorgangslog geschrieben.

## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/plugin-step-validate-metadata-fields-base.jar
/opt/digiverso/goobi/config/plugin_intranda_step_validateMetadataFields.xml
```

Nach der Installation des Plugins kann dieses innerhalb des Workflows für die jeweiligen Arbeitsschritte ausgewählt und somit automatisch ausgeführt werden.

Für die Verwendung des Plugins muss dieses in einem Arbeitsschritt ausgewählt sein:

| Parameter               | Belegung                                       |
|-------------------------|-------------------------------------------------|
| Automatische Aufgabe    | Ja                                              |
| Plugin für Arbeitsschritt | intranda_step_validateMetadataFields           |

## Überblick und Funktionsweise
Das Plugin liest beim Ausführen die METS-Datei des Vorgangs ein und prüft die darin enthaltenen Metadatenfelder anhand der in der Konfigurationsdatei definierten Regeln. Dabei werden folgende Validierungen unterstützt:

- **Pflichtfelder**: Prüfung, ob ein Metadatenfeld vorhanden ist und einen nicht-leeren Wert besitzt.
- **Muster (Pattern)**: Prüfung, ob der Wert eines Feldes einem regulären Ausdruck entspricht.
- **Erlaubte Inhalte (Liste)**: Prüfung, ob der Wert eines Feldes in einer externen Liste erlaubter Werte enthalten ist.
- **Mindestwortzahl**: Prüfung, ob der Wert eines Feldes eine Mindestanzahl an Wörtern enthält.

Wenn Validierungsfehler auftreten, werden alle Fehler gesammelt, als Fehlermeldung im Vorgangslog gespeichert und der Arbeitsschritt wird auf den Status `Fehler` gesetzt. Ist die Validierung erfolgreich, wird der Arbeitsschritt normal abgeschlossen.

## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `plugin_intranda_step_validateMetadataFields.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

Jedes `metadata`-Element definiert die Validierungsregeln für ein einzelnes Metadatenfeld. Folgende Attribute stehen zur Verfügung:

| Parameter                    | Erläuterung                                                                                                |
|------------------------------|-------------------------------------------------------------------------------------------------------------|
| `ugh`                        | Name des Metadatenfeldes im Regelsatz.                                                                     |
| `identifier`                 | Optionale Kennung zur Referenzierung des Feldes (z.B. für `either` oder `requiredFields`).                 |
| `required`                   | Wenn auf `true` gesetzt, muss das Metadatenfeld vorhanden sein und einen nicht-leeren Wert besitzen.       |
| `requiredErrorMessage`       | Fehlermeldung, die bei fehlendem oder leerem Pflichtfeld angezeigt wird.                                   |
| `pattern`                    | Regulärer Ausdruck, dem der Wert des Feldes entsprechen muss.                                              |
| `patternErrorMessage`        | Fehlermeldung, die angezeigt wird, wenn der Wert nicht dem Muster entspricht.                              |
| `list`                       | Pfad zu einer Textdatei mit erlaubten Werten (ein Wert pro Zeile).                                        |
| `listErrorMessage`           | Fehlermeldung, die angezeigt wird, wenn der Wert nicht in der Liste enthalten ist.                         |
| `wordcount`                  | Mindestanzahl an Wörtern, die der Wert enthalten muss.                                                    |
| `wordcountErrorMessage`      | Fehlermeldung, die angezeigt wird, wenn die Mindestwortzahl nicht erreicht wird.                           |
| `either`                     | Kennung eines anderen Feldes, von dem entweder dieses oder das andere einen Wert haben muss (derzeit nicht implementiert). |
| `requiredFields`             | Semikolon-getrennte Liste von Feld-Kennungen, die gefüllt sein müssen, damit dieses Feld Inhalt haben darf (derzeit nicht implementiert). |
