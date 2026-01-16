package com.timelane.core.di;

import android.app.Application;
import com.timelane.data.local.TimeLaneDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideDatabaseFactory implements Factory<TimeLaneDatabase> {
  private final Provider<Application> appProvider;

  public AppModule_ProvideDatabaseFactory(Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public TimeLaneDatabase get() {
    return provideDatabase(appProvider.get());
  }

  public static AppModule_ProvideDatabaseFactory create(Provider<Application> appProvider) {
    return new AppModule_ProvideDatabaseFactory(appProvider);
  }

  public static TimeLaneDatabase provideDatabase(Application app) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDatabase(app));
  }
}
