# Task 06 Test Cases

| Case | Expected result |
| --- | --- |
| Valid registration | Saved to the local persistent data file |
| Empty register number | Validation error |
| Empty name | Validation error |
| Missing event | Validation error |
| Duplicate registration | Same registration number and event are rejected |
| Filter by event | Matching rows only |
| CSV export | File created |
| Existing data file is unreadable | Storage error is reported |

Automated offline tests cover validation, persistence across service restarts, duplicate prevention, filtering, pagination, and CSV escaping.
