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

package com.afermiano.ecmgui;

import com.afermiano.ecmgui.control.Control;
import com.afermiano.ecmgui.control.bridge.Bridge;
import com.afermiano.ecmgui.control.bridge.BridgeEvent;
import com.afermiano.ecmgui.model.Model;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Context {
    @Getter
    @Setter
    private Bridge bridge;
    @Getter
    @Setter
    private Model model;
    @Getter
    @Setter
    private Control control;
    @Getter
    private Queue<BridgeEvent> eventQueue = new ConcurrentLinkedQueue<>();
}
