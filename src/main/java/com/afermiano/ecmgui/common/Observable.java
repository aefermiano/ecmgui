// Copyright (C) 2022 Antonio Fermiano
//
// This file is part of ecmgui.
//
// ecmgui is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ecmgui is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ecmgui.  If not, see <http://www.gnu.org/licenses/>.

package com.afermiano.ecmgui.common;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Observable {
    private Set<Observer> observers = new HashSet<>();

    public boolean registerObserver(Observer observer) {
        return observers.add(observer);
    }

    public void notifyEncodingStart() {
        broadcast(Observer::onEncodingStart);
    }

    public void notifyDecodingStart() {
        broadcast(Observer::onDecodingStart);
    }

    public void notifyEncodingSuccess() {
        broadcast(Observer::onEncodingSuccess);
    }

    public void notifyDecodingSuccess() {
        broadcast(Observer::onDecodingSuccess);
    }

    public void notifyEncodingFailure() {
        broadcast(Observer::onEncodingFailure);
    }

    public void notifyDecodingFailure() {
        broadcast(Observer::onDecodingFailure);
    }

    public void notifyProgressUpdate() {
        broadcast(Observer::onProgressUpdate);
    }

    private void broadcast(Consumer<Observer> consumer) {
        observers.forEach(consumer);
    }
}

