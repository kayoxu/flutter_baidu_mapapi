//
//  BMFAnnotationViewManager.m
//  flutter_baidu_mapapi_map
//
//  Created by Zhang,Baojin on 2020/11/12.
//

#import "BMFAnnotationViewManager.h"

#import <flutter_baidu_mapapi_base/BMFMapModels.h>
#import <flutter_baidu_mapapi_base/UIColor+BMFString.h>

#import "BMFAnnotation.h"
#import "BMFFileManager.h"
#import <Masonry/Masonry.h>

@implementation BMFAnnotationViewManager

#pragma mark - annotationView
+ (BMFAnnotationModel *)annotationModelfromAnnotionView:(BMKAnnotationView *)view {
    return ((BMKPointAnnotation *)view.annotation).annotationModel;
}
/// 根据anntation生成对应的View
+ (BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id<BMKAnnotation>)annotation {
    if ([annotation isKindOfClass:[BMKPointAnnotation class]]) {
        BMFAnnotationModel *model =((BMKPointAnnotation *)annotation).annotationModel;
        NSString *identifier = model.identifier ? model.identifier : NSStringFromClass([BMKPointAnnotation class]);
        BMKPinAnnotationView *annotationView = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:identifier];
        
        if (!annotationView) {
            annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:identifier];
        }
        
        if (model.icon) {
            //TODO:image加入空值判断
            
            UIImage* i1 = [UIImage imageWithContentsOfFile:[[BMFFileManager defaultCenter] pathForFlutterImageName:model.icon]];
            
            if (model.icon2 || model.icon3) {
                UIImageView *annotationImage = [[UIImageView alloc] initWithFrame:annotationView.frame];
                NSMutableArray *images = [NSMutableArray array];
                [images addObject:i1];
                if (model.icon2) {
                    UIImage* i2 = [UIImage imageWithContentsOfFile:[[BMFFileManager defaultCenter] pathForFlutterImageName:model.icon2]];
                    
                    [images addObject:i2];
                }
                if (model.icon3) {
                    UIImage* i3 = [UIImage imageWithContentsOfFile:[[BMFFileManager defaultCenter] pathForFlutterImageName:model.icon3]];
                    [images addObject:i3];
                }
                annotationImage.animationImages = images;
                annotationImage.animationDuration = 0.5 * 3;
                annotationImage.animationRepeatCount = 0;
                [annotationImage startAnimating];
                
                annotationView.image = i1;
                //                annotationView.paopaoView = nil;
                annotationView.tag = 99791233;
                
                UIView* root  = [UIView new];
                
                [root addSubview:annotationImage];
                [annotationView addSubview:root];
                
                
                double titleSize = 10;
                double title2Size  = 10;
                UIColor* titleColor =  [UIColor grayColor];
                UIColor* title2Color =  [UIColor grayColor];
                NSInteger title2Line = 1;
                double maxWidth = titleSize*model.title.length;
                
                if (model.title2) {
                    titleSize = 12;
                    maxWidth = titleSize*model.title.length;
                    
                    NSArray *array2=[model.title2 componentsSeparatedByString:@"\n"];
                    title2Line = array2.count;
                    
                    for (int i = 0; i<array2.count; i++) {
                        NSString *d = array2[i];
                        double l = d.length*(title2Size);
                        if (l>maxWidth) {
                            maxWidth=l;
                        }
                    }
                    
                }
                if (model.titleColor) {
//                    titleColor =  [UIColor colorWithHexString: model.titleColor];
                }
                
                if (model.title2Color) {
//                    title2Color =  [UIColor colorWithHexString: model.title2Color];
                }
                
                
                annotationImage.frame = CGRectMake(0, 0, 32, 32);
                UILabel* l1 = UILabel.new;
                l1.text = model.title;
                l1.textColor = titleColor;
                l1.font = [UIFont fontWithName:@"Heiti SC" size:titleSize];
                
                [root addSubview:l1];
                
                [l1 mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(root).offset(0);
                    make.height.equalTo(@(10));
                    make.width.equalTo(@(maxWidth)) ;
                    make.left.equalTo(root).offset(34);
                    //                    make.right.equalTo(root).offset(-10);
                    //                    make.bottom.equalTo(root).offset(-10);
                    
                }];
                
                if (model.title2) {
                    UILabel* l2 = UILabel.new;
                    l2.text = [model.title2 stringByReplacingOccurrencesOfString:@"\n" withString:@" \r\n" ];
                    l2.textColor =  title2Color;
                    l2.numberOfLines = 0;
                    l2.font = [UIFont fontWithName:@"Heiti SC" size:title2Size];
                    [root addSubview:l2];
                    
                    [l2 mas_makeConstraints:^(MASConstraintMaker *make) {
                        make.top.equalTo(root).offset(26);
                        make.height.equalTo(@(16*title2Line));
                        make.width.equalTo(@(maxWidth)) ;
                        make.left.equalTo(root).offset(34);
                        //                        make.right.equalTo(root).offset(-10);
                        //                        make.bottom.equalTo(root).offset(10);
                    }];
                    
                }
                
                
            }else{
                annotationView.image = [UIImage imageWithContentsOfFile:[[BMFFileManager defaultCenter] pathForFlutterImageName:model.icon]];
            }
        }
        
        if (model.centerOffset) {
            annotationView.centerOffset = [model.centerOffset toCGPoint];
        }
        annotationView.canShowCallout = model.canShowCallout;
        annotationView.selected = model.selected;
        annotationView.draggable = model.draggable;
        annotationView.enabled = model.enabled;
        annotationView.enabled3D = model.enabled3D;
        annotationView.hidePaopaoWhenDrag = model.hidePaopaoWhenDrag;
        annotationView.hidePaopaoWhenDragOthers = model.hidePaopaoWhenDragOthers;
        annotationView.hidePaopaoWhenSelectOthers = model.hidePaopaoWhenSelectOthers;
        annotationView.hidePaopaoWhenDoubleTapOnMap = model.hidePaopaoWhenDoubleTapOnMap;
        annotationView.hidePaopaoWhenTwoFingersTapOnMap = model.hidePaopaoWhenTwoFingersTapOnMap;
        annotationView.displayPriority = (float)model.displayPriority;
        annotationView.canShowCallout = NO;
        return annotationView;
    }
    return nil;
}

@end
