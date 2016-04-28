# fixture-failure

## The problem

There's a nasty little bug present in `lein test` (found in version 2.6.1, but
also seems to be present in master). In short:

* Exceptions thrown by test fixtures (e.g. connection errors when setting up a
  test database) are uncaught by `clojure.test` and bubble up to the scaffolding
  code generated by `leiningen.test/form-for-testing-namespaces`
* The `form-for-testing-namespaces` code also does not catch the exception,
  terminating early without calling `System.exit` as it normally does.
* When tests are running in a sub-process, as they do by default, the uncaught
  exception causes the main thread to die. Normally this isn't a problem - the
  sub-process exits with an error code, Leiningen sees that the sub-process
  failed, and reports that the tests failed.
* However, if the tests made any agent calls prior to the exception being
  thrown, the non-daemon threads in the agent executor service will prevent the
  sub-process from exiting when the main thread dies. If this happens, the
  sub-process (and thus `lein test`) will hang forever until explicitly killed.

## The workaround

Pending a fix in Leiningen itself (or in `clojure.test`) to catch and handle the
exception, it is possible to work around this issue by hooking the `lein test`
task and inject the exception catching code into the form generated by
`form-for-testing-namespaces`. This approach is illustrated in this project, in
`hooks/leiningen/hooks/test.clj`. The injected code hooks `clojure.test/test-ns`
to catch exceptions and report them as errors, allowing testing to continue in
other namespaces.

## The demo

This project is a demo of the bug and workaround. To run the demo:

```
# Without the workaround, hangs forever:
> lein test

# With the workaround, the exception is caught and reported as an error:
> CATCH_FIXTURE_ERRS=1 lein test
```

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
