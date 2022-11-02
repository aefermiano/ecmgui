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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame implements Observer {

    public static final String TITLE = "ECM GUI";
    public static final int WIDTH = 510;
    public static final int HEIGHT = 210;
    public static final int INSETS = 10;
    public static final int WEIGHT_X = 1;
    public static final double MILLI_TO_SECONDS_FACTOR = 1000.0;
    public static final int REFRESH_TIME_IN_MS = 200;
    private final Context context;
    private final Timer timer;

    private StringBuilder stringBuilder = new StringBuilder();

    public MainWindow(Context context) {
        super(TITLE);

        this.context = context;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                context.getControl().close();
                System.exit(0);
            }
        });
        setSize(WIDTH, HEIGHT);

        final LayoutManager layout = new GridBagLayout();
        setLayout(layout);

        final FilePanel filePanel = new FilePanel(context);
        final ButtonPanel buttonPanel = new ButtonPanel(context);
        final PercentagePanel percentagePanel = new PercentagePanel(context);

        GridBagConstraints constraints = buildConstraints(0, 0, true);
        add(filePanel, constraints);

        constraints = buildConstraints(0, 1, false);
        add(buttonPanel, constraints);

        constraints = buildConstraints(0, 2, true);
        add(percentagePanel, constraints);

        setVisible(true);
        setResizable(false);

        context.getControl().registerObserver(this);

        timer = new Timer(REFRESH_TIME_IN_MS, e -> context.getControl().pollEvents());
        timer.setRepeats(true);
    }

    private GridBagConstraints buildConstraints(int gridx, int gridy, boolean addInsets) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = WEIGHT_X;
        if (addInsets) {
            constraints.insets = new Insets(INSETS, INSETS, INSETS, INSETS);
        }

        return constraints;
    }

    @Override
    public void onEncodingStart() {
        timer.start();
    }

    @Override
    public void onDecodingStart() {
        timer.start();
    }

    @Override
    public void onEncodingSuccess() {
        timer.stop();
        final String report = buildEncodingReport();
        JOptionPane.showMessageDialog(this, report);
    }

    @Override
    public void onDecodingSuccess() {
        timer.stop();
        final String report = buildDecodingReport();
        JOptionPane.showMessageDialog(this, report);
    }

    @Override
    public void onEncodingFailure() {
        onFailure();
    }

    @Override
    public void onDecodingFailure() {
        onFailure();
    }

    private void onFailure() {
        timer.stop();
        final String message = String.format("Error: %s", context.getModel().getFailureReason().toString());
        JOptionPane.showMessageDialog(this, message);
    }

    private String buildEncodingReport() {
        final Model model = context.getModel();

        final double processingTimeInSeconds = (model.getEndTimeInMs() - model.getStartTimeInMs()) / MILLI_TO_SECONDS_FACTOR;

        stringBuilder.setLength(0);
        stringBuilder
                .append("Literal bytes: ").append(model.getLiteralBytes()).append('\n')
                .append("Mode 1 sectors: ").append(model.getMode1Sectors()).append('\n')
                .append("Mode 2 form 1 sectors: ").append(model.getMode2Form1Sectors()).append('\n')
                .append("Mode 2 form 2 sectors: ").append(model.getMode2Form2Sectors()).append('\n')
                .append("Encoded ").append(model.getBytesBeforeProcessing()).append(" bytes -> ").append(model.getBytesAfterProcessing()).append(" bytes\n")
                .append("Processing took ").append(processingTimeInSeconds).append(" seconds\n");

        return stringBuilder.toString();
    }

    private String buildDecodingReport() {
        final Model model = context.getModel();

        final double processingTimeInSeconds = (model.getEndTimeInMs() - model.getStartTimeInMs()) / MILLI_TO_SECONDS_FACTOR;

        stringBuilder.setLength(0);
        stringBuilder
                .append("Decoded ").append(model.getBytesBeforeProcessing()).append(" bytes -> ").append(model.getBytesAfterProcessing()).append(" bytes\n")
                .append("Processing took ").append(processingTimeInSeconds).append(" seconds\n");

        return stringBuilder.toString();
    }

}
