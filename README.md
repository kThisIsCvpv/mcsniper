![alt text](https://www.kthisiscvpv.com/mxNMs1578632887WxKTz.png "Logo")
# MCSniper Sniper v2.0.0 [![Build Status](https://travis-ci.com/Lyphiard/MCSniper.svg?token=Zg31zJNBhyU3bqeEVad1&branch=master)](https://travis-ci.com/Lyphiard/MCSniper) ##
&copy; Copyright 2017 MCSniper.co All Rights Reserved

# Introduction & Background
**This project is discontinued.** Many API calls that this software relies upon cease to function.

Back in Feburary 2015, Minecraft accounts gained the ability to change their character's display name. The following scenario arised:
1. **User A** would change their display name to **Name X**. **User A** would not be able to change their name for the next **30 days**.
2. **Name X** would put into a grace period of **37 days**.
3. **User A** can revert their name change back to **Name X** anytime before the grace period is over.
4. After **37 days**, the name is released to the public.


In April 2015, **[MCSniper.com](http://www.mcsniper.com/)** was released. Users would query names at the cost of **$5.00 USD** after giving the system a Minecraft account login. The system would then attempt to snipe (be the first entity to claim) the name once it became available.

**[MCSniper.com](http://www.mcsniper.com)** shut down months after. This is when two friends, **[Charles](https://github.com/kthisiscvpv) (back-end)** and **[James](https://github.com/lyphiard) (front-end)** decided to revive the operation on **[MCSniper.co](http://www.mcsniper.co)**. 

This project was the fastest sniper on the market, beatting all competition consistently. However, we too shut down operations after just over a year after we saw a decline in our revenue. By then, the market was fully saturated and most desirable names had been claimed. This marked the end of our journey.

This repository showcases our back-end mechanics.

# Infrastructure

Sniping requires processing a huge amount of requests during a very short and small window. This means that a fast CPU and network connection is necessary. 

We purchased several Minecraft servers and backboned our project in the form of a "plugin". This was extremely cost efficient as most Minecraft server retailers weight their plans using a RAM/price ratio. Our project does not require excess RAM, only a high CPU load. 

However, our project also featured a standalone executable that enabled us to run the program on any virtual machine running Java 8 through the command line.

Our web server and database was hosted on a DDOS protected [OVH Cloud Computing](https://www.ovh.com/ca/en/) VPS node.

# Process

### Tackling Accuracy:
Sniping requires high accuracy. This means that we are able to time our responses centered at the name's released time. Through testing, we noticed that there was an exposed epoch timestamp variable in one of Minecraft's API calls. Unfortunately, calling this endpoint was rate limited. We syncronized our web server's internal clock to their servers using this exposed variable. We then syncronized all our server's internal clocks with the web server, evading the rate limit. This provided us a high acccuracy internal clock on all our nodes. See **[WorldTime (...)](/src/main/java/co/mcsniper/mcsniper/util/WorldTime.java)**.

### Tackling Precision: 
Sniping requires high precision. This means that our responses have low time spread when released. Through testing, we noticed that each individual server had their own unique max load capacities. For instance, **Server A** might only be able to fire **1000 requests** while **Server B** can do **2000 requests** reliably. We define reliably as being able to process 80% of the server's requests within a +- 10s timeframe of when the name is.released. We also noticed that each server took a different amount of time to respond. This is due to our servers being located all around the globe. We manually configured each server with a reactive tuner (individual request count and request offset). For example, **Server A** might fire their requests at **-10 000ms** while **Server B** fires at **-8 000ms**. See **[AbstractSniper (...)](/src/main/java/co/mcsniper/mcsniper/MCSniper.java#L139)**.

### Response Logging:
As explained above, all our servers are tuned manually. We want to be able to log response attributes, then make any changes as necessary. See **[ResponseLog (...)](/src/main/java/co/mcsniper/mcsniper/sniper/ResponseLog.java)**.

![alt text](https://www.kthisiscvpv.com/sre8e1578632504h9nZ9.png "Response Sample")

### Automated Updates and Restarts:
We make frequent updates to our software. We've hooked and enabled **Travis CI** for automated rebuilding and releases. When a new release is detected, the program will automatically download the new update and restart the system. See **[Updater (...)](/src/main/java/co/mcsniper/mcsniper/util/Updater.java)**.
