# Parking Operations Pseudocode

## Allocate a free slot

```text
ALGORITHM Allocate(vehicle)
  VALIDATE vehicle
  FOR floor <- 0 TO numberOfFloors - 1
    FOR slot <- 0 TO capacity(floor) - 1
      IF slot is free
        occupy slot with vehicle
        RETURN [floor, slot]
      END IF
    END FOR
  END FOR
  RETURN NO_CAPACITY
END
```

## Exit and calculate fee

```text
ALGORITHM Exit(registration, exitTime, chargedKWh)
  ticket <- activeTickets[canonical(registration)]
  IF ticket does not exist: REJECT
  IF exitTime < entryTime OR chargedKWh < 0: REJECT WITHOUT MUTATION
  parking <- 0 if ticket user has valid monthly pass on entry date
             otherwise ticket.vehicle.calculateFee(entryTime, exitTime)
  charging <- ticket.vehicle.chargingFee(chargedKWh) if vehicle is Chargeable, else 0
  fee <- parking + charging
  RELEASE ticket slot
  REMOVE active ticket
  RETURN fee
END
```
