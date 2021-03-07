# BrickGuard

![banner](graphics/brickguard_gh_banner.png)

## Installation

[<img alt='Get it on Google Play'
      src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'
      height="80">](https://play.google.com/store/apps/details?id=com.gdlow.brickguard)

## Demo

### App workflow
<img src="graphics/demo_app.gif" width="350"/>

### Website blocking and tracking
<img src="graphics/demo_websites.gif" width="350"/>

## Description

Less crappy internet. Less crappy habits. BrickGuard is an accountability based web filter to protect your internet usage and fight online addiction 💪

😩 Annoyed by ads and want a no-fuss way to get rid of them completely?

🛡️ Want to protect yourself from phishing, tracking, malware, and other malicious domains?

🙈 Battling an online addiction (adult content, social media) and want to hold yourself accountable to your internet history?


🆘 **BrickGuard will help you in 2 ways:** 🆘

1. BrickGuard runs an internal DNS resolver that allows you to block ads, adult content and other malicious sites (phishing, tracking, malware) so you can enjoy safer, less crappy internet right off the bat. 🏏

2. Fighting an addiction to adult content or other online vices? We believe that the best way to quit is by blocking bad sites altogether and giving a trusted friend accountability over your online presence. You can opt-in for BrickGuard to send weekly reports about your streak, the bad sites you've tried to visit, as well as any attempts to change the service configuration. 💂


🌟 **Features!** 🌟

✓ **Security PIN** - So that nobody else messes around with your service configuration! 🔒

✓ **Toggle filter level** - Toggle your desired internet filter level. Behind the scenes, BrickGuard automatically routes your internet traffic to cleanbrowsing.org or OpenDNS to do a pre-filtering on malicious websites ⚡

✓ **Block and track ads & other malicious sites** - BrickGuard uses an internal dnsmasq resolver that captures and tracks the ads & malicious sites your device attempts to visit 👀

✓ **Blacklist additional custom domains** - You get to choose which additional domains you don't want to see 🙈

✓ **Streak** - BrickGuard counts the number of days you've been on it. If you deliberately deactivated the service, it resets to 0. 🏃

✓ **Accountability partner emails** - If you specify an accountability partner email, BrickGuard sends weekly reports about your streak, the malicious sites you've tried to visit, as well as any attempts to change the service configuration 💂


## How accountability works

BrickGuard allows you to opt-in to emailing weekly usage reports to a trusted accountability partner 🤝. What this email contains:

1. All attempts in the past week to visit blocked adult domains or other custom domains.

2. All attempts to deactivate the service or change the service configuration.

3. Your current streak - i.e. how long you have the service activated and running.


## Behind the scenes

This app creates a VPN tunnel to handle all DNS requests. An internal dnsmasq resolver parses dnsmasq configuration files to block attempts to malicious domains. Whenever a blocked domain is visited, the dnsmasq resolver nullifies the domain to 0.0.0.0, and the app logs an entry into a local database on your device. The app also sets the device's upstream DNS servers to cleanbrowsing.org or OpenDNS Family Shield for further protection.

If opted in to accountability emails, a worker thread sends a usage report every week to the specified email address.


## Frequently asked questions

Q. What's with the VPN?

A. A VPN tunnel is required for the app to run an internal DNS resolver in your device. The VPN is set up locally and does not connect your phone through a location proxy.

Q. Is my internet traffic monitored?

A. No. The app only logs the ads and malicious sites the device tries to connect to, and this information is stored in a local database in your device. We do not have access to any of that information, even when you opt in to accountability emails.

Q. Does this app slow down my internet?

A. No. The VPN service is run locally in your device and the service does not route your internet through any proxies. This means that your internet connection speeds are unaffected by this app.

Q. How does the accountability feature work?

A. See [How Accountability Works](#how-accountability-works)


## Requirements

* Minimum Android version: >= 5.0 (API 21)
* Recommended Android version: >= 7.1 (API 25)


## Donate ❤️

This project needs you! There are a number of things to improve which requires $$$, including getting a registered domain for the project. All donations are welcome. Thank you!

**PayPal**

[Choose how much you want to donate](https://www.paypal.com/donate/?business=FRC6AV3WFYN34&item_name=Support+for+BrickGuard+project&currency_code=GBP), all donations are welcome!

**My Bitcoin Wallet (Bitcoin only)**

	1392FJmmy3ZyB5TrJLCwjduTBxh11vnFj5

**My Ethereum Wallet (Ethereum only)**

	0x35bd1553bb3d137c96f969bb414f2fde9bc5c83e


## Contributions

We ❤️ contributions! If you have a feature request or would like to be a part of this project, send an email to brickguard.developer@outlook.com


## Open Source Licenses

* __[Daedalus](https://github.com/iTXTech/Daedalus)__ by *[iTX Technologies](https://github.com/iTXTech)* - [GPLv3](https://github.com/iTXTech/Daedalus#license)
* __[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)__ by *[Philipp Jahoda](https://github.com/PhilJay)* - [APL 2.0](https://github.com/PhilJay/MPAndroidChart#license-page_facing_up)
* __[ClearEditText](https://github.com/MrFuFuFu/ClearEditText)__ by *[Yuan Fu](https://github.com/MrFuFuFu)* - [APL 2.0](https://github.com/MrFuFuFu/ClearEditText)
* __[DNS66](https://github.com/julian-klode/dns66)__ by *[Julian Andres Klode](https://github.com/julian-klode)* - [GPLv3](https://github.com/julian-klode/dns66/blob/master/COPYING)
* __[Pcap4J](https://github.com/kaitoy/pcap4j)__ by *[Kaito Yamada](https://github.com/kaitoy)* - [MIT](https://github.com/kaitoy/pcap4j)
* __[MiniDNS](https://github.com/MiniDNS/minidns)__ by *[MiniDNS](https://github.com/MiniDNS)* - [APL 2.0](https://github.com/MiniDNS/minidns/blob/master/LICENCE_APACHE)
* __[Gson](https://github.com/google/gson)__ by *[Google](https://github.com/google)* - [APL 2.0](https://github.com/google/gson/blob/master/LICENSE)
* __[Shadowsocks](https://github.com/shadowsocks/shadowsocks-android)__ by *[Shadowsocks](https://github.com/shadowsocks)* - [GPLv3](https://github.com/shadowsocks/shadowsocks-android/blob/master/LICENSE)


## License

    Copyright (C) 2021 BrickGuard Developers <brickguard.developer@outlook.com>
    
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.


## Privacy Policy

**TLDR:** We do not knowingly collect any user information or in-app activity (logged internet domains, app interactions). The logged user interactions and internet domains are saved to the device's local storage and not shared with us. We do not have access to the message body of the weekly usage reports sent to a user's accountability email and do not knowingly use the user's accountability email for any marketing or commercial purposes. 

View the full privacy policy [here](https://gdlow.github.io/brickguard/about/privacy_policy.html).