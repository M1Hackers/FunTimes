# Fun Times
App for finding fun places nearby.

A project for YHack 2017.

## Setup instructions

This app uses the Google Cloud Vision API and the Google
 Places API Web Service. Therefore, you'll need an API key
 from Google valid for use with those services to compile
 it. The API key is stored in the root of the package, in
 the `GlobalSecretKeys` class, as the `static final String
 GOOGLE_API_KEY`. Example:

```
package dev.m1hackers.funtimes;

final class GlobalSecretKeys {
    static final String GOOGLE_API_KEY = "xxxxxxxxxxxxxx";
}
```
