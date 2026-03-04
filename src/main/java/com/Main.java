package com;

import com.message.core.DataManager;
import com.message.core.database.Database;
import com.message.core.database.DbConnector;
import com.message.core.database.EntityManager;
import com.message.ihm.MessageApp;
import javafx.application.Application;

public class Main {

    /**
     * Indique si le mode bouchoné est activé.
     */
    protected static boolean IS_MOCK_ENABLED = false;

    public static void main(String[] args) {

        // Backend
        Database database = new Database();
        EntityManager entityManager = new EntityManager(database);
        DataManager dataManager = new DataManager(database, entityManager);
        DbConnector dbConnector = new DbConnector(database);

        if (IS_MOCK_ENABLED) {
            System.out.println("Mode mock activé (à implémenter pour JavaFX)");
        }

        // Lancer l'application JavaFX
        // On passe le DataManager au constructeur de MessageAppFX
        MessageApp.setDataManager(dataManager);
        Application.launch(MessageApp.class, args);
    }
}