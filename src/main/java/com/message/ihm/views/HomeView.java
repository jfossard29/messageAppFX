package com.message.ihm.views;

import com.message.datamodel.User;
import com.message.ihm.controllers.EasterEggManagerFx;
import com.message.ihm.controllers.IChannelController;
import com.message.ihm.controllers.IChatController;
import com.message.ihm.controllers.IProfileController;
import com.message.ihm.controllers.ISessionController;
import com.message.ihm.controllers.SidebarController;
import javafx.scene.layout.BorderPane;

public class HomeView extends BorderPane {

    protected ISessionController mSessionController;
    protected IChannelController mChannelController;
    protected IChatController mChatController;
    protected IProfileController mProfileController;

    protected SidebarView mSidebarView;
    protected ChatView mChatView;
    protected EasterEggManagerFx mEasterEggManager;

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
        SidebarController sidebarController = new SidebarController(mSessionController, mChannelController, mProfileController);
        this.mSidebarView = new SidebarView(sidebarController);

        this.mChatView = new ChatView(
                mSessionController,
                mChatController,
                mChannelController
        );

        // Placement dans le BorderPane
        this.setLeft(mSidebarView);
        this.setCenter(mChatView);

        // Optionnel : style global
        this.setStyle("-fx-background-color: rgb(54,57,63);");

        // Initialisation du gestionnaire d'Easter Eggs
        this.mEasterEggManager = new EasterEggManagerFx(this, mChatView, mSidebarView);
        this.mChatView.addEasterEggObserver(mEasterEggManager);
    }

    public void setUser(User user) {
        if (mSidebarView != null) {
            mSidebarView.setUser(user);
        }
    }
}
