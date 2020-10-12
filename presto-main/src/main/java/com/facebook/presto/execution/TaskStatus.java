/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.execution;

import com.facebook.drift.annotations.ThriftConstructor;
import com.facebook.drift.annotations.ThriftField;
import com.facebook.drift.annotations.ThriftStruct;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.facebook.presto.execution.TaskState.PLANNED;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

@ThriftStruct
public class TaskStatus
{
    /**
     * The first valid version that will be returned for a remote task.
     */
    public static final long STARTING_VERSION = 1;

    /**
     * A value lower than {@link #STARTING_VERSION}. This value can be used to
     * create an initial local task that is always older than any remote task.
     */
    private static final long MIN_VERSION = 0;

    /**
     * A value larger than any valid value. This value can be used to create
     * a final local task that is always newer than any remote task.
     */
    private static final long MAX_VERSION = Long.MAX_VALUE;

    private final TaskId taskId;
    private final UUID taskInstanceId;
    private final long version;
    private final TaskState state;
    private final URI self;
    private final String nodeId;
    private final Set<Lifespan> completedDriverGroups;

    private final int queuedPartitionedDrivers;
    private final int runningPartitionedDrivers;

    private final double outputBufferUtilization;
    private final boolean outputBufferOverutilized;

    private final long physicalWrittenDataSizeInBytes;
    private final long memoryReservationInBytes;
    private final long systemMemoryReservationInBytes;

    private final long fullGcCount;
    private final long fullGcTimeInMillis;

    private final List<ExecutionFailureInfo> failures;

    @ThriftConstructor
    @JsonCreator
    public TaskStatus(
            @JsonProperty("taskId") TaskId taskId,
            @JsonProperty("taskInstanceId") UUID taskInstanceId,
            @JsonProperty("version") long version,
            @JsonProperty("state") TaskState state,
            @JsonProperty("self") URI self,
            @JsonProperty("nodeId") String nodeId,
            @JsonProperty("completedDriverGroups") Set<Lifespan> completedDriverGroups,
            @JsonProperty("failures") List<ExecutionFailureInfo> failures,
            @JsonProperty("queuedPartitionedDrivers") int queuedPartitionedDrivers,
            @JsonProperty("runningPartitionedDrivers") int runningPartitionedDrivers,
            @JsonProperty("outputBufferUtilization") double outputBufferUtilization,
            @JsonProperty("outputBufferOverutilized") boolean outputBufferOverutilized,
            @JsonProperty("physicalWrittenDataSizeInBytes") long physicalWrittenDataSizeInBytes,
            @JsonProperty("memoryReservationInBytes") long memoryReservationInBytes,
            @JsonProperty("systemMemoryReservationInBytes") long systemMemoryReservationInBytes,
            @JsonProperty("fullGcCount") long fullGcCount,
            @JsonProperty("fullGcTimeInMillis") long fullGcTimeInMillis)
    {
        this.taskId = requireNonNull(taskId, "taskId is null");
        this.taskInstanceId = taskInstanceId;
        checkState(version >= MIN_VERSION, "version must be >= MIN_VERSION");
        this.version = version;
        this.state = requireNonNull(state, "state is null");
        this.self = requireNonNull(self, "self is null");
        this.nodeId = requireNonNull(nodeId, "nodeId is null");
        this.completedDriverGroups = requireNonNull(completedDriverGroups, "completedDriverGroups is null");

        checkArgument(queuedPartitionedDrivers >= 0, "queuedPartitionedDrivers must be positive");
        this.queuedPartitionedDrivers = queuedPartitionedDrivers;

        checkArgument(runningPartitionedDrivers >= 0, "runningPartitionedDrivers must be positive");
        this.runningPartitionedDrivers = runningPartitionedDrivers;

        this.outputBufferUtilization = outputBufferUtilization;
        this.outputBufferOverutilized = outputBufferOverutilized;

        this.physicalWrittenDataSizeInBytes = physicalWrittenDataSizeInBytes;

        this.memoryReservationInBytes = memoryReservationInBytes;
        this.systemMemoryReservationInBytes = systemMemoryReservationInBytes;
        this.failures = ImmutableList.copyOf(requireNonNull(failures, "failures is null"));

        checkArgument(fullGcCount >= 0, "fullGcCount is negative");
        this.fullGcCount = fullGcCount;
        this.fullGcTimeInMillis = fullGcTimeInMillis;
    }

    @ThriftField(1)
    @JsonProperty
    public TaskId getTaskId()
    {
        return taskId;
    }

