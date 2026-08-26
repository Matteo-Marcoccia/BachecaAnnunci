CREATE DATABASE IF NOT EXISTS BachecaAnnunci
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE BachecaAnnunci;

CREATE TABLE Utente (
    Username VARCHAR(30) NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Nome VARCHAR(50) NOT NULL,
    Cognome VARCHAR(50) NOT NULL,
    CodiceFiscale CHAR(16) NOT NULL,
    DataNascita DATE NOT NULL,
    IndirizzoResidenza VARCHAR(100) NOT NULL,
    IndirizzoFatturazione VARCHAR(100) NULL,
    Email VARCHAR(100) NULL,
    Cellulare VARCHAR(20) NULL,
    TelefonoFisso VARCHAR(20) NULL,
    RecapitoPreferito VARCHAR(15) NOT NULL,
    IsGestore BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_Utente PRIMARY KEY (Username),
    CONSTRAINT uq_Utente_codice_fiscale UNIQUE (CodiceFiscale)
) ENGINE = InnoDB;

CREATE TABLE Categoria (
    CodiceCategoria INT UNSIGNED NOT NULL AUTO_INCREMENT,
    Nome VARCHAR(50) NOT NULL,
    CategoriaPadre INT UNSIGNED NULL,
    GestoreCreatore VARCHAR(30) NOT NULL,

    CONSTRAINT pk_Categoria PRIMARY KEY (CodiceCategoria),
    INDEX idx_Categoria_padre (CategoriaPadre),
    CONSTRAINT fk_Categoria_padre
        FOREIGN KEY (CategoriaPadre) REFERENCES Categoria (CodiceCategoria),
    CONSTRAINT fk_Categoria_gestore_creatore
        FOREIGN KEY (GestoreCreatore) REFERENCES Utente (Username)
) ENGINE = InnoDB;

CREATE TABLE Annuncio (
    Codice INT UNSIGNED NOT NULL AUTO_INCREMENT,
    Titolo VARCHAR(100) NOT NULL,
    DescrizioneArticolo TEXT NOT NULL,
    Prezzo DECIMAL(10, 2) UNSIGNED NOT NULL,
    Stato VARCHAR(20) NOT NULL DEFAULT 'InVendita',
    Autore VARCHAR(30) NOT NULL,
    Categoria INT UNSIGNED NOT NULL,

    CONSTRAINT pk_Annuncio PRIMARY KEY (Codice),
    INDEX idx_Annuncio_categoria_stato (Categoria, Stato),
    INDEX idx_Annuncio_autore (Autore),
    CONSTRAINT fk_Annuncio_autore
        FOREIGN KEY (Autore) REFERENCES Utente (Username),
    CONSTRAINT fk_Annuncio_categoria
        FOREIGN KEY (Categoria) REFERENCES Categoria (CodiceCategoria)
) ENGINE = InnoDB;

CREATE TABLE Nuova_Nota (
    CodiceNota INT UNSIGNED NOT NULL AUTO_INCREMENT,
    TestoNota TEXT NOT NULL,
    DataOra DATETIME NOT NULL,
    Annuncio INT UNSIGNED NOT NULL,

    CONSTRAINT pk_Nuova_Nota PRIMARY KEY (CodiceNota),
    INDEX idx_Nota_annuncio (Annuncio),
    CONSTRAINT fk_Nota_annuncio
        FOREIGN KEY (Annuncio) REFERENCES Annuncio (Codice)
) ENGINE = InnoDB;

CREATE TABLE Commento_Pubblico (
    CodiceCommento INT UNSIGNED NOT NULL AUTO_INCREMENT,
    Testo TEXT NOT NULL,
    DataOra DATETIME NOT NULL,
    Annuncio INT UNSIGNED NOT NULL,
    Autore VARCHAR(30) NOT NULL,

    CONSTRAINT pk_Commento_Pubblico PRIMARY KEY (CodiceCommento),
    INDEX idx_Commento_annuncio (Annuncio),
    CONSTRAINT fk_Commento_annuncio
        FOREIGN KEY (Annuncio) REFERENCES Annuncio (Codice),
    CONSTRAINT fk_Commento_autore
        FOREIGN KEY (Autore) REFERENCES Utente (Username)
) ENGINE = InnoDB;

CREATE TABLE Messaggio_Privato (
    CodiceMessaggio INT UNSIGNED NOT NULL AUTO_INCREMENT,
    Testo TEXT NOT NULL,
    DataOra DATETIME NOT NULL,
    Annuncio INT UNSIGNED NOT NULL,
    Mittente VARCHAR(30) NOT NULL,
    Destinatario VARCHAR(30) NOT NULL,

    CONSTRAINT pk_Messaggio_Privato PRIMARY KEY (CodiceMessaggio),
    INDEX idx_Messaggio_conversazione (Annuncio, Mittente, Destinatario),
    INDEX idx_Messaggio_data_ora (DataOra),
    CONSTRAINT fk_Messaggio_annuncio
        FOREIGN KEY (Annuncio) REFERENCES Annuncio (Codice),
    CONSTRAINT fk_Messaggio_mittente
        FOREIGN KEY (Mittente) REFERENCES Utente (Username),
    CONSTRAINT fk_Messaggio_destinatario
        FOREIGN KEY (Destinatario) REFERENCES Utente (Username)
) ENGINE = InnoDB;

CREATE TABLE Segui (
    UsernameUtente VARCHAR(30) NOT NULL,
    CodiceAnnuncio INT UNSIGNED NOT NULL,

    CONSTRAINT pk_Segui PRIMARY KEY (UsernameUtente, CodiceAnnuncio),
    INDEX idx_Segui_annuncio (CodiceAnnuncio),
    CONSTRAINT fk_Segui_utente
        FOREIGN KEY (UsernameUtente) REFERENCES Utente (Username),
    CONSTRAINT fk_Segui_annuncio
        FOREIGN KEY (CodiceAnnuncio) REFERENCES Annuncio (Codice)
) ENGINE = InnoDB;
