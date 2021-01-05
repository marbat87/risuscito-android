# Risuscitò per Android

#### [View Releases and Changelogs](https://github.com/marbat87/risuscito-android/releases)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/c38f970e33d24ad1b439840a7d5a5c2e)](https://www.codacy.com/manual/marbat87/risuscito-android/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=marbat87/risuscito-android&amp;utm_campaign=Badge_Grade)

---

## How to add a TRANSLATION

If you want you can contribute by translating the necessary files you can give a look to the small guide below.

Actually the files below are translated in:
1. Italian
2. Ukraininan
3. English (United Kingdom)
4. Turkish
5. Polish
6. English (Philippines) 

You can find each version in the related folder (values, values-uk, values-en, values-tr, values-pl, values-en-rPH).
The hardest work is to translate and format songs files (in raw folders).

Here below there is a small guide with the description of the files that need to be translated.

---

#### Folder RES/RAW

It contains all files with the text and chords of songs. 
Actually files are present in the following folders:
1. Italian --> in [res/raw](app/src/main/res/raw)
2. Ukrainian --> in [res/raw-uk](app/src/main/res/raw-uk)
3. English (United Kingdom) --> in [res/raw-en](app/src/main/res/raw-en)
4. Turkish --> in [res/raw-tr](app/src/main/res/raw-tr)
5. Polish --> in [res/raw-pl](app/src/main/res/raw-pl)
6. Philippines English --> in [res/raw-en-rPH](app/src/main/res/raw-en-rPH)

The operation needed is to create, for each file, a French version that will be placed into res/raw-fr with the translated song.

The "format" of each file must remain very similar to the existing.

The following files inside this folder can be ignored:
- changelog
- fileout

---

#### Folder VALUES

About the remaining files inside "values", values-uk, values-en, values-tr, values-pl" and "values-de-rPH" folders, you need to create a new folder for French -> values-fr.

Below a little explanation of files that need to be translated.

For all files ONLY TAG CONTENTS must be translated, not tag name.

- **ARGOMENTI.XML** --> it contains the titles of the arguments of songs

- **LINK.XML** --> It contains the links to the records. For each song, you must put inside each tag, the link to the ONLINE record. If it's not present you can leave the TAG empty (like the UK and EN versions)

- **NOMI_LITURGICI.XML** --> It contains the name of some liturgic periods

- **PAGINE.XML** --> It contains the pages of the songs in your version of the songbook.

- **SALMI.XML** --> It contains the titles of all psalms with a corresponding song in the Risuscito songbook

- **SORGENTI.XML** --> It contains the names of the HTM files with the song texts. They correspond  to the files inside the raw folder. If a song is not translated, you must put the value **no_canto** in the corresponding tag (see examples in [values-uk/sorgenti.xml](app/src/main/res/values-uk/sorgenti.xml) file)

- **STRINGS.XML** --> It contains all the menus voices of the applications.

- **TITOLI.XML** --> It contains all the titles of the songs

**IMPORTANT**: You can recognize the lines corresponding to a specific song in the various files, because in each file the "NAME" property is the same, but has a different suffix (for example "_link" or "_title").
