# Rate Limiter

## DIY

### Fixed Window Counter

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/window/FixedWindowCounterRateLimiter.java).

<img alt="fixed-window-counter" src="images/fixed-window-counter.png" width="600"/>

### Sliding Window Log

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/window/SlidingWindowLogRateLimiter.java).

<img alt="sliding-window-log" src="images/sliding-window-log.png" width="600"/>

### Sliding Window Counter

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/window/SlidingWindowCounterRateLimiter.java).

#### Based on buckets

<img alt="sliding-window-counter-backeted" src="images/sliding-window-counter-backeted.png" width="600"/>

#### Based on linear interpolation

<img alt="sliding-window-counter-linint" src="images/sliding-window-counter-linint.png" width="600"/>
