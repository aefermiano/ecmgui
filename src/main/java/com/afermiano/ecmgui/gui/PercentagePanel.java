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

public class PercentagePanel extends JPanel implements Observer {

    public static final double LABEL_WEIGHT_X = 0.05;
    public static final double PERCENTAGE_BAR_WEIGHT_X = 0.95;
    public static final String ANALYSIS_TEXT = "Analysis:";
    public static final String PROCESSING_TEXT = "Processing:";

    private final Context context;

    private JProgressBar analysisBar;
    private JProgressBar processingBar;

    public PercentagePanel(Context context) {
        this.context = context;

        final LayoutManager layout = new GridBagLayout();
        setLayout(layout);

        createAnalysisPanel();
        createProcessingPanel();

        context.getControl().registerObserver(this);
    }

    private void createAnalysisPanel() {
        final JLabel label = new JLabel(ANALYSIS_TEXT);
        analysisBar = new JProgressBar(0, 100);

        GridBagConstraints constraints = buildLabelConstraints(0, 0);
        add(label, constraints);

        constraints = buildPercentageBarConstraints(1, 0);
        add(analysisBar, constraints);
    }

    private void createProcessingPanel() {
        final JLabel label = new JLabel(PROCESSING_TEXT);
        processingBar = new JProgressBar(0, 100);

        GridBagConstraints constraints = buildLabelConstraints(0, 1);
        add(label, constraints);

        constraints = buildPercentageBarConstraints(1, 1);
        add(processingBar, constraints);
    }

    final GridBagConstraints buildLabelConstraints(int gridx, int gridy) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.weightx = LABEL_WEIGHT_X;

        return constraints;
    }

    final GridBagConstraints buildPercentageBarConstraints(int gridx, int gridy) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = PERCENTAGE_BAR_WEIGHT_X;

        return constraints;
    }

    @Override
    public void onEncodingStart() {
        resetPercentages();
    }

    @Override
    public void onDecodingStart() {
        resetPercentages();
    }

    @Override
    public void onEncodingSuccess() {
        resetPercentages();
    }

    @Override
    public void onDecodingSuccess() {
        resetPercentages();
    }

    @Override
    public void onEncodingFailure() {
        resetPercentages();
    }

    @Override
    public void onDecodingFailure() {
        resetPercentages();
    }

    @Override
    public void onProgressUpdate() {
        final Model model = context.getModel();

        analysisBar.setValue(model.getAnalyzePercentage());
        processingBar.setValue(model.getEncodingOrDecodingPercentage());
    }

    private void resetPercentages() {
        analysisBar.setValue(0);
        processingBar.setValue(0);
    }
}
