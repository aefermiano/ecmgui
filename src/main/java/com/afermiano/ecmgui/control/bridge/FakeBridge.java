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
import com.afermiano.ecmgui.model.nativemapping.FailureReason;
import lombok.SneakyThrows;

import java.io.File;

public class FakeBridge extends Bridge {
    private boolean shouldSucceed;
    private static int PROCESSING_TIME_IN_SECONDS = 10;
    private static int OFFSET_FOR_PROGRESSING_BAR_IN_SECONDS = 2;
    private static int PROCESSING_PAUSE_IN_MS = 200;
    private long startTimeInMs;

    public FakeBridge(Context context, boolean shouldSucceed) {
        super(context);
        this.shouldSucceed = shouldSucceed;
    }

    @Override
    public FailureReason prepareEncoding(String inputFileName, String outputFileName) {
        return commonPrepare(inputFileName);
    }

    @Override
    public void encode() {
        commonProcessing(true);
    }

    @Override
    public FailureReason prepareDecoding(String inputFileName, String outputFileName) {
        return commonPrepare(inputFileName);
    }

    @Override
    public void decode() {
        commonProcessing(false);
    }

    private FailureReason commonPrepare(String inputFileName) {
        if (!new File(inputFileName).exists()) {
            return FailureReason.ERROR_OPENING_INPUT_FILE;
        }
        this.startTimeInMs = System.currentTimeMillis();

        return FailureReason.SUCCESS;
    }

    @SneakyThrows
    private void commonProcessing(boolean encode) {
        final long currentTimeInMs = System.currentTimeMillis();
        final long diffInMs = currentTimeInMs - startTimeInMs;

        if (diffInMs < (PROCESSING_TIME_IN_SECONDS * 1000)) {
            // There is still processing to go

            final int analyzePercentage = encode ? (int) Math.min(100, Math.round(((((double) diffInMs) / 1000) / PROCESSING_TIME_IN_SECONDS) * 100)) : 0;
            final int processingPercentage = (diffInMs > OFFSET_FOR_PROGRESSING_BAR_IN_SECONDS) ? (int) Math.min(100, Math.round((((((double) diffInMs) / 1000) - OFFSET_FOR_PROGRESSING_BAR_IN_SECONDS) / (PROCESSING_TIME_IN_SECONDS - OFFSET_FOR_PROGRESSING_BAR_IN_SECONDS)) * 100)) : 0;

            setPercentage(analyzePercentage, processingPercentage);

            // Wait some time to emulate processing
            Thread.sleep(PROCESSING_PAUSE_IN_MS);

            return;
        }

        // Processing is done
        if (!shouldSucceed) {
            setFailure(FailureReason.OUT_OF_MEMORY);
            return;
        }

        if (encode) {
            setEncodingComplete(1000, 2000, 3000, 4000, 6000, 5000);
        } else {
            setDecodingComplete(6000, 7000);
        }
    }
}
