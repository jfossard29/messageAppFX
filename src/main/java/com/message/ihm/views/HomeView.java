package com.message.ihm.views;

import com.message.datamodel.User;
import com.message.ihm.controllers.IChannelController;
import com.message.ihm.controllers.IChatController;
import com.message.ihm.controllers.IProfileController;
import com.message.ihm.controllers.ISessionController;
import javafx.scene.layout.BorderPane;

public class HomeView extends BorderPane {

    protected ISessionController mSessionController;
    protected IChannelController mChannelController;
    protected IChatController mChatController;
    protected IProfileController mProfileController;

    protected SidebarView mSidebarView;
    protected ChatView mChatView;

    public HomeView(ISessionController sessionController,
                    IChannelController channelController,
                    IChatController chatController,
                    IProfileController profileController) {

        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mChatController = chatController;
        this.mProfileController = profileController;

        initGui();
    }

    private void initGui() {

        // Initialisation des sous-vues
        this.mSidebarView = new SidebarView(
                mSessionController,
                mChannelController,
                mProfileController
        );

        this.mChatView = new ChatView(
                mSessionController,
                mChatController,
                mChannelController // Ajout du ChannelController
        );

        // Placement dans le BorderPane
        this.setLeft(mSidebarView);
        this.setCenter(mChatView);

        // Optionnel : style global
        this.setStyle("-fx-background-color: rgb(54,57,63);");
    }

    public void setUser(User user) {
        if (mSidebarView != null) {
            mSidebarView.setUser(user);
        }
    }
}