package com.message.ihm.controllers;

import com.message.ihm.views.ChatView;
import com.message.ihm.views.SidebarView;
import com.message.ihm.views.fx.EasterEggAnimationFx;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;

/**
 * Superviseur centralisé des animations et Easter Eggs.
 */
public class EasterEggManagerFx implements IEasterEggObserver {

    private final Pane mainView;
    private final ChatView chatView;
    private final SidebarView sidebarView;

    public EasterEggManagerFx(Pane mainView, ChatView chatView, SidebarView sidebarView) {
        this.mainView = mainView;
        this.chatView = chatView;
        this.sidebarView = sidebarView;
    }

    @Override
    public void onEasterEggTriggered(String command) {
        Platform.runLater(() -> {
            switch (command) {
                case "/flash" -> EasterEggAnimationFx.playFlash(mainView);
                case "/earthquake" -> EasterEggAnimationFx.playEarthquake(chatView);
                case "/flip" -> EasterEggAnimationFx.playFlip(chatView);
                case "/party" -> playPartyOnAllLists();
                case "/detach" -> playDetachOnAllLists();
                case "/dvd" -> EasterEggAnimationFx.playDvd(chatView);
                case "/zerog" -> playZeroGOnAllLists();
                case "/matrix" -> EasterEggAnimationFx.playMatrix(mainView);
                case "/all" -> {
                    EasterEggAnimationFx.playFlash(mainView);
                    EasterEggAnimationFx.playEarthquake(chatView);
                    EasterEggAnimationFx.playFlip(chatView);
                    EasterEggAnimationFx.playDvd(chatView);
                    playPartyOnAllLists();
                    playDetachOnAllLists();
                    playZeroGOnAllLists();
                }
            }
        });
    }

    /**
     * Méthode utilitaire pour appliquer le Party aux listes
     */
    private void playPartyOnAllLists() {
        EasterEggAnimationFx.playParty(sidebarView);
        EasterEggAnimationFx.playParty(chatView);
    }

    /**
     * Méthode utilitaire pour appliquer le Detach aux listes avec le délai
     */
    private void playDetachOnAllLists() {
        PauseTransition delay = new PauseTransition(javafx.util.Duration.millis(100));
        delay.setOnFinished(e -> {
            applyDetachToCells(sidebarView.getChannelCells());
            applyDetachToCells(sidebarView.getUserCells());
            applyDetachToCells(chatView.getActiveCells());
        });
        delay.play();
    }

    private void playZeroGOnAllLists() {
        PauseTransition delay = new PauseTransition(javafx.util.Duration.millis(100));
        delay.setOnFinished(e -> {
            applyZeroGToCells(sidebarView.getChannelCells());
            applyZeroGToCells(sidebarView.getUserCells());
            applyZeroGToCells(chatView.getActiveCells());
        });
        delay.play();
    }

    /**
     * Applique l'effet de détachement aux cellules d'une liste.
     */
    private void applyDetachToCells(List<? extends Node> cells) {
        for (Node cell : cells) {
            EasterEggAnimationFx.playDetach(cell);
        }
    }

    private void applyZeroGToCells(List<? extends Node> cells) {
        for (Node cell : cells) {
            EasterEggAnimationFx.playZeroG(cell);
        }
    }
}
