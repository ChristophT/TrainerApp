# Description

This is an Android app supporting Whitewater Slalom trainers. During training sessions a trainer orders their athletes to perform runs of varying length and training goals. Runs can be as short as a few seconds and as long as 1 or 2 hours. For run durations under 10 minutes the accuracy should be milliseconds. Above 10 minutes an accuracy of seconds is sufficient. Each athlete can only have one currently active run, but multiple (or even all) athletes can perform runs in parallel.

The main flow is as follwows:

- The trainer starts a training session and selects the participating athletes
- Runs are performed: 
	- The trainer gives a start signal to a specific athlete (verbally, not in the app)
	- At the same time the trainer starts a run for this athlete in the app
	- The app remembers the timestamp of this start
	- The app displays the current duration of this run
	- When the run is finished, the trainer stops the current run in the app. The total duration of this run is displayed
	- A new run can be started at any time

optional feature: To speed up the setup of a training session, athletes can be managed in traning groups. In a training session, the trainer can add all members of a group to the current session.

# Use Cases

The app is set up in different screens with following use cases

## Athlete list Screen
- Display existing athletes
- Add athletes to training
- Create new athlete
- Change athlete name
- Display results history

## Training Screen
- Add athlete
- Add group
- Remove athlete
- Manage runs
	- Start run-time
	- Display the current time of this run
	- Stop run, display and save time
	- Add note
	- Add voice memo
- (If space allows:) Display last run time
- Display last results and notes

## Results history Screen
- Show last trainings
- Show athletes
- Show results for an athlete
- Show notes
- Export results for an athlete as CSV

## Training groups Screen
- Show existing groups
- Change group name
- Delete group
- Add athlete to group
- Remove athlete from group
- Add group to workout


# Data model
## Athlete
- Name: String
- currentRun: Run

## Run
- athlete: Athlete
- startedAt: Timestamp
- finishedAt: Timestamp

## Training
- Date/time (YYYY-MM-DD HH:mm)
- description: String
- participants: List of Athlete
- runs: List of Run

## Training group
- name: String
- members: List of Athlete

# Architecture

- Native Android app in Kotlin. Use current technologies and best practices.
- Store all data locally on the device. No cloud storage or synchronization.
- Make sure to implement a strong separation of concerns
- include build scripts to create an installable package
