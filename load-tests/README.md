CurrencyCompareApp
================================================================================
```
---- Global Information --------------------------------------------------------
> request count                                       4625 (OK=4506   KO=119   )
> min response time                                      1 (OK=1      KO=228   )
> max response time                                  10008 (OK=9909   KO=10008 )
> mean response time                                  1170 (OK=983    KO=8250  )
> std deviation                                       2700 (OK=2411   KO=3435  )
> response time 50th percentile                          5 (OK=5      KO=10003 )
> response time 75th percentile                         19 (OK=10     KO=10004 )
> response time 95th percentile                       9544 (OK=9206   KO=10006 )
> response time 99th percentile                      10004 (OK=9844   KO=10007 )
> mean requests/sec                                 36.133 (OK=35.203 KO=0.93  )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                          3617 ( 78%)
> 800 ms < t < 1200 ms                                  51 (  1%)
> t > 1200 ms                                          838 ( 18%)
> failed                                               119 (  3%)
---- Errors --------------------------------------------------------------------
> i.n.c.ConnectTimeoutException: connection timed out: /127.0.0.     94 (78.99%)
1:8080
> i.n.c.AbstractChannel$AnnotatedSocketException: Connection res     22 (18.49%)
et by peer: /127.0.0.1:8080
> j.i.IOException: Premature close                                    3 ( 2.52%)
================================================================================
```
wrk
```
Running 30s test @ http://127.0.0.1:8080/fib?idx=1000
  3 threads and 1200 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    75.32ms  116.98ms   2.00s    97.00%
    Req/Sec     3.68k     1.85k    8.67k    66.94%
  319824 requests in 30.09s, 107.67MB read
  Socket errors: connect 0, read 2823, write 189, timeout 552
Requests/sec:  10627.70
Transfer/sec:      3.58MB
```
CurrencyCompareBindActorApp
================================================================================
```
---- Global Information --------------------------------------------------------
> request count                                       4625 (OK=4609   KO=16    )
> min response time                                      1 (OK=1      KO=664   )
> max response time                                   5124 (OK=5124   KO=2805  )
> mean response time                                   338 (OK=333    KO=1919  )
> std deviation                                        944 (OK=940    KO=595   )
> response time 50th percentile                          4 (OK=4      KO=2033  )
> response time 75th percentile                          8 (OK=8      KO=2361  )
> response time 95th percentile                       3029 (OK=3036   KO=2661  )
> response time 99th percentile                       4203 (OK=4206   KO=2776  )
> mean requests/sec                                 38.542 (OK=38.408 KO=0.133 )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                          4054 ( 88%)
> 800 ms < t < 1200 ms                                  79 (  2%)
> t > 1200 ms                                          476 ( 10%)
> failed                                                16 (  0%)
---- Errors --------------------------------------------------------------------
> i.n.c.AbstractChannel$AnnotatedSocketException: Connection res     16 (100.0%)
et by peer: /127.0.0.1:8080
================================================================================
```
wrk
```
Running 30s test @ http://127.0.0.1:8080
  3 threads and 1200 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   174.51ms  339.48ms   1.99s    89.09%
    Req/Sec     4.51k     1.93k    8.74k    66.24%
  325127 requests in 30.09s, 59.53MB read
  Socket errors: connect 0, read 1999, write 57, timeout 415
  Non-2xx or 3xx responses: 325127
Requests/sec:  10805.93
Transfer/sec:      1.98MB
```
CurrencyCompareBindApp
================================================================================
```
---- Global Information --------------------------------------------------------
> request count                                       4625 (OK=4523   KO=102   )
> min response time                                      1 (OK=1      KO=4     )
> max response time                                  10006 (OK=9889   KO=10006 )
> mean response time                                  1100 (OK=935    KO=8415  )
> std deviation                                       2707 (OK=2452   KO=3304  )
> response time 50th percentile                          4 (OK=4      KO=10002 )
> response time 75th percentile                          8 (OK=8      KO=10004 )
> response time 95th percentile                       9545 (OK=9500   KO=10005 )
> response time 99th percentile                      10003 (OK=9828   KO=10006 )
> mean requests/sec                                 36.133 (OK=35.336 KO=0.797 )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                          3735 ( 81%)
> 800 ms < t < 1200 ms                                  51 (  1%)
> t > 1200 ms                                          737 ( 16%)
> failed                                               102 (  2%)
---- Errors --------------------------------------------------------------------
> i.n.c.ConnectTimeoutException: connection timed out: /127.0.0.     82 (80.39%)
1:8080
> i.n.c.AbstractChannel$AnnotatedSocketException: Connection res     15 (14.71%)
et by peer: /127.0.0.1:8080
> j.i.IOException: Premature close                                    5 ( 4.90%)
================================================================================
```
wrk
```
Running 30s test @ http://127.0.0.1:8080/fib?idx=1000
  3 threads and 1200 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    28.93ms   41.74ms   1.98s    96.42%
    Req/Sec     6.56k     2.63k   11.68k    65.74%
  587298 requests in 30.08s, 197.71MB read
  Socket errors: connect 0, read 2396, write 82, timeout 492
Requests/sec:  19524.77
Transfer/sec:      6.57MB
```
CurrencyCompareBindAsyncApp
================================================================================
```
---- Global Information --------------------------------------------------------
> request count                                       4625 (OK=4436   KO=189   )
> min response time                                      1 (OK=1      KO=76    )
> max response time                                  10007 (OK=9940   KO=10007 )
> mean response time                                  1223 (OK=1033   KO=5689  )
> std deviation                                       2780 (OK=2547   KO=4010  )
> response time 50th percentile                          6 (OK=6      KO=3747  )
> response time 75th percentile                         73 (OK=11     KO=10003 )
> response time 95th percentile                       9544 (OK=9233   KO=10005 )
> response time 99th percentile                      10003 (OK=9862   KO=10007 )
> mean requests/sec                                 36.133 (OK=34.656 KO=1.477 )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                          3665 ( 79%)
> 800 ms < t < 1200 ms                                  60 (  1%)
> t > 1200 ms                                          711 ( 15%)
> failed                                               189 (  4%)
---- Errors --------------------------------------------------------------------
> i.n.c.AbstractChannel$AnnotatedSocketException: Connection res    101 (53.44%)
et by peer: /127.0.0.1:8080
> i.n.c.ConnectTimeoutException: connection timed out: /127.0.0.     85 (44.97%)
1:8080
> j.i.IOException: Premature close                                    3 ( 1.59%)
================================================================================
```
wrk
```
Running 30s test @ http://127.0.0.1:8080/fib?idx=1000
  3 threads and 1200 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    27.55ms   46.55ms   1.98s    96.15%
    Req/Sec     6.78k     2.56k   10.93k    63.59%
  606816 requests in 30.07s, 204.28MB read
  Socket errors: connect 0, read 2230, write 128, timeout 509
Requests/sec:  20179.76
Transfer/sec:      6.79MB
```
CurrencyCompareBindConnectionApp
================================================================================
```
---- Global Information --------------------------------------------------------
> request count                                       4625 (OK=4525   KO=100   )
> min response time                                      2 (OK=2      KO=25    )
> max response time                                  10917 (OK=10917  KO=7353  )
> mean response time                                   445 (OK=399    KO=2521  )
> std deviation                                       1435 (OK=1392   KO=1752  )
> response time 50th percentile                          6 (OK=6      KO=2411  )
> response time 75th percentile                         10 (OK=10     KO=3372  )
> response time 95th percentile                       3918 (OK=3686   KO=5520  )
> response time 99th percentile                       7382 (OK=7424   KO=6070  )
> mean requests/sec                                 38.223 (OK=37.397 KO=0.826 )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                          4141 ( 90%)
> 800 ms < t < 1200 ms                                  48 (  1%)
> t > 1200 ms                                          336 (  7%)
> failed                                               100 (  2%)
---- Errors --------------------------------------------------------------------
> i.n.c.AbstractChannel$AnnotatedSocketException: Connection res     99 (99.00%)
et by peer: /127.0.0.1:8080
> j.i.IOException: Premature close                                    1 ( 1.00%)
================================================================================
```
wrk:
```
Running 30s test @ http://127.0.0.1:8080/fib?idx=1000
  3 threads and 1200 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    28.86ms   59.22ms   2.00s    97.66%
    Req/Sec     6.66k     1.32k   11.85k    78.71%
  594589 requests in 30.05s, 200.17MB read
  Socket errors: connect 0, read 2569, write 115, timeout 458
Requests/sec:  19787.44
Transfer/sec:      6.66MB
```