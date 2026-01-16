package com.timelane;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = TimeLaneApp.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface TimeLaneApp_GeneratedInjector {
  void injectTimeLaneApp(TimeLaneApp timeLaneApp);
}
