# Screenshot Checklist

No screenshots have been captured or fabricated. Capture genuine VS Code terminal output and save it into the matching `output_screenshots/Task01` through `Task12` folder.

## Compile once from the project root

```powershell
cd "C:\Users\KAVYA SREE\JAVA 1"
$files = Get-ChildItem -Recurse -Filter *.java |
    Where-Object { $_.FullName -notmatch '\\(out|build)\\' } |
    Select-Object -ExpandProperty FullName
javac -Xlint:all -d out $files
```

## Suggested captures

| Screenshot | Terminal command | Save under |
| --- | --- | --- |
| Task 01 analysis | `java -cp out task01.ProblemAnalysis` | `output_screenshots/Task01` |
| Task 02 precision example | `java -cp out task02.DataRepresentation` | `output_screenshots/Task02` |
| Task 03 expected output | `java -cp out task03.ParkingFacility` | `output_screenshots/Task03` |
| Task 04 constructors | `java -cp out task04.ClassesObjectsConstructorsDemo` | `output_screenshots/Task04` |
| Task 05 static counter | `java -cp out task05.StaticMembersDemo` | `output_screenshots/Task05` |
| Task 06 inheritance | `java -cp out task06.VehicleHierarchyDemo` | `output_screenshots/Task06` |
| Task 07 object overrides | `java -cp out task07.VehicleOverrideDemo` | `output_screenshots/Task07` |
| Task 08 expected fees | `java -cp out task08.FeeDemo` | `output_screenshots/Task08` |
| Task 09 abstraction | `java -cp out task09.AbstractionDemo` | `output_screenshots/Task09` |
| Task 10 encapsulation | `java -cp out task10.EncapsulationDemo` | `output_screenshots/Task10` |
| Task 11 integrated checks | `java -cp out task11.SmartParkingSystemDemo` | `output_screenshots/Task11` |
| Task 12 design checks | `java -cp out task12.Day4DesignChallenge` | `output_screenshots/Task12` |
| Full build and test output | `javac -Xlint:all -d out $files` then `java -cp out campusparking.app.AcceptanceTests` | Task11 or a separate evidence folder |

Use the integrated acceptance test output to show the Task 12 fee behavior. The e-bike amount in automated tests is only an injected test fixture (Rs 17.50); it is not stated by the official assignment and must not be presented as the official tariff.
