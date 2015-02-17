CREATE TABLE abrechnungswochen (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  von DATE NULL,
  bis DATE NULL,
  zahltag DATE NULL,
  abr_woche INTEGER UNSIGNED NULL,
  abr_monat INTEGER UNSIGNED NULL,
  abr_jahr INTEGER UNSIGNED NULL,
  PRIMARY KEY(id)
);

CREATE TABLE aktionen (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  kunde INTEGER UNSIGNED NOT NULL,
  angelegt DATETIME NULL,
  aktionstyp INTEGER UNSIGNED NULL,
  marktforscher INTEGER UNSIGNED NULL,
  ergebnis INTEGER UNSIGNED NULL,
  eingangsdatum DATETIME NULL,
  PRIMARY KEY(id, kunde)
);

CREATE TABLE aktionstyp (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(30) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE backup (
  kunde INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  ergebnis VARCHAR(50) NULL,
  tuerfarbe VARCHAR(50) NULL,
  fassadenfarbe VARCHAR(50) NULL,
  PRIMARY KEY(kunde)
);

CREATE TABLE bearbeitungsstatus (
  id INTEGER UNSIGNED NOT NULL,
  name VARCHAR(50) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE ergebnisse (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(30) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE farben (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE fassadenart (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE gespraeche (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  kunde INTEGER UNSIGNED NULL,
  datum_md DATE NULL,
  datum_vd DATE NULL,
  lieferung VARCHAR(10) NULL,
  produkt INTEGER UNSIGNED NULL,
  auftragnehmer VARCHAR(20) NULL,
  werbeleiter INTEGER UNSIGNED NULL,
  summe INTEGER UNSIGNED NULL,
  marktforscher INTEGER UNSIGNED NULL,
  marktdatenermittler INTEGER UNSIGNED NULL,
  ergebnis INTEGER UNSIGNED NULL,
  PRIMARY KEY(id)
);

CREATE TABLE heizung (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE kunden (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  bearbeitungsstatus INTEGER UNSIGNED NOT NULL DEFAULT 2,
  fassadenfarbe INTEGER UNSIGNED NOT NULL,
  nachname VARCHAR(50) NULL,
  vorname VARCHAR(50) NULL,
  strasse VARCHAR(50) NULL,
  hausnummer VARCHAR(50) NULL,
  plz VARCHAR(50) NULL,
  stadt VARCHAR(50) NULL,
  telprivat VARCHAR(50) NULL,
  telbuero VARCHAR(50) NULL,
  telefax VARCHAR(50) NULL,
  email VARCHAR(100) NULL,
  fensterzahl INTEGER UNSIGNED ZEROFILL NULL,
  fassadenart INTEGER UNSIGNED NULL,
  haustuerfarbe INTEGER UNSIGNED NULL,
  glasbausteine BOOL NULL,
  heizung INTEGER UNSIGNED NULL,
  zaunlaenge INTEGER UNSIGNED ZEROFILL NULL,
  angelegt DATETIME NULL,
  bearbeiter VARCHAR(50) NULL,
  sourcefile VARCHAR(250) NULL,
  marktforscher INTEGER UNSIGNED NULL DEFAULT 0,
  PRIMARY KEY(id),
  INDEX kunden_nachname(nachname),
  INDEX kunden_plz(plz),
  INDEX kunden_strasse(strasse),
  INDEX kunden_state_mafo(bearbeitungsstatus, marktforscher)
);

CREATE TABLE marktdatenermittler (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  nachname VARCHAR(50) NULL,
  vorname VARCHAR(50) NULL,
  strasse VARCHAR(50) NULL,
  hausnummer VARCHAR(50) NULL,
  plz VARCHAR(50) NULL,
  stadt VARCHAR(50) NULL,
  telefon VARCHAR(50) NULL,
  handy VARCHAR(50) NULL,
  telefax VARCHAR(50) NULL,
  email VARCHAR(50) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE marktforscher (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  aktiv BOOL NULL,
  anrede VARCHAR(10) NULL,
  nachname VARCHAR(50) NULL,
  vorname VARCHAR(50) NULL,
  kurzname VARCHAR(10) NULL,
  strasse VARCHAR(50) NULL,
  hausnummer VARCHAR(50) NULL,
  plz VARCHAR(50) NULL,
  stadt VARCHAR(50) NULL,
  telefon VARCHAR(50) NULL,
  handy VARCHAR(50) NULL,
  telefax VARCHAR(50) NULL,
  email VARCHAR(50) NULL,
  geburtsdatum DATE NULL,
  kontonummer VARCHAR(30) NULL,
  blz VARCHAR(30) NULL,
  honorar_termin DOUBLE NULL,
  honorar_adresse DOUBLE NULL,
  honorar_pauschale DOUBLE NULL,
  PRIMARY KEY(id)
);

CREATE TABLE plzbereiche (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  plzvon VARCHAR(10) NULL,
  plzbis VARCHAR(10) NULL,
  name VARCHAR(50) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE produkte (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE terminergebnisse (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NULL,
  erfolg BOOL NULL,
  PRIMARY KEY(id)
);

CREATE TABLE werbeleiter (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  nachname VARCHAR(50) NULL,
  vorname VARCHAR(50) NULL,
  kurzname VARCHAR(10) NULL,
  strasse VARCHAR(50) NULL,
  hausnummer VARCHAR(50) NULL,
  plz VARCHAR(50) NULL,
  stadt VARCHAR(50) NULL,
  telefon VARCHAR(50) NULL,
  handy VARCHAR(50) NULL,
  telefax VARCHAR(50) NULL,
  email VARCHAR(50) NULL,
  PRIMARY KEY(id)
);
