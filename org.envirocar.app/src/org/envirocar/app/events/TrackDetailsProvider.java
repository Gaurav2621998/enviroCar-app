/**
 * Copyright (C) 2013 - 2015 the enviroCar community
 *
 * This file is part of the enviroCar app.
 *
 * The enviroCar app is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The enviroCar app is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the enviroCar app. If not, see http://www.gnu.org/licenses/.
 */
package org.envirocar.app.events;

import android.location.Location;
import android.os.SystemClock;

import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.envirocar.core.entity.Measurement;
import org.envirocar.core.events.NewMeasurementEvent;
import org.envirocar.core.events.gps.GpsLocationChangedEvent;
import org.envirocar.core.logging.Logger;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author dewall
 */
public class TrackDetailsProvider {
    private static final Logger LOGGER = Logger.getLogger(TrackDetailsProvider.class);


    private final Scheduler.Worker mMainThreadWorker = AndroidSchedulers
            .mainThread().createWorker();

    private PathOverlay mTrackMapOverlay = new PathOverlay();

    private int mNumMeasurements;
    private double mDistanceValue;
    private double mTotalSpeed;
    private int mAvrgSpeed;

    private long mStartingBaseTime;

    private Location mLastLocation;
    private Location mCurrentLocation;

    private final Bus mBus;

    /**
     * Constructor.
     *
     * @param bus
     */
    public TrackDetailsProvider(Bus bus) {
        this.mBus = bus;
        // we do not need to register on the bus!
    }

    //    @Subscribe
    //    public void onReceiveBluetoothServiceStateChangedEvent(
    //            BluetoothServiceStateChangedEvent event) {
    //        LOGGER.info(String.format("Received event: %s", event.toString()));
    //        mMainThreadWorker.schedule(() -> {
    //            if (event.mState == BluetoothServiceState.SERVICE_STOPPED) {
    //                mTrackMapOverlay.clearPath();
    //                mNumMeasurements = 0;
    //                mDistanceValue = 0;
    //                mTotalSpeed = 0;
    //                mAvrgSpeed = 0;
    //                mStartingBaseTime = 0;
    //                mLastLocation = null;
    //                mCurrentLocation = null;
    //            }
    //        });
    //    }

    @Subscribe
    public void onReceiveLocationChangedEvent(GpsLocationChangedEvent event) {
        LOGGER.debug(String.format("Received event: %s", event.toString()));

    }


    @Subscribe
    public void onReceiveNewMeasurementEvent(NewMeasurementEvent event) {
        LOGGER.debug(String.format("Received event: %s", event.toString()));

        if (mNumMeasurements == 0) {
            mStartingBaseTime = SystemClock.elapsedRealtime();
            mBus.post(new StartingTimeEvent(mStartingBaseTime, true));
        }

        mNumMeasurements++;

        // update computed features
        updateDistance(event.mMeasurement);
        updateAverageSpeed(event.mMeasurement);
        updatePathOverlay(event.mMeasurement);
    }

    @Produce
    public TrackPathOverlayEvent provideTrackPathOverlay() {
        return new TrackPathOverlayEvent(mTrackMapOverlay);
    }

    @Produce
    public DistanceValueUpdateEvent provideDistanceValue() {
        return new DistanceValueUpdateEvent(mDistanceValue);
    }

    @Produce
    public AvrgSpeedUpdateEvent provideAverageSpeed() {
        return new AvrgSpeedUpdateEvent(mAvrgSpeed);
    }

    @Produce
    public StartingTimeEvent provideStartingTime() {
        if (mStartingBaseTime == 0)
            return new StartingTimeEvent(SystemClock.elapsedRealtime(), false);
        return new StartingTimeEvent(mStartingBaseTime, true);
    }

    private void updatePathOverlay(Measurement measurement) {
        mMainThreadWorker.schedule(() -> {
            mTrackMapOverlay.addPoint(measurement.getLatitude(), measurement.getLongitude());
        });
    }

    /**
     * Updates the distance value based on the new measurements location and the location of the
     * previous location.
     *
     * @param measurement the measurement to compute the distance relative to the last measurement.
     */
    private void updateDistance(Measurement measurement) {
        if (mLastLocation == null) {
            mLastLocation = new Location("GPS");
            mLastLocation.setLatitude(measurement.getLatitude());
            mLastLocation.setLongitude(measurement.getLongitude());
        } else {
            mCurrentLocation = new Location("GPS");
            mCurrentLocation.setLatitude(measurement.getLatitude());
            mCurrentLocation.setLongitude(measurement.getLongitude());

            // Compute the distance between the last location and the new location.
            float[] res = new float[1];
            Location.distanceBetween(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(),
                    mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(),
                    res);

            // update the distance value
            if (res[0] > 0) {
                mDistanceValue += res[0] / 1000;
                mBus.post(provideDistanceValue());
            }
            mLastLocation = mCurrentLocation;
            mCurrentLocation = null;
        }
    }

    /**
     * @param measurement
     */
    private void updateAverageSpeed(Measurement measurement) {
        if (measurement.hasProperty(Measurement.PropertyKey.SPEED)){
            mTotalSpeed += measurement.getProperty(Measurement.PropertyKey.SPEED);
            mAvrgSpeed = (int) mTotalSpeed / mNumMeasurements;
            mBus.post(provideAverageSpeed());
        }
    }


    public void clear() {
        mMainThreadWorker.schedule(() -> {
            mTrackMapOverlay.clearPath();
            mNumMeasurements = 0;
            mDistanceValue = 0;
            mTotalSpeed = 0;
            mAvrgSpeed = 0;
            mStartingBaseTime = 0;
            mLastLocation = null;
            mCurrentLocation = null;
        });
    }


}