    @ThriftField(2)
    @JsonProperty
    public UUID getTaskInstanceId()
    {
        return taskInstanceId;
    }

    @ThriftField(3)
    @JsonProperty
    public long getVersion()
    {
        return version;
    }

    @ThriftField(4)
    @JsonProperty
    public TaskState getState()
    {
        return state;
    }

    @ThriftField(5)
    @JsonProperty
    public URI getSelf()
    {
        return self;
    }

    @ThriftField(6)
    @JsonProperty
    public String getNodeId()
    {
        return nodeId;
    }

    @ThriftField(7)
    @JsonProperty
    public Set<Lifespan> getCompletedDriverGroups()
    {
        return completedDriverGroups;
    }

    @ThriftField(8)
    @JsonProperty
    public List<ExecutionFailureInfo> getFailures()
    {
        return failures;
    }

    @ThriftField(9)
    @JsonProperty
    public int getQueuedPartitionedDrivers()
    {
        return queuedPartitionedDrivers;
    }

    @ThriftField(10)
    @JsonProperty
    public int getRunningPartitionedDrivers()
    {
        return runningPartitionedDrivers;
    }

    @ThriftField(11)
    @JsonProperty
    public double getOutputBufferUtilization()
    {
        return outputBufferUtilization;
    }

    @ThriftField(12)
    @JsonProperty
    public boolean isOutputBufferOverutilized()
    {
        return outputBufferOverutilized;
    }

    @ThriftField(13)
    @JsonProperty
    public long getPhysicalWrittenDataSizeInBytes()
    {
        return physicalWrittenDataSizeInBytes;
    }

    @ThriftField(14)
    @JsonProperty
    public long getMemoryReservationInBytes()
    {
        return memoryReservationInBytes;
    }

    @ThriftField(15)
    @JsonProperty
    public long getSystemMemoryReservationInBytes()
    {
        return systemMemoryReservationInBytes;
    }

    @ThriftField(16)
    @JsonProperty
    public long getFullGcCount()
    {
        return fullGcCount;
    }

    @ThriftField(17)
    @JsonProperty
    public long getFullGcTimeInMillis()
    {
        return fullGcTimeInMillis;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("state", state)
                .toString();
    }

    public static TaskStatus initialTaskStatus(TaskId taskId, URI location, String nodeId)
    {
        return new TaskStatus(
                taskId,
                new UUID(0, 0),
                MIN_VERSION,
                PLANNED,
                location,
                nodeId,
                ImmutableSet.of(),
                ImmutableList.of(),
                0,
                0,
                0.0,
                false,
                0,
                0,
                0,
                0,
                0);
    }

    public static TaskStatus failWith(TaskStatus taskStatus, TaskState state, List<ExecutionFailureInfo> exceptions)
    {
        return new TaskStatus(
                taskStatus.getTaskId(),
                taskStatus.getTaskInstanceId(),
                MAX_VERSION,
                state,
                taskStatus.getSelf(),
                taskStatus.getNodeId(),
                taskStatus.getCompletedDriverGroups(),
                exceptions,
                taskStatus.getQueuedPartitionedDrivers(),
                taskStatus.getRunningPartitionedDrivers(),
                taskStatus.getOutputBufferUtilization(),
                taskStatus.isOutputBufferOverutilized(),
                taskStatus.getPhysicalWrittenDataSizeInBytes(),
                taskStatus.getMemoryReservationInBytes(),
                taskStatus.getSystemMemoryReservationInBytes(),
                taskStatus.getFullGcCount(),
                taskStatus.getFullGcTimeInMillis());
    }

    public TaskStatus createDelta(TaskStatus other)
    {
        return new TaskStatus(
                other.taskId,
                other.taskInstanceId,
                other.version,
                other.state,
                other.self,
                other.nodeId,
                Sets.difference(completedDriverGroups, other.completedDriverGroups),
                other.failures,
                other.queuedPartitionedDrivers - queuedPartitionedDrivers,
                other.runningPartitionedDrivers - runningPartitionedDrivers,
                other.outputBufferUtilization - outputBufferUtilization,
                other.outputBufferOverutilized,
                other.physicalWrittenDataSizeInBytes - physicalWrittenDataSizeInBytes,
                other.memoryReservationInBytes - memoryReservationInBytes,
                other.systemMemoryReservationInBytes - systemMemoryReservationInBytes,
                other.fullGcCount - fullGcCount,
                other.fullGcTimeInMillis - fullGcTimeInMillis);
    }
}
