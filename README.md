# LifeOS

A lightweight personal productivity app: tasks, recurring objectives, journal,
daily/weekly/monthly reviews, and simple analytics — all local, offline,
single-user.

## Stack

Kotlin, Jetpack Compose (Material 3), Room, Navigation-Compose, DataStore,
AlarmManager. No DI framework, no repository-per-microservice layering —
manual wiring in `LifeOSApplication` since this is a small single-module app.

## Structure

```
data/entity/        Room entities (Task, Objective, ObjectiveInstance,
                     JournalEntry, DailyReview, PeriodicReview,
                     NotificationSchedule)
data/dao/            Room DAOs, one per entity group
data/db/             LifeOSDatabase + type converters
data/repository/     Thin repositories: Task, Objective, Journal, Review,
                     Analytics (analytics = SQL + Kotlin arithmetic, no
                     separate "engine")
data/settings/       User settings via DataStore (chosen over a Room
                     Settings table — it's just key/value prefs)
notification/        NotificationScheduler (single scheduler for every
                     notification type), NotificationReceiver, BootReceiver,
                     NotificationHelper (channel + builder)
ui/theme/            Warm cream/clay palette, typography, shapes
ui/components/       Reusable design-system pieces: LifeOSButton, LifeOSCard,
                     LifeOSTopBar, LifeOSTextField, LifeOSProgressBar,
                     LifeOSSegmentedControl, LifeOSTaskCard,
                     LifeOSObjectiveCard, LifeOSMetricCard,
                     LifeOSHourlyTimeline (hour-by-hour day planner),
                     LifeOSWeekStrip (7-day picker with completion dots),
                     LifeOSMonthGrid (month calendar with per-day markers)
ui/common/           Shared dialogs (add/edit task with time+duration,
                     add/edit objective, journal entry, reschedule) +
                     AppViewModelFactory
ui/screens/*/        One ViewModel + one Screen per tab: Today, Calendar,
                     Objectives, Journal, Reviews, Analytics, Settings
ui/navigation/       Bottom nav + NavHost wiring the 7 screens
```

Small actions (add/edit task, add/edit objective, journal entry, reschedule)
are dialogs, not separate screens, per the "don't create a screen for every
tiny operation" requirement.

## Planning model: Hour → Day → Week → Month → Year

- **Today** — the daily driver. An hour-by-hour timeline (5am–midnight,
  auto-scrolled near the current time) where tasks render as blocks
  positioned and sized by their time and duration; tapping an empty hour
  starts "add task" pre-filled with that slot. A week strip up top shows
  completion dots for context; objectives, anything overdue, and anything
  unscheduled sit above the timeline.
