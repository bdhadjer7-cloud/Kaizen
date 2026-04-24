# Kaizen Study Platform (改善)

A JavaFX desktop application for collaborative studying — built with FXML, CSS, and MVC architecture.

## Features
- **Login/Signup** with input validation and exception handling
- **Home/Profile** with overview stats, badges, calendar heatmap, study history
- **Courses** with progress tracking and lesson navigation
- **Lessons** with rich content display and chapter sidebar
- **Notes** editor with searchable note list
- **Resources** grid with type filtering and ratings
- **Pomodoro** timer with start/pause/reset, task management, and streak tracking
- **Fire Feed** community posts with reactions and tags
- **Contacts/Messages** real-time chat with message bubbles
- **Rooms** study room cards with join functionality
- **Settings** (Profile, Security, Notifications, Study Prefs, Appearances)

## Requirements
- Java 21+
- JavaFX 21.0.2 SDK

## Demo Login
- **Username:** `bouchra`
- **Password:** `password123`

## Project Structure
```
src/
  controller/    — JavaFX controllers for all pages
  model/         — Data models (User, Course, Post, etc.)
  service/       — Business logic + DemoDataService for standalone testing
  exception/     — Custom exception hierarchy
  util/          — NavigationManager, AlertHelper, Validator
  security/      — InputSanitizer, RateLimiter, SecurityManager
  repository/    — Database repositories (for future DB integration)
resources/
  view/          — FXML files for all pages
  css/           — Global + page-specific stylesheets
  images/        — Character mascots, logos, icons
```
