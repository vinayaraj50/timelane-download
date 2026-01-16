package com.timelane.core.di;

import com.timelane.data.repository.TaskRepositoryImpl;
import com.timelane.domain.repository.TaskRepository;
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
public final class AppModule_ProvideTaskRepositoryFactory implements Factory<TaskRepository> {
  private final Provider<TaskRepositoryImpl> repoProvider;

  public AppModule_ProvideTaskRepositoryFactory(Provider<TaskRepositoryImpl> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public TaskRepository get() {
    return provideTaskRepository(repoProvider.get());
  }

  public static AppModule_ProvideTaskRepositoryFactory create(
      Provider<TaskRepositoryImpl> repoProvider) {
    return new AppModule_ProvideTaskRepositoryFactory(repoProvider);
  }

  public static TaskRepository provideTaskRepository(TaskRepositoryImpl repo) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTaskRepository(repo));
  }
}
