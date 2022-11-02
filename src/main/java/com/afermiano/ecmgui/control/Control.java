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

package com.afermiano.ecmgui.control;

import com.afermiano.ecmgui.Context;
import com.afermiano.ecmgui.common.Observable;
import com.afermiano.ecmgui.control.bridge.Bridge;
import com.afermiano.ecmgui.control.bridge.BridgeEvent;
import com.afermiano.ecmgui.model.Model;
import com.afermiano.ecmgui.model.nativemapping.FailureReason;
import com.afermiano.ecmgui.model.nativemapping.State;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Control extends Observable implements AutoCloseable {
    public static final int EXECUTOR_TERMINATION_WAIT_IN_SECONDS = 2;

    private final Context context;
    private ExecutorService bridgeExecutor;

    public Control(Context context) {
        this.context = context;
        bridgeExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    @SneakyThrows
    public void close() {
        bridgeExecutor.shutdown();
        if (!bridgeExecutor.awaitTermination(EXECUTOR_TERMINATION_WAIT_IN_SECONDS, TimeUnit.SECONDS)) {
            bridgeExecutor.shutdownNow();
        }
    }

    public void startEncoding() {
        final Model model = context.getModel();
        final Bridge bridge = context.getBridge();

        model.clearStatus();
        final long now = System.currentTimeMillis();
        model.setStartTimeInMs(now);

        final FailureReason failureReason = bridge.prepareEncoding(model.getInputFileName(), model.getOutputFileName());
        if (failureReason != FailureReason.SUCCESS) {
            model.setState(State.FAILURE);
            model.setFailureReason(failureReason);
            notifyEncodingFailure();
            return;
        }
        model.setState(State.IN_PROGRESS);

        notifyEncodingStart();

        bridgeExecutor.submit(() -> {
            final Queue<BridgeEvent> eventQueue = context.getEventQueue();

            while (true) {
                bridge.encode();

                if (model.getState() == State.FAILURE) {
                    eventQueue.add(BridgeEvent.ENCODING_FAILURE);
                    break;
                }

                if (model.getState() == State.COMPLETED) {
                    eventQueue.add(BridgeEvent.ENCODING_SUCCESS);
                    break;
                }

                eventQueue.add(BridgeEvent.PROGRESS_UPDATE);
            }
        });
    }

    public void startDecoding() {
        final Model model = context.getModel();
        final Bridge bridge = context.getBridge();

        model.clearStatus();
        final long now = System.currentTimeMillis();
        model.setStartTimeInMs(now);

        final FailureReason failureReason = bridge.prepareDecoding(model.getInputFileName(), model.getOutputFileName());
        if (failureReason != FailureReason.SUCCESS) {
            model.setState(State.FAILURE);
            model.setFailureReason(failureReason);
            notifyDecodingFailure();
            return;
        }
        model.setState(State.IN_PROGRESS);

        notifyDecodingStart();

        bridgeExecutor.submit(() -> {
            final Queue<BridgeEvent> queue = context.getEventQueue();

            while (true) {
                bridge.decode();

                if (model.getState() == State.FAILURE) {
                    queue.add(BridgeEvent.DECODING_FAILURE);
                    break;
                }

                if (model.getState() == State.COMPLETED) {
                    queue.add(BridgeEvent.DECODING_SUCCESS);
                    break;
                }

                queue.add(BridgeEvent.PROGRESS_UPDATE);
            }
        });
    }

    @SneakyThrows
    public void pollEvents() {
        final Model model = context.getModel();
        final Queue<BridgeEvent> eventQueue = context.getEventQueue();

        BridgeEvent event;

        while ((event = eventQueue.poll()) != null) {
            switch (event) {
                case PROGRESS_UPDATE:
                    while (eventQueue.peek() == BridgeEvent.PROGRESS_UPDATE) {
                        // Handles only most recent PROGRESS_UPDATE
                        eventQueue.poll();
                    }
                    notifyProgressUpdate();
                    break;
                case ENCODING_FAILURE:
                    notifyEncodingFailure();
                    model.clearStatus();
                    deleteOutputFile();
                    break;
                case ENCODING_SUCCESS:
                    notifyEncodingSuccess();
                    model.clearStatus();
                    break;
                case DECODING_FAILURE:
                    notifyDecodingFailure();
                    model.clearStatus();
                    deleteOutputFile();
                    break;
                case DECODING_SUCCESS:
                    notifyDecodingSuccess();
                    model.clearStatus();
                    break;
            }
        }
    }

    private void deleteOutputFile() {
        final Model model = context.getModel();

        try {
            Files.deleteIfExists(Paths.get(model.getOutputFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPercentage(int analyzePercentage, int encodingOrDecodingPercentage) {
        final Model model = context.getModel();

        model.setAnalyzePercentage(analyzePercentage);
        model.setEncodingOrDecodingPercentage(encodingOrDecodingPercentage);
    }

    public void setFailure(FailureReason failureReason) {
        final Model model = context.getModel();

        model.setState(State.FAILURE);
        model.setFailureReason(failureReason);
    }

    public void setEncodingComplete(long literalBytes, long mode1Sectors, long mode2Form1Sectors, long mode2Form2Sectors, long bytesBeforeProcessing, long bytesAfterProcessing) {
        final long now = System.currentTimeMillis();

        final Model model = context.getModel();

        model.setState(State.COMPLETED);
        model.setEndTimeInMs(now);

        model.setLiteralBytes(literalBytes);
        model.setMode1Sectors(mode1Sectors);
        model.setMode2Form1Sectors(mode2Form1Sectors);
        model.setMode2Form2Sectors(mode2Form2Sectors);
        model.setBytesBeforeProcessing(bytesBeforeProcessing);
        model.setBytesAfterProcessing(bytesAfterProcessing);

        model.setEncodingOrDecodingPercentage(100);
        model.setAnalyzePercentage(100);
    }

    public void setDecodingComplete(long bytesBeforeProcessing, long bytesAfterProcessing) {
        final long now = System.currentTimeMillis();

        final Model model = context.getModel();

        model.setState(State.COMPLETED);
        model.setEndTimeInMs(now);

        model.setBytesBeforeProcessing(bytesBeforeProcessing);
        model.setBytesAfterProcessing(bytesAfterProcessing);

        model.setEncodingOrDecodingPercentage(100);
    }
}
