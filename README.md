# cljs-benchmark

This repository shows how to benchmark Clojurescript code in a browser.

## Overview

Edit lines [L26-L31](https://github.com/ducky427/cljs-benchmark/blob/master/src/bench/core.cljs#L26-L31) of `core.cljs` to add functions you want to benchmark.

For benchmarking, its recommended to use a production build of the Clojurescript code. To create a production build:

```bash
lein build
```

Then go to folder `resources/public` and serve by that directory using a static webserver. An example of that using Python is:

```bash
python -m SimpleHTTPServer
```

Now open `http://localhost:8000` in a browser of your choice and press the 'Run Benchmark' button to begin benchmarking.


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
