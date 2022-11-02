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
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class FilePanel extends JPanel implements Observer {

    public static final double LABEL_WEIGHT_X = 0.1;
    public static final double TEXT_FIELD_WEIGHT_X = 0.9;
    public static final double BUTTON_WEIGHT_X = 0.1;
    public static final String INPUT_FILE_TEXT = "Input file:";
    public static final String OPEN_TEXT = "Open...";
    public static final String OUTPUT_FILE_TEXT = "Output file:";
    public static final String BIN_OR_ECM_FILES_TEXT = "BIN or ECM files";
    public static final String BIN_EXTENSION_TEXT = ".bin";
    public static final String ECM_EXTENSION_TEXT = ".ecm";

    private JTextField inputFileTextField;
    private JButton inputFileButton;
    private JFileChooser inputFileChooser;

    private JTextField outputFileTextField;
    private JButton outputFileButton;
    private JFileChooser outputFileChooser;

    private final Model model;

    public FilePanel(Context context) {
        final LayoutManager layout = new GridBagLayout();
        setLayout(layout);

        createFileChoosers();
        createInputFilePanel();
        createOutputFilePanel();

        this.model = context.getModel();
        context.getControl().registerObserver(this);
    }

    private void createFileChoosers() {
        final FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }

                final String fileName = file.getName();
                return fileName.endsWith(".bin") || fileName.endsWith(".ecm");
            }

            @Override
            public String getDescription() {
                return BIN_OR_ECM_FILES_TEXT;
            }
        };

        inputFileChooser = new JFileChooser();
        inputFileChooser.setFileFilter(fileFilter);

        outputFileChooser = new JFileChooser();
        outputFileChooser.setFileFilter(fileFilter);
    }

    private GridBagConstraints createLabelConstraints(int gridx, int gridy) {
        final GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.weightx = LABEL_WEIGHT_X;

        return constraints;
    }

    private GridBagConstraints createTextFieldConstraints(int gridx, int gridy) {
        final GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.weightx = TEXT_FIELD_WEIGHT_X;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        return constraints;
    }

    private GridBagConstraints createButtonConstraints(int gridx, int gridy) {
        final GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.weightx = BUTTON_WEIGHT_X;

        return constraints;
    }

    private void createInputFilePanel() {

        final JLabel label = new JLabel(INPUT_FILE_TEXT);
        inputFileTextField = new JTextField(10);
        inputFileTextField.setEditable(false);
        inputFileButton = new JButton(OPEN_TEXT);

        GridBagConstraints constraints = createLabelConstraints(0, 0);
        add(label, constraints);

        constraints = createTextFieldConstraints(1, 0);
        add(inputFileTextField, constraints);

        constraints = createButtonConstraints(2, 0);
        add(inputFileButton, constraints);

        inputFileButton.addActionListener(l -> {
            final int ret = inputFileChooser.showOpenDialog(this);
            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }

            final String filePath = inputFileChooser.getSelectedFile().getAbsolutePath();
            String suggestedOutputPath = null;
            if (filePath.endsWith(BIN_EXTENSION_TEXT)) {
                suggestedOutputPath = filePath + ECM_EXTENSION_TEXT;
            } else if (filePath.endsWith(ECM_EXTENSION_TEXT)) {
                suggestedOutputPath = filePath.replace(ECM_EXTENSION_TEXT, BIN_EXTENSION_TEXT);
            }

            if (suggestedOutputPath != null) {
                setOutputFileName(suggestedOutputPath);
            }
            setInputFileName(filePath);
        });
    }

    private void setInputFileName(String filePath) {
        inputFileTextField.setText(filePath);
        model.setInputFileName(filePath);
    }

    private void setOutputFileName(String filePath) {
        outputFileTextField.setText(filePath);
        model.setOutputFileName(filePath);
    }

    private void createOutputFilePanel() {

        final JLabel label = new JLabel(OUTPUT_FILE_TEXT);
        outputFileTextField = new JTextField(10);
        outputFileTextField.setEditable(false);
        outputFileButton = new JButton(OPEN_TEXT);

        GridBagConstraints constraints = createLabelConstraints(0, 1);
        add(label, constraints);

        constraints = createTextFieldConstraints(1, 1);
        add(outputFileTextField, constraints);

        constraints = createButtonConstraints(2, 1);
        add(outputFileButton, constraints);

        outputFileButton.addActionListener(l -> {
            final int ret = outputFileChooser.showSaveDialog(this);
            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }
            final String filePath = outputFileChooser.getSelectedFile().getAbsolutePath();
            outputFileTextField.setText(filePath);
            setOutputFileName(filePath);
        });
    }

    private void setComponentsEnable(boolean enabled) {
        inputFileButton.setEnabled(enabled);
        inputFileTextField.setEnabled(enabled);
        outputFileButton.setEnabled(enabled);
        outputFileTextField.setEnabled(enabled);
    }

    @Override
    public void onEncodingStart() {
        setComponentsEnable(false);
    }

    @Override
    public void onDecodingStart() {
        setComponentsEnable(false);
    }

    @Override
    public void onEncodingSuccess() {
        setComponentsEnable(true);
    }

    @Override
    public void onDecodingSuccess() {
        setComponentsEnable(true);
    }

    @Override
    public void onEncodingFailure() {
        setComponentsEnable(true);
    }

    @Override
    public void onDecodingFailure() {
        setComponentsEnable(true);
    }
}
