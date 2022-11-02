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

package com.afermiano.ecmgui.model;

import com.afermiano.ecmgui.model.nativemapping.FailureReason;
import com.afermiano.ecmgui.model.nativemapping.State;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

@Getter(onMethod_ = {@Synchronized})
@Setter(onMethod_ = {@Synchronized})
public class Model {
    private State state;
    private FailureReason failureReason;
    private int analyzePercentage;
    private int encodingOrDecodingPercentage;
    private long literalBytes;
    private long mode1Sectors;
    private long mode2Form1Sectors;
    private long mode2Form2Sectors;
    private long bytesBeforeProcessing;
    private long bytesAfterProcessing;
    private long startTimeInMs;
    private long endTimeInMs;
    private String inputFileName;
    private String outputFileName;

    public synchronized void clearStatus() {
        analyzePercentage = 0;
        encodingOrDecodingPercentage = 0;
        literalBytes = 0;
        mode1Sectors = 0;
        mode2Form1Sectors = 0;
        mode2Form2Sectors = 0;
        bytesBeforeProcessing = 0;
        bytesAfterProcessing = 0;
        startTimeInMs = 0;
        endTimeInMs = 0;
    }
}
