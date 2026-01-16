package com.timelane.core.di;

import com.timelane.data.local.TaskDao;
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
public final class AppModule_ProvideTaskDaoFactory implements Factory<TaskDao> {
  private final Provider<TimeLaneDatabase> dbProvider;

  public AppModule_ProvideTaskDaoFactory(Provider<TimeLaneDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TaskDao get() {
    return provideTaskDao(dbProvider.get());
  }

  public static AppModule_ProvideTaskDaoFactory create(Provider<TimeLaneDatabase> dbProvider) {
    return new AppModule_ProvideTaskDaoFactory(dbProvider);
  }

  public static TaskDao provideTaskDao(TimeLaneDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTaskDao(db));
  }
}
