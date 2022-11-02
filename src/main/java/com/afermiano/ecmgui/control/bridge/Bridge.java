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

package com.afermiano.ecmgui.control.bridge;

import com.afermiano.ecmgui.Context;
import com.afermiano.ecmgui.control.Control;
import com.afermiano.ecmgui.model.nativemapping.FailureReason;

public abstract class Bridge {
    private final Context context;

    protected Bridge(Context context) {
        this.context = context;
    }

    public abstract FailureReason prepareEncoding(String inputFileName, String outputFileName);

    public abstract void encode();

    public abstract FailureReason prepareDecoding(String inputFileName, String outputFileName);

    public abstract void decode();

    protected void setPercentage(int analyzePercentage, int encodingOrDecodingPercentage) {
        final Control control = context.getControl();

        control.setPercentage(analyzePercentage, encodingOrDecodingPercentage);
    }

    protected void setFailure(FailureReason failureReason) {
        final Control control = context.getControl();

        control.setFailure(failureReason);
    }

    protected void setEncodingComplete(long literalBytes, long mode1Sectors, long mode2Form1Sectors, long mode2Form2Sectors, long bytesBeforeProcessing, long bytesAfterProcessing) {
        final Control control = context.getControl();

        control.setEncodingComplete(literalBytes, mode1Sectors, mode2Form1Sectors, mode2Form2Sectors, bytesBeforeProcessing, bytesAfterProcessing);
    }

    protected void setDecodingComplete(long bytesBeforeProcessing, long bytesAfterProcessing) {
        final Control control = context.getControl();

        control.setDecodingComplete(bytesBeforeProcessing, bytesAfterProcessing);
    }
}
