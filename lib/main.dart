import 'package:barcode_scan/barcode_scan.dart';
import 'package:dartz/dartz.dart' as dartz;

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String scanqr = '';

  static const platform = const MethodChannel('test_channel');
  Future<void> _invoke_test_method(String scannedQRCode) async {
    try {
      final String result = await platform.invokeMethod(
          'test_method', <String, String>{'url': scannedQRCode});
      setState(() {
        scanqr = result;
      });
    } on PlatformException catch (e) {
      print("unable ${e}");
      // Unable to open the browser print(e);
    }
  }


  Future<dartz.Option<ScanResult>> _scanQR() async {
    print("_scanQR");
    try {
      ScanResult qrResult = await BarcodeScanner.scan();
      return dartz.optionOf(qrResult);
    } on PlatformException catch (error) {
      print("error ${error}");
    } on FormatException catch (error) {
      print("format exception ${error}");
    }
    return dartz.none();
  }

  Future<void> _scanQRCode() async {
    final result = await _scanQR();
    print("result inside _scanQRCode");

    result.fold(() {
      print("none");
    }, (scannedResult) {
      print("scanned QR content ${scannedResult.rawContent}");

      _invoke_test_method(scannedResult.rawContent);
    },);
  }

 /* Future<bool> blah() async {
    print("blah");
    try {
      ScanResult qrResult = await BarcodeScanner.scan();
      print("result ${qrResult.rawContent}");

      return true;
    } on PlatformException catch (ex) {
      print("platform ${ex}");
    } on FormatException catch (error) {
      print("platform ${error}");
    }

    return false;
  }
*/

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              scanqr,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          _scanQRCode();
        },
        tooltip: 'Scan QR',
        child: Icon(Icons.camera_alt),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
