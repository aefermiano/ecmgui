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
import com.afermiano.ecmgui.control.bridge.FakeBridge;
import com.afermiano.ecmgui.control.bridge.NativeBridge;
import com.afermiano.ecmgui.gui.MainWindow;
import com.afermiano.ecmgui.model.Model;

import javax.swing.*;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "ecmgui", mixinStandardHelpOptions = true, description = "GUI for libecm")
public class App implements Callable<Integer> {

    @Option(names = {"-t", "--test-mode-success"}, description = "Internally mocks lib call to test GUI - always succeeds")
    private boolean testModeSuccess = false;
    @Option(names = {"-f", "--test-mode-failure"}, description = "Internally mocks lib call to test GUI - always fail with OUT_OF_MEMORY error")
    private boolean testModeFailure = false;

    private Bridge buildBridge(Context context) {
        final Bridge bridge;

        if (testModeSuccess) {
            bridge = new FakeBridge(context, true);
        } else if (testModeFailure) {
            bridge = new FakeBridge(context, false);
        } else {
            bridge = new NativeBridge(context);
        }

        return bridge;
    }

    @Override
    public Integer call() {
        final Context context = new Context();

        final Model model = new Model();
        context.setModel(model);

        final Control control = new Control(context);
        context.setControl(control);

        final Bridge bridge = buildBridge(context);
        context.setBridge(bridge);

        SwingUtilities.invokeLater(() -> new MainWindow(context));

        return 0;
    }
}
