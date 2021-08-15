
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterBaiduMapapiMapWeb {
  static const MethodChannel _channel =
      const MethodChannel('flutter_baidu_mapapi_map_web');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
