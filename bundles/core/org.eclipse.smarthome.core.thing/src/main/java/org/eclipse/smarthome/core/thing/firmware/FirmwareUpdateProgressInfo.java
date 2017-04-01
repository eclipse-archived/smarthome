/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;

import com.google.common.base.Preconditions;

/**
 * The {@link FirmwareUpdateProgressInfo} represents the progress indicator for a firmware update.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Christoph Knauf - Added progress and pending
 */
public final class FirmwareUpdateProgressInfo {

    private FirmwareUID firmwareUID;

    private ProgressStep progressStep;

    private Collection<ProgressStep> sequence;

    private boolean pending;

    private Integer progress;

    /**
     * Default constructor. Will allow to instantiate this class by reflection.
     */
    protected FirmwareUpdateProgressInfo() {
        // does nothing at all
    }

    /**
     * Creates a new {@link FirmwareUpdateProgressInfo}.
     *
     * @param firmwareUID the UID of the firmware that is updated (must not be null)
     * @param progressStep the current progress step (must not be null)
     * @param sequence the collection of progress steps describing the sequence of the firmware update process
     *            (must not be null)
     * @param pending the flag indicating if the update is pending
     * @param progress the progress of the update in percent
     *
     * @throws NullPointerException if firmware UID or current progress step is null
     * @throws IllegalArgumentException if sequence is null or empty or progress is not between 0 and 100
     */
    FirmwareUpdateProgressInfo(FirmwareUID firmwareUID, ProgressStep progressStep, Collection<ProgressStep> sequence,
            boolean pending, int progress) {
        Preconditions.checkNotNull(firmwareUID, "Firmware UID must not be null.");
        Preconditions.checkArgument(progress >= 0 && progress <= 100, "The progress must be between 0 and 100.");

        this.firmwareUID = firmwareUID;
        this.progressStep = progressStep;
        this.sequence = sequence;
        this.pending = pending;
        this.progress = progress;
    }

    /**
     * Creates a new {@link FirmwareUpdateProgressInfo}.
     *
     * @param firmwareUID the UID of the firmware that is updated (must not be null)
     * @param progressStep the current progress step (must not be null)
     * @param sequence the collection of progress steps describing the sequence of the firmware update process
     *            (must not be null)
     * @param pending the flag indicating if the update is pending
     *
     * @throws NullPointerException if firmware UID or current progress step is null
     * @throws NullPointerException if progressStep is null
     * @throws IllegalArgumentException if sequence is null or empty
     */
    FirmwareUpdateProgressInfo(FirmwareUID firmwareUID, ProgressStep progressStep, Collection<ProgressStep> sequence,
            boolean pending) {
        Preconditions.checkNotNull(firmwareUID, "Firmware UID must not be null.");
        Preconditions.checkArgument(sequence != null && !sequence.isEmpty(), "Sequence must not be null or empty.");
        Preconditions.checkNotNull(progressStep, "Progress step must not be null.");

        this.firmwareUID = firmwareUID;
        this.progressStep = progressStep;
        this.sequence = sequence;
        this.pending = pending;
        this.progress = null;
    }

    /**
     * Returns the UID of the firmware that is updated.
     *
     * @return the UID of the firmware that is updated (not null)
     */
    public FirmwareUID getFirmwareUID() {
        return firmwareUID;
    }

    /**
     * Returns the current progress step.
     *
     * @return the current progress step (not null)
     */
    public ProgressStep getProgressStep() {
        return progressStep;
    }

    /**
     * Returns the sequence of the firmware update process.
     *
     * @return the sequence (not null)
     */
    public Collection<ProgressStep> getSequence() {
        return sequence;
    }

    /**
     * Returns true if the firmware update is pending, false otherwise
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return pending;
    }

    /**
     * Returns the percentage progress of the firmware update.
     * 
     * @return the progress between 0 and 100 or null if no progress was set
     */
    public Integer getProgress() {
        return progress;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firmwareUID == null) ? 0 : firmwareUID.hashCode());
        result = prime * result + (pending ? 1231 : 1237);
        result = prime * result + progress;
        result = prime * result + ((progressStep == null) ? 0 : progressStep.hashCode());
        result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FirmwareUpdateProgressInfo)) {
            return false;
        }
        FirmwareUpdateProgressInfo other = (FirmwareUpdateProgressInfo) obj;
        if (firmwareUID == null) {
            if (other.firmwareUID != null) {
                return false;
            }
        } else if (!firmwareUID.equals(other.firmwareUID)) {
            return false;
        }
        if (pending != other.pending) {
            return false;
        }
        if (!progress.equals(other.progress)) {
            return false;
        }
        if (progressStep != other.progressStep) {
            return false;
        }
        if (sequence == null) {
            if (other.sequence != null) {
                return false;
            }
        } else if (!sequence.equals(other.sequence)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FirmwareUpdateProgressInfo [firmwareUID=" + firmwareUID + ", progressStep=" + progressStep
                + ", sequence=" + sequence + ", pending=" + pending + ", progress=" + progress + "]";
    }

}
