import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_baidu_mapapi_map_web/flutter_baidu_mapapi_map_web.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_baidu_mapapi_map_web');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterBaiduMapapiMapWeb.platformVersion, '42');
  });
}
