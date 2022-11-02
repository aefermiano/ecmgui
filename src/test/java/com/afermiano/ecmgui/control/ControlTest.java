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
import com.afermiano.ecmgui.common.Observer;
import com.afermiano.ecmgui.control.bridge.Bridge;
import com.afermiano.ecmgui.control.bridge.BridgeEvent;
import com.afermiano.ecmgui.model.Model;
import com.afermiano.ecmgui.model.nativemapping.FailureReason;
import com.afermiano.ecmgui.model.nativemapping.State;
import org.agrona.collections.MutableInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ControlTest {
    private static final String INPUT_FILE_NAME = "inputfile";
    private static final String OUTPUT_FILE_NAME = "outputfile";
    public static final int ASYNC_TIMEOUT_IN_SECONDS = 10;

    private Context context;
    private Control control;
    private Model model;
    private Bridge bridge;
    private ExecutorService bridgeExecutor;

    @Before
    public void setup() {
        context = new Context();

        model = spy(new Model());
        model.setInputFileName(INPUT_FILE_NAME);
        model.setOutputFileName(OUTPUT_FILE_NAME);
        context.setModel(model);

        bridge = mock(Bridge.class);
        context.setBridge(bridge);

        control = new Control(context);
    }

    @After
    public void tearDown() {
        control.close();
    }

    private void disableBridgeExecutor() throws NoSuchFieldException, IllegalAccessException, InterruptedException {

        bridgeExecutor = mock(ExecutorService.class);
        when(bridgeExecutor.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(true);

        final Field field = control.getClass().getDeclaredField("bridgeExecutor");
        field.setAccessible(true);

        final ExecutorService oldService = (ExecutorService) field.get(control);
        oldService.shutdownNow();

        field.set(control, bridgeExecutor);
    }

    @Test
    public void startEncodingShouldBehaveCorrectlyIfSuccess() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        disableBridgeExecutor();

        when(bridge.prepareEncoding(anyString(), anyString())).thenReturn(FailureReason.SUCCESS);

        final Observer observer = mock(Observer.class);

        control.registerObserver(observer);

        control.startEncoding();

        verify(model, times(1)).clearStatus();
        assertTrue(model.getStartTimeInMs() > 0);
        verify(bridge, times(1)).prepareEncoding(INPUT_FILE_NAME, OUTPUT_FILE_NAME);
        verify(observer, never()).onEncodingFailure();
        assertEquals(State.IN_PROGRESS, model.getState());
        verify(observer, times(1)).onEncodingStart();
        verify(bridgeExecutor, times(1)).submit(any(Runnable.class));
    }

    @Test
    public void startEncodingShouldBehaveCorrectlyIfFailure() throws NoSuchFieldException, InterruptedException, IllegalAccessException {
        disableBridgeExecutor();

        final FailureReason FAILURE_REASON = FailureReason.OUT_OF_MEMORY;
        when(bridge.prepareEncoding(anyString(), anyString())).thenReturn(FAILURE_REASON);

        final Observer observer = mock(Observer.class);

        control.registerObserver(observer);

        control.startEncoding();

        verify(model, times(1)).clearStatus();
        assertTrue(model.getStartTimeInMs() > 0);
        verify(bridge, times(1)).prepareEncoding(INPUT_FILE_NAME, OUTPUT_FILE_NAME);
        assertEquals(State.FAILURE, model.getState());
        assertEquals(FAILURE_REASON, model.getFailureReason());
        verify(observer, times(1)).onEncodingFailure();
        verify(observer, never()).onEncodingStart();
        verify(bridgeExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    public void encodingShouldSendCorrectEventsIfSuccess() {
        when(bridge.prepareEncoding(anyString(), anyString())).thenReturn(FailureReason.SUCCESS);
        final int PROGRESS_UPDATE_CALLS = 2;
        final MutableInteger encodeCallsRemaining = new MutableInteger(PROGRESS_UPDATE_CALLS);

        doAnswer(invocationOnMock -> {
            final State state = encodeCallsRemaining.getAndDecrement() == 0 ? State.COMPLETED : State.IN_PROGRESS;
            model.setState(state);

            return null;
        }).when(bridge).encode();

        control.startEncoding();

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();

        await().atMost(Duration.ofSeconds(ASYNC_TIMEOUT_IN_SECONDS)).until(() -> eventQueue.size() >= PROGRESS_UPDATE_CALLS + 1);

        assertEquals(PROGRESS_UPDATE_CALLS + 1, eventQueue.size());

        for (int i = 0; i < PROGRESS_UPDATE_CALLS; i++) {
            assertEquals(BridgeEvent.PROGRESS_UPDATE, eventQueue.poll());
        }
        assertEquals(BridgeEvent.ENCODING_SUCCESS, eventQueue.poll());
    }

    @Test
    public void encodingShouldSendCorrectEventsIfFailure() {
        when(bridge.prepareEncoding(anyString(), anyString())).thenReturn(FailureReason.SUCCESS);
        final int PROGRESS_UPDATE_CALLS = 2;
        final MutableInteger encodeCallsRemaining = new MutableInteger(PROGRESS_UPDATE_CALLS);

        doAnswer(invocationOnMock -> {
            final State state = encodeCallsRemaining.getAndDecrement() == 0 ? State.FAILURE : State.IN_PROGRESS;
            model.setState(state);

            return null;
        }).when(bridge).encode();

        control.startEncoding();

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();

        await().atMost(Duration.ofSeconds(ASYNC_TIMEOUT_IN_SECONDS)).until(() -> eventQueue.size() >= PROGRESS_UPDATE_CALLS + 1);

        assertEquals(PROGRESS_UPDATE_CALLS + 1, eventQueue.size());

        for (int i = 0; i < PROGRESS_UPDATE_CALLS; i++) {
            assertEquals(BridgeEvent.PROGRESS_UPDATE, eventQueue.poll());
        }
        assertEquals(BridgeEvent.ENCODING_FAILURE, eventQueue.poll());
    }

    @Test
    public void startDecodingShouldBehaveCorrectlyIfSuccess() throws NoSuchFieldException, InterruptedException, IllegalAccessException {
        disableBridgeExecutor();

        when(bridge.prepareDecoding(anyString(), anyString())).thenReturn(FailureReason.SUCCESS);

        final Observer observer = mock(Observer.class);

        control.registerObserver(observer);

        control.startDecoding();

        verify(model, times(1)).clearStatus();
        assertTrue(model.getStartTimeInMs() > 0);
        verify(bridge, times(1)).prepareDecoding(INPUT_FILE_NAME, OUTPUT_FILE_NAME);
        verify(observer, never()).onDecodingFailure();
        assertEquals(State.IN_PROGRESS, model.getState());
        verify(observer, times(1)).onDecodingStart();
        verify(bridgeExecutor, times(1)).submit(any(Runnable.class));
    }

    @Test
    public void startDecodingShouldBehaveCorrectlyIfFailure() throws NoSuchFieldException, InterruptedException, IllegalAccessException {
        disableBridgeExecutor();

        final FailureReason FAILURE_REASON = FailureReason.OUT_OF_MEMORY;
        when(bridge.prepareDecoding(anyString(), anyString())).thenReturn(FAILURE_REASON);

        final Observer observer = mock(Observer.class);

        control.registerObserver(observer);

        control.startDecoding();

        verify(model, times(1)).clearStatus();
        assertTrue(model.getStartTimeInMs() > 0);
        verify(bridge, times(1)).prepareDecoding(INPUT_FILE_NAME, OUTPUT_FILE_NAME);
        assertEquals(State.FAILURE, model.getState());
        assertEquals(FAILURE_REASON, model.getFailureReason());
        verify(observer, times(1)).onDecodingFailure();
        verify(observer, never()).onDecodingStart();
        verify(bridgeExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    public void decodingShouldSendCorrectEventsIfSuccess() {
        when(bridge.prepareDecoding(anyString(), anyString())).thenReturn(FailureReason.SUCCESS);
        final int PROGRESS_UPDATE_CALLS = 2;
        final MutableInteger encodeCallsRemaining = new MutableInteger(PROGRESS_UPDATE_CALLS);

        doAnswer(invocationOnMock -> {
            final State state = encodeCallsRemaining.getAndDecrement() == 0 ? State.COMPLETED : State.IN_PROGRESS;
            model.setState(state);

            return null;
        }).when(bridge).decode();

        control.startDecoding();

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();

        await().atMost(Duration.ofSeconds(ASYNC_TIMEOUT_IN_SECONDS)).until(() -> eventQueue.size() >= PROGRESS_UPDATE_CALLS + 1);

        assertEquals(PROGRESS_UPDATE_CALLS + 1, eventQueue.size());

        for (int i = 0; i < PROGRESS_UPDATE_CALLS; i++) {
            assertEquals(BridgeEvent.PROGRESS_UPDATE, eventQueue.poll());
        }
        assertEquals(BridgeEvent.DECODING_SUCCESS, eventQueue.poll());
    }

    @Test
    public void decodingShouldSendCorrectEventsIfFailure() {
        when(bridge.prepareDecoding(anyString(), anyString())).thenReturn(FailureReason.SUCCESS);
        final int PROGRESS_UPDATE_CALLS = 2;
        final MutableInteger encodeCallsRemaining = new MutableInteger(PROGRESS_UPDATE_CALLS);

        doAnswer(invocationOnMock -> {
            final State state = encodeCallsRemaining.getAndDecrement() == 0 ? State.FAILURE : State.IN_PROGRESS;
            model.setState(state);

            return null;
        }).when(bridge).decode();

        control.startDecoding();

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();

        await().atMost(Duration.ofSeconds(ASYNC_TIMEOUT_IN_SECONDS)).until(() -> eventQueue.size() >= PROGRESS_UPDATE_CALLS + 1);

        assertEquals(PROGRESS_UPDATE_CALLS + 1, eventQueue.size());

        for (int i = 0; i < PROGRESS_UPDATE_CALLS; i++) {
            assertEquals(BridgeEvent.PROGRESS_UPDATE, eventQueue.poll());
        }
        assertEquals(BridgeEvent.DECODING_FAILURE, eventQueue.poll());
    }

    @Test
    public void shouldHandleProgressUpdates() {
        final Observer observer = mock(Observer.class);
        control.registerObserver(observer);

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();
        eventQueue.add(BridgeEvent.PROGRESS_UPDATE);

        control.pollEvents();

        assertTrue(eventQueue.isEmpty());
        verify(observer, times(1)).onProgressUpdate();
    }

    @Test
    public void shouldHandleOnlyMostRecentProgressUpdate() {
        final Observer observer = mock(Observer.class);
        control.registerObserver(observer);

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();
        for (int i = 0; i < 10; i++) {
            eventQueue.add(BridgeEvent.PROGRESS_UPDATE);
        }

        control.pollEvents();

        assertTrue(eventQueue.isEmpty());
        verify(observer, times(1)).onProgressUpdate();
    }

    @Test
    public void shouldHandleEncodingFailure() {
        final Observer observer = mock(Observer.class);
        control.registerObserver(observer);

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();
        eventQueue.add(BridgeEvent.ENCODING_FAILURE);

        control.pollEvents();

        assertTrue(eventQueue.isEmpty());
        verify(observer, times(1)).onEncodingFailure();
        verify(model, times(1)).clearStatus();
    }

    @Test
    public void shouldHandleEncodingSuccess() {
        final Observer observer = mock(Observer.class);
        control.registerObserver(observer);

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();
        eventQueue.add(BridgeEvent.ENCODING_SUCCESS);

        control.pollEvents();

        assertTrue(eventQueue.isEmpty());
        verify(observer, times(1)).onEncodingSuccess();
        verify(model, times(1)).clearStatus();
    }

    @Test
    public void shouldHandleDecodingFailure() {
        final Observer observer = mock(Observer.class);
        control.registerObserver(observer);

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();
        eventQueue.add(BridgeEvent.DECODING_FAILURE);

        control.pollEvents();

        assertTrue(eventQueue.isEmpty());
        verify(observer, times(1)).onDecodingFailure();
        verify(model, times(1)).clearStatus();
    }

    @Test
    public void shouldHandleDecodingSuccess() {
        final Observer observer = mock(Observer.class);
        control.registerObserver(observer);

        final Queue<BridgeEvent> eventQueue = context.getEventQueue();
        eventQueue.add(BridgeEvent.DECODING_SUCCESS);

        control.pollEvents();

        assertTrue(eventQueue.isEmpty());
        verify(observer, times(1)).onDecodingSuccess();
        verify(model, times(1)).clearStatus();
    }

    @Test
    public void shouldTerminateBridgeExecutorGracefully() throws NoSuchFieldException, InterruptedException, IllegalAccessException {
        disableBridgeExecutor();
        when(bridgeExecutor.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(true);

        control.close();

        verify(bridgeExecutor, times(1)).awaitTermination(anyLong(), any(TimeUnit.class));
        verify(bridgeExecutor, never()).shutdownNow();
    }

    @Test
    public void shouldTerminateBridgeIfShutdownTimeouts() throws NoSuchFieldException, InterruptedException, IllegalAccessException {
        disableBridgeExecutor();
        when(bridgeExecutor.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(false);

        control.close();

        verify(bridgeExecutor, times(1)).awaitTermination(anyLong(), any(TimeUnit.class));
        verify(bridgeExecutor, times(1)).shutdownNow();
    }
}
