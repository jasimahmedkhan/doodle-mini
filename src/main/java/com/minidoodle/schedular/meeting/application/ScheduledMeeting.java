package com.minidoodle.schedular.meeting.application;

import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.shared.domain.TimeRange;

/** A meeting paired with the time range owned by its slot. */
public record ScheduledMeeting(Meeting meeting, TimeRange timeRange) {
}
