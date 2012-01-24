# Omniture API Scala Client

This small library can be used for requesting and retrieving Omniture reports via the Omniture 'REST' API.

Take a look at OmnitureClientTests to see how it's used.

## Testing

The test requests a report and then polls the status of that report, using a nifty piece of code I stole from swells. When the appropriate status comes through, the report is requested and asserted upon.
