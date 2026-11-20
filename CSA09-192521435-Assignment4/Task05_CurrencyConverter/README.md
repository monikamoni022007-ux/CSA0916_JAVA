# Task 05 – Currency Converter

## Styles implemented
1. ActionListener implementation
2. Named inner class implementation
3. Anonymous inner class implementation
4. Lambda expression implementation

## Updated requirement
Each version supports conversion when the Convert button is clicked and when Enter is pressed inside the amount field.

## Functional behavior
- Input is accepted as a decimal amount.
- Empty input is rejected as invalid.
- Non-numeric input is rejected as invalid.
- Negative values are rejected as invalid.
- Zero values are allowed and convert to zero in the target currency.
- The amount field triggers conversion on Enter using an `ActionListener` and the button uses the same conversion logic.
- All versions share the same conversion model, using INR as the base reference currency:
  - INR = 1.0
  - USD = 83.0
  - EUR = 90.0

## Listener comparison
- `ActionListener` is the most explicit and common style.
- Inner classes keep the listener close to the component but add a separate class.
- Anonymous classes reduce boilerplate but are less reusable.
- Lambda expressions provide the cleanest syntax for simple event handlers.

## Event handling summary
- Button click: triggers the conversion action.
- Enter key in the amount field: triggers the conversion action.
- Result label: updates with the converted value.
- Error dialog: appears for invalid numeric or empty input.
