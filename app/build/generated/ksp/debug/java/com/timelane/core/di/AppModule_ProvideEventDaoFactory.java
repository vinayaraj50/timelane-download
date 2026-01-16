package com.timelane.core.di;

import com.timelane.data.local.EventDao;
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
public final class AppModule_ProvideEventDaoFactory implements Factory<EventDao> {
  private final Provider<TimeLaneDatabase> dbProvider;

  public AppModule_ProvideEventDaoFactory(Provider<TimeLaneDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public EventDao get() {
    return provideEventDao(dbProvider.get());
  }

  public static AppModule_ProvideEventDaoFactory create(Provider<TimeLaneDatabase> dbProvider) {
    return new AppModule_ProvideEventDaoFactory(dbProvider);
  }

  public static EventDao provideEventDao(TimeLaneDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEventDao(db));
  }
}
