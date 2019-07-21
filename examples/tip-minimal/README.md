# Tip example with minimal configuration

This configuration does not use cloud board or set labels on pull requests. It simply logs when all paths have been verified.

Run with `sbt run` which should output

```sbtshell
[info] Running example.Hello
2019-07-21 14:44:05,392 INFO - Register is now verified
2019-07-21 14:44:05,396 INFO - Update User is now verified
2019-07-21 14:44:05,397 INFO - All tests in production passed.
2019-07-21 14:44:05,399 INFO - Terminating Actor System...
2019-07-21 14:44:05,420 INFO - Successfully terminated actor system
```

