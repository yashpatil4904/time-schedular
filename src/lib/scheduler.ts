interface Meeting {
  id: string;
  title: string;
  priority: number;
  duration_minutes: number;
  deadline: string;
}

interface AvailabilitySlot {
  start_time: string;
  end_time: string;
}

interface ScheduledMeeting {
  meeting: Meeting;
  scheduledStart: string;
  scheduledEnd: string;
  score: number;
}

const PRIORITY_WEIGHT = 0.4;
const DEADLINE_WEIGHT = 0.4;
const DURATION_WEIGHT = 0.2;

export function optimizeSchedule(
  meetings: Meeting[],
  availabilitySlots: AvailabilitySlot[]
): ScheduledMeeting[] {
  const scheduled: ScheduledMeeting[] = [];
  const remainingMeetings = [...meetings];

  remainingMeetings.sort((a, b) => {
    const scoreA = calculateMeetingScore(a, new Date());
    const scoreB = calculateMeetingScore(b, new Date());
    return scoreB - scoreA;
  });

  const occupiedSlots: { start: Date; end: Date }[] = [];

  for (const meeting of remainingMeetings) {
    let bestSlot: { start: Date; end: Date; score: number } | null = null;

    for (const slot of availabilitySlots) {
      const slotStart = new Date(slot.start_time);
      const slotEnd = new Date(slot.end_time);
      const deadline = new Date(meeting.deadline);

      if (slotEnd > deadline) continue;

      let currentTime = new Date(slotStart);

      while (currentTime.getTime() + meeting.duration_minutes * 60000 <= slotEnd.getTime()) {
        const proposedEnd = new Date(currentTime.getTime() + meeting.duration_minutes * 60000);

        const hasConflict = occupiedSlots.some(occupied => {
          return !(proposedEnd <= occupied.start || currentTime >= occupied.end);
        });

        if (!hasConflict) {
          const score = calculateSchedulingScore(meeting, currentTime, deadline);

          if (!bestSlot || score > bestSlot.score) {
            bestSlot = {
              start: new Date(currentTime),
              end: new Date(proposedEnd),
              score,
            };
          }
        }

        currentTime = new Date(currentTime.getTime() + 15 * 60000);
      }
    }

    if (bestSlot) {
      scheduled.push({
        meeting,
        scheduledStart: bestSlot.start.toISOString(),
        scheduledEnd: bestSlot.end.toISOString(),
        score: bestSlot.score,
      });

      occupiedSlots.push({
        start: bestSlot.start,
        end: bestSlot.end,
      });
    }
  }

  return scheduled;
}

function calculateMeetingScore(meeting: Meeting, now: Date): number {
  const deadline = new Date(meeting.deadline);
  const timeUntilDeadline = deadline.getTime() - now.getTime();
  const maxTime = 30 * 24 * 60 * 60 * 1000;

  const normalizedPriority = meeting.priority / 10;
  const normalizedDeadline = Math.max(0, 1 - timeUntilDeadline / maxTime);
  const normalizedDuration = 1 - Math.min(meeting.duration_minutes / 240, 1);

  return (
    normalizedPriority * PRIORITY_WEIGHT +
    normalizedDeadline * DEADLINE_WEIGHT +
    normalizedDuration * DURATION_WEIGHT
  );
}

function calculateSchedulingScore(meeting: Meeting, scheduledTime: Date, deadline: Date): number {
  const timeUntilDeadline = deadline.getTime() - scheduledTime.getTime();
  const maxTime = 30 * 24 * 60 * 60 * 1000;

  const normalizedPriority = meeting.priority / 10;
  const normalizedDeadline = Math.max(0, 1 - timeUntilDeadline / maxTime);
  const normalizedDuration = 1 - Math.min(meeting.duration_minutes / 240, 1);

  return (
    normalizedPriority * PRIORITY_WEIGHT +
    normalizedDeadline * DEADLINE_WEIGHT +
    normalizedDuration * DURATION_WEIGHT
  );
}
