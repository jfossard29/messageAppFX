package com.message.ihm.controllers;

public interface IEasterEggObservable {
    void addEasterEggObserver(IEasterEggObserver observer);
    void removeEasterEggObserver(IEasterEggObserver observer);
    void notifyEasterEgg(String command);
}
