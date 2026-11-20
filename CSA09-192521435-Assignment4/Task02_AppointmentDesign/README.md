# Task 02 – Event-Driven Design

## Event model
| Event | Source | Event object | Listener | Action |
| --- | --- | --- | --- | --- |
| Department change | JComboBox | ActionEvent | ActionListener | Refresh doctor list |
| Book appointment | JButton | ActionEvent | ActionListener | Validate and book |
| Window close | JFrame | WindowEvent | WindowAdapter | Release resources |

## Component hierarchy
JFrame -> Header panel -> Title label
JFrame -> Form panel -> Patient ID, Department, Doctor, Time Slot
JFrame -> Button panel -> Book Appointment
JFrame -> Status panel -> Status label

## Event trace
The window initializes, populates the department list, reacts to selection changes, validates user input, and then confirms the appointment. Closing the window triggers a `WindowListener` event and handles disposal.
