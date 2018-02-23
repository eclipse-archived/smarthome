# XPath Transformation Service

Transforms an [XML](https://www.w3.org/XML/) input using an [XPath](https://www.w3.org/TR/xpath/#section-Expressions) expression.



## Advanced Example
Given a retrived XML e.g. from an HIK Vision device with the namespace `xmlns="http://www.hikvision.com/ver20/XMLSchema"`.

**hikvision.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<PTZStatus version="2.0" xmlns="http://www.hikvision.com/ver20/XMLSchema">
	<AbsoluteHigh>
		<elevation>0</elevation>
		<azimuth>450</azimuth>
		<absoluteZoom>10</absoluteZoom>
	</AbsoluteHigh>
</PTZStatus>
```
A simple xpath query to fetch the Azimut value does not work as it does not adress the namespace.

**.rules**
```php
val azimuth = transform("XPATH", "/PTZStatus/AbsoluteHigh/azimuth/text()", testXml.toString)
```

To Adress the namespace in the query there are two ways, one simple which may not work in complex xml and one full qualified.

**.rules**
```php
val testXml ="<PTZStatus version=\"2.0\" xmlns=\"http://www.hikvision.com/ver20/XMLSchema\" ><AbsoluteHigh><elevation>0</elevation><azimuth>450</azimuth><absoluteZoom>10</absoluteZoom></AbsoluteHigh></PTZStatus>"
// Simple
val mytest = transform("XPATH", "/*[name()='PTZStatus']
                                 /*[name()='AbsoluteHigh']
                                 /*[name()='azimuth']
                                 /text()[1]", testXml.toString)  
// Full qualified
val mytest = transform("XPATH", "/*[local-name()='PTZStatus'    and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']
                                 /*[local-name()='AbsoluteHigh' and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']
                                 /*[local-name()='azimuth'      and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']
                                 /text()[1]", testXml.toString)
logInfo('debug','mytest : ' + mytest )
```

## Further Reading
For a [introduction](https://www.w3schools.com/xml/xpath_intro.asp) of XPath have a look at W3School.
A informative explanation of [common mistakes](https://qxf2.com/blog/common-xpath-mistakes/).
Online validation tools like [this](https://www.freeformatter.com/xpath-tester.html) can help to check your syntax.