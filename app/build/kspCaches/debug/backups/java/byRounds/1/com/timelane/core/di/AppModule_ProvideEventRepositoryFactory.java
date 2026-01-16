package com.timelane.core.di;

import com.timelane.data.repository.EventRepositoryImpl;
import com.timelane.domain.repository.EventRepository;
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
public final class AppModule_ProvideEventRepositoryFactory implements Factory<EventRepository> {
  private final Provider<EventRepositoryImpl> repoProvider;

  public AppModule_ProvideEventRepositoryFactory(Provider<EventRepositoryImpl> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public EventRepository get() {
    return provideEventRepository(repoProvider.get());
  }

  public static AppModule_ProvideEventRepositoryFactory create(
      Provider<EventRepositoryImpl> repoProvider) {
    return new AppModule_ProvideEventRepositoryFactory(repoProvider);
  }

  public static EventRepository provideEventRepository(EventRepositoryImpl repo) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEventRepository(repo));
  }
}
