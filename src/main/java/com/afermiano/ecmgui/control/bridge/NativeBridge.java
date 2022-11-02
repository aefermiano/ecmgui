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
import com.afermiano.ecmgui.util.FailureReasonMapper;

public class NativeBridge extends Bridge {

    public NativeBridge(Context context) {
        super(context);
        System.loadLibrary("ecmglue");
    }

    @Override
    public FailureReason prepareEncoding(String inputFileName, String outputFileName) {
        final int ret = nativePrepareEncoding(inputFileName, outputFileName);
        return FailureReasonMapper.get(ret);
    }

    @Override
    public FailureReason prepareDecoding(String inputFileName, String outputFileName) {
        final int ret = nativePrepareDecoding(inputFileName, outputFileName);
        return FailureReasonMapper.get(ret);
    }

    @Override
    public native void encode();

    @Override
    public native void decode();

    private native int nativePrepareEncoding(String inputFileName, String outputFileName);

    private native int nativePrepareDecoding(String inputFileName, String outputFileName);

    @SuppressWarnings("unused")
    private void setFailure(int nativeCode) {
        setFailure(FailureReasonMapper.get(nativeCode));
    }
}
