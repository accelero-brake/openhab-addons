/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bh1750.internal;

import static org.openhab.binding.bh1750.internal.BH1750BindingConstants.CHANNEL_LIGHT_MEASURE;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;

/**
 * The {@link BH1750Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author accelero-brake - Initial contribution
 */
@NonNullByDefault
public class BH1750Handler extends BaseThingHandler/* implements GpioPinListenerDigital */ {

    private final Logger logger = LoggerFactory.getLogger(BH1750Handler.class);

    private @Nullable BH1750Configuration configuration;

    private @Nullable BH1750FVIDriver bh1750fvi;

    private @Nullable ScheduledFuture<?> refreshFuture;

    public BH1750Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(BH1750Configuration.class);
        initDevice();
        if (configuration.refresh > 0) {
            // no refresh is refresh rate is 0
            refreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, configuration.refresh,
                    configuration.refresh, TimeUnit.SECONDS);
        }
    }

    public void refresh() {
        boolean restart = false;
        try {
            if (bh1750fvi != null && bh1750fvi.isAlive()) {
                for (final Channel channel : getThing().getChannels()) {
                    final ChannelUID uid = channel.getUID();

                    if (isLinked(uid)) {
                        handleRefresh(uid);
                    }
                }
            } else {
                logger.debug("Thing {} seems not reachable, trying to restart.", getThing().getUID());
                restart = true;
            }
        } catch (final IOException e) {
            logger.debug("IOException, trying to restart {}", getThing().getUID(), e);
            restart = true;
        }
        if (restart) {
            initDevice();
        }
    }

    private void initDevice() {
        try {
            bh1750fvi = BH1750FVIDriver.getInstance(I2CBus.BUS_1, BH1750FVIDriver.I2C_ADDRESS_23);
            bh1750fvi.open();

            /*
             * } catch (final UnsupportedBusNumberException e) {
             * logger.debug("UnsupportedBusNumberException", e);
             * final String message = String.format("Could not connect to %s."
             * + ". Either it's not accessible or the user running openHAB is not part of the group `i2c and `gpio`",
             * getThing().getUID());
             * 
             * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
             * if (bh1750fvi != null) {
             * try {
             * bh1750fvi.close();
             * } catch (IOException ex) {
             * logger.warn(ex.getMessage(), ex);
             * }
             * }
             */
        } catch (final IllegalStateException e) {
            logger.error("{}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            if (bh1750fvi != null) {
                try {
                    bh1750fvi.close();
                } catch (IOException ex) {
                    logger.warn("{}", ex.getMessage(), ex);
                }
            }
            return;
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            if (bh1750fvi != null) {
                try {
                    bh1750fvi.close();
                } catch (IOException ex) {
                    logger.warn("{}", ex.getMessage(), ex);
                }
            }
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            try {
                handleRefresh(channelUID);
            } catch (IOException e) {
                logger.error("{}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void handleRefresh(final ChannelUID channelUID) throws IOException {
        if (CHANNEL_LIGHT_MEASURE.equals(channelUID.getId())) {
            logger.debug("Optical: {}", bh1750fvi.getOptical());
            updateState(channelUID, new DecimalType(bh1750fvi.getOptical()));
        } else {
            logger.error("Unsupported channel");
        }
    }

    @Override
    public void dispose() {
        if (bh1750fvi != null) {
            try {
                bh1750fvi.close();
            } catch (IOException e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        final ScheduledFuture<?> future = refreshFuture;

        if (future != null) {
            future.cancel(true);
        }
    }

    /*
     * @Override
     * public void channelLinked(final ChannelUID channelUID) {
     * final ChannelPin<?> gpioPin = pinStateHolder.getGpioPin(channelUID);
     * final Channel channel = getThing().getChannel(channelUID.getId());
     * 
     * logger.debug("Initialize pin for channel {}: {}", channelUID, gpioPin);
     * if (gpioPin != null || channel == null) {
     * logger.warn("Unable to link channel {} to a pin", channelUID);
     * } else {
     * final Configuration channelConfig = channel.getConfiguration();
     * final String groupId = channelUID.getGroupId();
     * 
     * if (GROUP_IN.equals(groupId)) {
     * final GpioPinDigitalInput inputPin = pinStateHolder.initializeInputPin(channelUID,
     * channelConfig.as(InputPinConfiguration.class));
     * inputPin.addListener(this);
     * } else {
     * pinStateHolder.initializeOutputPin(channelUID, channelConfig.as(OutputPinConfiguration.class));
     * }
     * handleRefresh(channelUID);
     * super.channelLinked(channelUID);
     * }
     * }
     */

    @Override
    public void channelUnlinked(final ChannelUID channelUID) {
        logger.info("Channel {} unlinked", channelUID);
    }

    /*
     * @Override
     * public void handleGpioPinDigitalStateChangeEvent(final @Nullable GpioPinDigitalStateChangeEvent event) {
     * if (event == null) {
     * return;
     * }
     * logger.debug("{}: Input event for pin {}: {}", getThing().getUID(), event.getPin(), event.getState());
     * final Optional<Entry<ChannelUID, ChannelPin<GpioPinDigitalInput>>> channelForPin = pinStateHolder
     * .getChannelForInputPin((GpioPinDigitalInput) event.getPin());
     * 
     * if (channelForPin.isPresent()) {
     * channelForPin.ifPresent(pin -> updateState(pin.getKey(),
     * pinStateHolder.getEventState(event, (ChannelPin<GpioPinDigitalInput>) pin.getValue())));
     * } else {
     * logger.debug("Could not find input channel for pin: {}", event.getPin());
     * }
     * }
     */
}
