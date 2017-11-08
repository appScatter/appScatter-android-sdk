
## The AppScatter IAP (In App Purchase) SDK


As a continuous service to our Developers, appScatter provides this SDK that will ease the implementation of In App Billing inside the android applications.
This SDK is based and improves on the good work done by the One Platform Foundation team that previously provided a [base version] for this library.
With it, the developer only needs to implement the agnostic version provided by the SDK and the it will pick the correct implementation to use, from the available stores.

To do this, the developer still needs to provide the information regarding the products that are available on each store, that the user can buy.
This can be done either inside the app it self, in which case the developer needs to account for all the stores and provide the products to the SDK manually, or by adding this information on the AppScatter servers and use the automatic configuration feature, this will create the correct provider and the products automatically based on the provided configuration, without the need to code them inside the app.

## Dependencies
As with the original OPFIab library, the AppScatter SDK library is designed to be extensible and is split up in to several provider modules.

Core dependency:
```groovy
  dependencies {
    compile 'com.appscatter.iab:corex.x.x@aar'
    compile('com.appscatter.iab:utils:x.x.x@aar') {
        transitive = true
    }
  }
```

In order to support the stores where your app is deployed, you'll need to add one or more `BillingProvider` modules.
Available providers :
```groovy
  dependencies {
    // Google
    compile 'com.appscatter.iab:google:x.x.x@aar'

    // Amazon
    compile 'com.appscatter.iab:amazon:x.x.x@aar'

    // Samsung
    compile 'com.appscatter.iab:samsung:x.x.x@aar'

    // OpenStore
    compile 'com.appscatter.iab:openstore:x.x.x@aar'

    // Fortumo
    compile 'com.appscatter.iab:fortumo:x.x.x@aar'

    // Aptoide
    compile 'com.appscatter.iab:aptoide:x.x.x@aar'

    // SlideMe
    compile 'com.appscatter.iab:slideme:x.x.x@aar'

  }
```


## Samples
For specifics on the implementation please refer to the Trivial Drive example.
* [TrivialDrive](https://github.com/appScatter/Trivial-Drive)

## Improvements
AppScatter build upon the good concept originally developed by the One Platform Foundation and added support for the Fortumo provider, proprietary Aptoide SDK implementation and SlideMe Proprietary SDK implementation.

AppScatter also implemented a mechanism to automatically configure the products and return the correct BillingProvider without the need to add the specific products and create the appropriate BillingProvider manually in the code.

## Thanks
Thanks for [One Platform Foundation](https://github.com/onepf) for starting such a cool and useful project.

## License

    Copyright 2012-2015 One Platform Foundation
    Copyright 2016-2016 AppScatter

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


