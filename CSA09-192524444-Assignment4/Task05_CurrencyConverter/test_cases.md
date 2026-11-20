# Task 05 Test Cases

| Case | Input | Expected result |
| --- | --- | --- |
| Valid conversion | 2500 INR to USD | Converted amount displayed |
| Decimal conversion | 12.5 EUR to INR | Accurate decimal conversion displayed |
| Zero value | 0 INR to USD | Result shown as 0.00 |
| Empty input | "" | Invalid amount message |
| Invalid amount | abc | Invalid amount message |
| Negative value | -10 USD to INR | Invalid amount message |
| Enter key | Press Enter inside amount field | Trigger conversion |
| Button click | Press Convert | Trigger conversion |

## Validation notes
- Each implementation should behave identically with respect to conversion and validation.
- The correct result is calculated using the selected currencies and the common exchange-rate model.
- `ActionListener` and `ActionListener`-style input handling are both accepted for the Enter key trigger.
