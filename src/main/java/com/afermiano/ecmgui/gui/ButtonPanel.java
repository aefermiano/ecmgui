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

package com.afermiano.ecmgui.gui;

import com.afermiano.ecmgui.Context;
import com.afermiano.ecmgui.common.Observer;
import com.afermiano.ecmgui.model.Model;

import javax.swing.*;
import java.awt.*;

public class ButtonPanel extends JPanel implements Observer {
    public static final String PLEASE_SELECT_BOTH_FILES_TEXT = "Please select both files";
    private JButton encodeButton;
    private JButton decodeButton;

    private Model model;

    public ButtonPanel(Context context) {
        final LayoutManager layout = new FlowLayout();
        setLayout(layout);

        this.model = context.getModel();

        encodeButton = new JButton("Encode");
        decodeButton = new JButton("Decode");

        add(encodeButton);
        add(decodeButton);

        context.getControl().registerObserver(this);

        encodeButton.addActionListener(e -> {
            if (!checkSelectedFiles()) {
                return;
            }

            context.getControl().startEncoding();
        });
        decodeButton.addActionListener(e -> {
            if (!checkSelectedFiles()) {
                return;
            }

            context.getControl().startDecoding();
        });
    }

    private boolean checkSelectedFiles() {
        final String inputFile = model.getInputFileName();
        final String outputFile = model.getOutputFileName();

        if (inputFile == null || inputFile.isBlank() || outputFile == null || outputFile.isBlank()) {
            JOptionPane.showMessageDialog(this, PLEASE_SELECT_BOTH_FILES_TEXT);
            return false;
        }

        return true;
    }

    private void setButtonsEnable(boolean enabled) {
        encodeButton.setEnabled(enabled);
        decodeButton.setEnabled(enabled);
    }

    @Override
    public void onEncodingStart() {
        setButtonsEnable(false);
    }

    @Override
    public void onDecodingStart() {
        setButtonsEnable(false);
    }

    @Override
    public void onEncodingSuccess() {
        setButtonsEnable(true);
    }

    @Override
    public void onDecodingSuccess() {
        setButtonsEnable(true);
    }

    @Override
    public void onEncodingFailure() {
        setButtonsEnable(true);
    }

    @Override
    public void onDecodingFailure() {
        setButtonsEnable(true);
    }
}
