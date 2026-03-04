package com.message.ihm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class HomeController {

    @FXML
    private BorderPane homeContainer;

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    private final IChatController mChatController;
    private final IProfileController mProfileController;

    public HomeController(ISessionController sessionController, IChannelController channelController, IChatController chatController, IProfileController profileController) {
        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mChatController = chatController;
        this.mProfileController = profileController;
    }

    @FXML
    public void initialize() {
        loadSidebar();
        loadChatView();
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SidebarView.fxml"));
            SidebarController sidebarController = new SidebarController(mSessionController, mChannelController, mProfileController);
            loader.setController(sidebarController);
            Pane sidebar = loader.load();
            homeContainer.setLeft(sidebar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatView.fxml"));
            // Le ChatController est déjà initialisé, on le passe directement
            loader.setController(this.mChatController);
            
            // Enregistrer le ChatController comme observateur de la session
            if (mChatController instanceof ISessionController.ISessionControllerObserver) {
                mSessionController.addObserver((ISessionController.ISessionControllerObserver) mChatController);
            }

            Pane chatView = loader.load();
            homeContainer.setCenter(chatView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
