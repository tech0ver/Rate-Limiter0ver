# Rate Limiter

## DIY

### Based on Bucket

#### Token Bucket

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/bucket/TokenBucketRateLimiter.java).

<img alt="token-bucket" src="images/token-bucket.png" width="600"/>

### Based on Window

#### Fixed Window Counter

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/window/FixedWindowCounterRateLimiter.java).

<img alt="fixed-window-counter" src="images/fixed-window-counter.png" width="600"/>

#### Sliding Window Log

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/window/SlidingWindowLogRateLimiter.java).

<img alt="sliding-window-log" src="images/sliding-window-log.png" width="600"/>

#### Sliding Window Counter

[See implementation](diy/src/main/java/io/github/tech0ver/ratelimiter/window/SlidingWindowCounterRateLimiter.java).

##### Based on buckets

<img alt="sliding-window-counter-backeted" src="images/sliding-window-counter-backeted.png" width="600"/>

##### Based on linear interpolation

<img alt="sliding-window-counter-linint" src="images/sliding-window-counter-linint.png" width="600"/>

#### Comparison

<img alt="windows-battle-1" src="images/windows-battle-1.png" width="600"/>

<img alt="windows-battle-2" src="images/windows-battle-2.png" width="600"/>