- **Calendar** — Day / Week / Month / Year, one segmented control:
  - *Day* is the same hourly timeline, for any date, with prev/next-day
    arrows.
  - *Week* is a real multi-column hourly grid (Google Calendar-style — all
    7 day columns share one scroll position), with a "reflect on this
    week" shortcut into the weekly review.
  - *Month* is a calendar grid with a dot per day (amber if pending, sage
    if everything's done) and a "reflect on this month" shortcut.
  - *Year* is a 3×4 grid of months with a completion count per month;
    tapping one opens it in Month view.

Tapping a day in Week, Month, or Year always drops you into the same Day
timeline Today uses — one component reused at every altitude.

## Deleting things

Every plan type can be removed from wherever it appears:
- **Tasks** — tap any task (in the hourly timeline, an unscheduled list, or
  a week-grid block) to open a sheet with Reschedule *and* Delete.
- **Objectives** — tap an objective card (Today or Objectives) to open
  Skip today / Pause / Delete.
- **Journal entries** — long-press an entry to delete it.
- **Reviews** — long-press a daily/weekly/monthly review entry to delete it.

## Permissions — why notifications actually fire now

Three separate Android permissions determine whether a scheduled reminder
arrives at all: notification permission, exact-alarm scheduling, and
battery-optimization exemption ("allowed to run in the background"). A
person can grant one without the others, and Android doesn't make the
connection obvious, so:

- `MainActivity` requests notification permission, then the
  battery-optimization exemption dialog, on first launch (only if not
  already granted — it stops appearing once allowed).
- **Settings** has a standing "Permissions" card showing live status of all
  three with a "Fix" button each, refreshed automatically when you return
  to the app (e.g. after granting something in system settings).
- The previous version of this app only (re)armed recurring notifications
  (morning plan, daily review) when you changed a Settings toggle — so a
  fresh install with default settings never scheduled anything. Fixed:
  `LifeOSApplication` re-arms every recurring notification on every app
  start, and weekly review / monthly review / journal reminder — which
  existed as notification types but were never actually scheduled anywhere
  — now are. After a recurring notification fires, `NotificationReceiver`
  immediately re-arms the next occurrence, instead of relying on the app
  being reopened.
- Tasks created from the **Calendar** tab previously never scheduled a
  reminder at all (only Today did); centralized into one
  `NotificationScheduler.scheduleTaskStart()` used everywhere a task is
  created, edited, or rescheduled.

## Look & feel

Restyled to resemble Google Calendar: white surfaces, Google-blue accent,
saturated red/amber/green event colors, flat hairline-bordered cards, a
pill "Create" FAB, and a real multi-day hourly grid for the week view. The
month grid follows Calendar's convention exactly — today is blue text,
only the selected day gets the filled blue circle.

## Notable simplifications (intentional)

- **Settings is DataStore, not a Room table** — it's pure key/value app
  preferences, so a Preferences DataStore is simpler than a one-row Room
  entity + DAO.
- **No NotificationHistory table** — `NotificationSchedule.fired` is enough
  to know what's already gone out; a full history log wasn't needed for a
  single-user app with no history UI planned yet.
- **No DI framework** — `AppViewModelFactory` is one generic reflection-based
  factory used by every ViewModel; `LifeOSApplication` holds lazy singletons
  for the DB and repositories.
- **WEEKLY objective frequency fires on Monday only** — a placeholder rule;
  swap in a stored day if you want a different weekly anchor. CUSTOM_DAYS
  already supports arbitrary day-of-week combinations via a bitmask.
- **Objective instances are pre-materialized** 14 days ahead each time the
  Today/Objectives screen loads (idempotent — safe to call repeatedly).

## Notifications

One `NotificationType` enum covers every kind (morning plan, task reminder,
task start, overdue, daily/weekly/monthly review, journal reminder,
milestone). `NotificationScheduler` has exactly two operations that matter:
`scheduleOrReplace` and `cancel` — every feature reuses those instead of
having its own scheduling code. `BootReceiver` re-arms alarms after a reboot
since AlarmManager doesn't survive one.

## Fixed since the last revision: notifications were silently broken

The prior notification-permission fix was necessary but not sufficient —
there was a second, more fundamental bug: `NotificationDao`'s cancel/replace
queries used `targetId = :targetId`. In SQL, `x = NULL` never matches
*anything*, even a row where `targetId` genuinely is `NULL`. Every
recurring notification (morning plan, daily review, journal reminder,
weekly/monthly review) always passes `targetId = null` — so `cancel()` and
the "replace" step of `scheduleOrReplace()` silently did nothing for every
one of them. Each app launch or settings change was quietly stacking a new
duplicate alarm on top of the old one, forever, instead of replacing it.
Fixed by switching those two queries to SQLite's `IS` operator, which is
NULL-safe (`targetId IS :targetId` correctly matches `NULL` against
`NULL`). The database version was also bumped to force a clean local reset
of the notification table, since old duplicate rows may already exist from
the previous build. If you already ran that build on a device, a full
uninstall/reinstall is worth doing too, since AlarmManager alarms it
registered live outside the app's database and won't be cleaned up by a
schema bump alone.

## Building

Open the `LifeOS/` folder in Android Studio — it will generate the Gradle
wrapper automatically on first sync. Requires:

- Android Studio with AGP 9.0+ and Kotlin 2.0+ (see note below)
- JDK 17
- minSdk 26 (Android 8.0+)

**Gradle/Kotlin 2.0 note:** this project targets an AGP 9.0+ / Kotlin 2.0+
toolchain: no explicit `org.jetbrains.kotlin.android` plugin (built into
AGP 9), no `kotlinOptions {}` (replaced by the top-level
`kotlin { compilerOptions { jvmTarget.set(...) } }` DSL), and Compose's
compiler configured via the separate `org.jetbrains.kotlin.plugin.compose`
plugin instead of `composeOptions.kotlinCompilerExtensionVersion`. The
exact AGP/Kotlin/KSP version strings in the root `build.gradle.kts` are
placeholders — this sandbox has no network access to Google's/JetBrains'
Maven repos to verify current release numbers, so bump them to whatever
your Android Studio has installed if Gradle sync complains about a missing
version.

**Schema note:** the `Task` table changed shape in an earlier revision
(added `durationMinutes`), and the database is set to
`fallbackToDestructiveMigration()` since there's no shipped release to
preserve data for yet — reinstalling will reset local data once, which is
expected pre-release.

No Gradle wrapper jar is checked in (this environment has no network access
to Gradle's distribution servers); Android Studio's "Sync Project with
Gradle Files" will fetch it on first open.

## Not implemented (explicitly out of scope for v1, per spec)

Cloud sync, AI journal analysis, natural-language task parsing, home screen
widgets, Wear OS / Android Auto, advanced charts, multi-device sync.
