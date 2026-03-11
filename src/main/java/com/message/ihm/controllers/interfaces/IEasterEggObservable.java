package com.message.ihm.controllers.interfaces;

public interface IEasterEggObservable {
    void addEasterEggObserver(IEasterEggObserver observer);
    void removeEasterEggObserver(IEasterEggObserver observer);
    void notifyEasterEgg(String command);
}
