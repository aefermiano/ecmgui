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

package com.afermiano.ecmgui.util;

import com.afermiano.ecmgui.model.nativemapping.FailureReason;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FailureReasonMapper {
    private static final Map<Integer, FailureReason> failureReasonMap = new HashMap<>();

    static {
        Arrays.stream(FailureReason.values()).forEach(failureReason -> failureReasonMap.put(failureReason.getNativeCode(), failureReason));
    }

    public static FailureReason get(int nativeCode) {
        final FailureReason value = failureReasonMap.get(nativeCode);
        if (value == null) {
            throw new IllegalArgumentException();
        }

        return value;
    }
}